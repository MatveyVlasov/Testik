package com.app.testik.presentation.screen.gradingsystem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.model.*
import com.app.testik.presentation.screen.gradingsystem.mapper.toDomain
import com.app.testik.presentation.screen.gradingsystem.mapper.toGradeItem
import com.app.testik.presentation.screen.gradingsystem.model.GradeDelegateItem
import com.app.testik.presentation.screen.gradingsystem.model.GradingSystemScreenEvent
import com.app.testik.presentation.screen.gradingsystem.model.GradingSystemScreenUIState
import com.app.testik.util.removeExtraSpaces
import com.app.testik.util.toIntOrZero
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class GradingSystemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTestGradesUseCase: GetTestGradesUseCase,
    private val updateGradesUseCase: UpdateGradesUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<GradingSystemScreenUIState>>
        get() = _uiState

    val event: SharedFlow<GradingSystemScreenEvent>
        get() = _event

    val hasUnsavedChanges
        get() = screenUIState != oldScreenUIState

    private val _uiState = MutableStateFlow<UIState<GradingSystemScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<GradingSystemScreenEvent>()

    private val args = GradingSystemFragmentArgs.fromSavedStateHandle(savedStateHandle)

    var screenUIState = GradingSystemScreenUIState(testId = args.testId)
        private set

    private var oldScreenUIState = screenUIState.copy()

    init {
        getGrades()
    }

    fun saveGrades() {
        if (!validateData()) return
        emitEvent(GradingSystemScreenEvent.Loading)

        viewModelScope.launch {
            val grades = screenUIState.grades.map { it.toDomain() }

            updateGradesUseCase(
                testId = screenUIState.testId,
                isGradesEnabled = screenUIState.isEnabled,
                grades = grades
            ).onSuccess {
                oldScreenUIState = screenUIState
                emitEvent(GradingSystemScreenEvent.ShowSnackbarByRes(R.string.saved))
            }.onError {
                handleError(it)
            }
        }
    }

    fun onGradeTextChanged(grade: GradeDelegateItem, text: String) {
        val item = screenUIState.grades.find { it.id == grade.id } ?: return
        if (text == item.grade) return

        val pos = screenUIState.grades.indexOf(item)
        val grades = screenUIState.grades.map { it }.toMutableList().also {
            it[pos] = it[pos].copy(grade = text.removeExtraSpaces())
        }
        screenUIState = screenUIState.copy(grades = grades)
    }

    fun onFromTextChanged(grade: GradeDelegateItem, text: String) {
        val item = screenUIState.grades.find { it.id == grade.id } ?: return
        if (text == item.pointsFrom) return

        val pos = screenUIState.grades.indexOf(item)
        val grades = screenUIState.grades.map { it }.toMutableList().also {
            it[pos] = it[pos].copy(pointsFrom = text)
        }
        screenUIState = screenUIState.copy(grades = grades)
    }

    fun onToTextChanged(grade: GradeDelegateItem, text: String) {
        val item = screenUIState.grades.find { it.id == grade.id } ?: return
        if (text == item.pointsTo) return

        val pos = screenUIState.grades.indexOf(item)
        val grades = screenUIState.grades.map { it }.toMutableList().also {
            it[pos] = it[pos].copy(pointsTo = text)
        }
        screenUIState = screenUIState.copy(grades = grades)
    }

    fun onEnableChanged(isEnabled: Boolean) {
        if (isEnabled == screenUIState.isEnabled) return
        updateScreenState(screenUIState.copy(isEnabled = isEnabled))
    }

    fun addGrade() {
        val grades = screenUIState.grades.map { it }.toMutableList().also {
            it.add(GradeDelegateItem())
        }
        updateScreenState(screenUIState.copy(grades = grades))
    }

    fun deleteGrade(grade: GradeDelegateItem) {
        val grades = screenUIState.grades.map { it }.toMutableList().also {
            it.removeIf { item -> item.id == grade.id }
        }
        updateScreenState(screenUIState.copy(grades = grades))
    }

    fun moveGrade(from: Int, to: Int) {
        val grades = screenUIState.grades.map { it }.toMutableList().also {
            val item = it[from]
            it.removeAt(from)
            it.add(to, item)
        }
        updateScreenState(screenUIState.copy(grades = grades))
    }

    private fun getGrades() {
        emitEvent(GradingSystemScreenEvent.Loading)

        viewModelScope.launch {
            getTestGradesUseCase(screenUIState.testId).onSuccess {
                val screenState = screenUIState.copy(
                    isEnabled = it.isEnabled,
                    grades = it.grades.map { grade -> grade.toGradeItem() }
                )
                oldScreenUIState = screenState
                updateScreenState(screenState)
            }.onError {
                handleError(it)
            }
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(GradingSystemScreenEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("error occurred") -> {
                emitEvent(GradingSystemScreenEvent.ShowSnackbarByRes(R.string.error_occurred))
            }
            else -> emitEvent(GradingSystemScreenEvent.ShowSnackbar(error))
        }
    }

    private fun validateData(): Boolean {
        val points = mutableMapOf<Int, Boolean>()

        screenUIState.grades.forEach { grade ->
            val from = grade.pointsFrom.toIntOrZero()
            val to = grade.pointsTo.toIntOrZero()

            (checkGradeNotEmpty(grade) && checkGradeIntervalSize(from, to)).also { isValid ->
                if (!isValid) return false
            }

            for (i in from..to) {
                if (points[i] == true) {
                    emitEvent(GradingSystemScreenEvent.ErrorOverlappingIntervals(i))
                    return false
                }
                points[i] = true
            }
        }
        return true
    }

    private fun checkGradeNotEmpty(grade: GradeDelegateItem): Boolean {
        return (grade.grade.isNotEmpty() && grade.pointsFrom.isNotEmpty() && grade.pointsTo.isNotEmpty()).also {
            if (!it) emitEvent(GradingSystemScreenEvent.ShowSnackbarByRes(R.string.fill_in_empty_fields))
        }
    }

    private fun checkGradeIntervalSize(from: Int, to: Int): Boolean {
        return (from <= to).also {
            if (!it) emitEvent(GradingSystemScreenEvent.ShowSnackbarByRes(R.string.correct_grades_incorrect_interval))
        }
    }

    private fun updateScreenState(state: GradingSystemScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: GradingSystemScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}