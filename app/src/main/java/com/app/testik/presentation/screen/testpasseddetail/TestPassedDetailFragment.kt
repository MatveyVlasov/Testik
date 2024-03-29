package com.app.testik.presentation.screen.testpasseddetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.R
import com.app.testik.databinding.FragmentTestPassedDetailBinding
import com.app.testik.presentation.adapter.ErrorDelegateAdapter
import com.app.testik.presentation.adapter.LoadingDelegateAdapter
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.testpasseddetail.adapter.QuestionAnsweredDelegateAdapter
import com.app.testik.presentation.screen.testpasseddetail.model.TestPassedDetailScreenEvent
import com.app.testik.presentation.screen.testpasseddetail.model.TestPassedDetailScreenUIState
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestPassedDetailFragment : BaseFragment<FragmentTestPassedDetailBinding>() {

    private val viewModel: TestPassedDetailViewModel by viewModels()

    private val questionsAdapter by lazy {
        CompositeAdapter.Builder()
            .add(ErrorDelegateAdapter(viewModel::updateList))
            .add(LoadingDelegateAdapter())
            .add(
                QuestionAnsweredDelegateAdapter { navigateToQuestion(it) }
            )
            .build()
    }

    private var enableProgressAnimation = true

    override fun createBinding(inflater: LayoutInflater) = FragmentTestPassedDetailBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()
    }

    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {
            rvQuestions.adapter = questionsAdapter
        }
    }

    private fun initListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { navController.navigateUp() }

            ivImage.setOnClickListener { viewImage(image = viewModel.screenUIState.image) }

            tvTitleData.setOnClickListener { navigateToTestInfo() }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        it.onSuccess { data ->
                            questionsAdapter.submitList(data.questions)
                            renderUIState(data)
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun renderUIState(data: TestPassedDetailScreenUIState) {
        binding.apply {
            clTestPassedDetail.isVisible = true

            tvTitleData.text = data.title
            tvUsername.text = data.username
            tvDateData.text = data.date
            tvTimeSpentData.text = data.timeSpent
            tvPointsData.text = getString(R.string.points_earned, data.pointsEarned, data.pointsMax)

            progressPoints.apply {
                setProgress(data.pointsEarned.toDouble(), data.pointsMax.toDouble())
                setProgressTextAdapter {
                    getProgressPointsText(
                        pointsEarned = data.pointsEarned,
                        pointsMax = data.pointsMax,
                        gradeEarned = data.gradeEarned
                    )
                }

                isAnimationEnabled = enableProgressAnimation
                enableProgressAnimation = false // animate only once
            }

            val showPoints = data.isResultsShown && (data.isFinished || data.pointsCalculated)
            progressPoints.isVisible = showPoints
            tvPoints.isVisible = showPoints
            tvPointsData.isVisible = showPoints
            tvPointsUnavailable.isVisible = !showPoints
            tvQuestionList.isVisible = showPoints && data.questions.isNotEmpty()
            rvQuestions.isVisible = showPoints

            if (data.pointsHasError) tvPointsUnavailable.text = getString(R.string.points_error)

            val showUser = data.username.isNotEmpty()
            tvUser.isVisible = showUser
            tvUsername.isVisible = showUser

            tvNotFinished.isVisible = !data.isFinished && !data.pointsHasError
        }
        loadImage(data.image)
        setLoadingState(false)
    }

    private fun handleEvent(event: TestPassedDetailScreenEvent) {
        when (event) {
            is TestPassedDetailScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is TestPassedDetailScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is TestPassedDetailScreenEvent.Loading -> Unit
        }
        setLoadingState(event is TestPassedDetailScreenEvent.Loading)
    }

    private fun loadImage(url: String) {
        loadTestImage(context = requireContext(), imageView = binding.ivImage, url = url)

        binding.ivImage.isVisible = url.isNotEmpty()
    }

    private fun navigateToQuestion(question: QuestionDelegateItem) {
        viewModel.testPassed?.let {
            val questions = viewModel.screenUIState.questions.filterIsInstance<QuestionDelegateItem>().toTypedArray()
            navController.navigate(
                TestPassedDetailFragmentDirections.toQuestionMain(
                    test = it,
                    questions = questions,
                    startQuestion = questionsAdapter.currentList.indexOf(question)
                )
            )
        }
    }

    private fun navigateToTestInfo() {
        navController.navigate(
            TestPassedDetailFragmentDirections.toTestInfo(viewModel.screenUIState.testId)
        )
    }
}