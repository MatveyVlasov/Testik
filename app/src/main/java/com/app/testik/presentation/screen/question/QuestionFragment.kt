package com.app.testik.presentation.screen.question

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.R
import com.app.testik.databinding.FragmentQuestionBinding
import com.app.testik.presentation.activity.ImageViewActivity
import com.app.testik.presentation.adapter.answer.MultipleChoiceDelegateAdapter
import com.app.testik.presentation.adapter.answer.SingleChoiceDelegateAdapter
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.question.model.QuestionScreenEvent
import com.app.testik.presentation.screen.question.model.QuestionScreenUIState
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuestionFragment(private val question: QuestionDelegateItem) : BaseFragment<FragmentQuestionBinding>() {

    private val viewModel: QuestionViewModel by viewModels()

    private val answersAdapter by lazy {
        CompositeAdapter.Builder()
            .add(
                SingleChoiceDelegateAdapter(
                    onSelectClick = { item -> viewModel.onSelectClick(answer = item) }
                )
            )
            .add(
                MultipleChoiceDelegateAdapter(
                    onSelectClick = { item, isChecked -> viewModel.onSelectClick(answer = item, isChecked = isChecked) }
                )
            )
            .build()
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentQuestionBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        addBackPressedCallback { onBackPressed() }

        viewModel.updateQuestion(question)
    }


    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {
            rvAnswers.adapter = answersAdapter
        }
    }

    private fun initListeners() {
        binding.apply {
            ivImage.setOnClickListener { viewImage() }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        it.onSuccess { data ->
                            answersAdapter.submitList(data.answers)
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

    private fun handleEvent(event: QuestionScreenEvent) {
        when (event) {
            is QuestionScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is QuestionScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is QuestionScreenEvent.Loading -> Unit
        }
        setLoadingState(event is QuestionScreenEvent.Loading)
    }

    private fun renderUIState(data: QuestionScreenUIState) {
        binding.apply {
            tvTitle.text = data.title
            tvDescription.text = data.description
            tvType.setText(data.type.instruction)
        }

        loadImage(question.image)
    }

    private fun loadImage(url: String) {
        loadQuestionImage(context = requireContext(), imageView = binding.ivImage, url = url)

        binding.ivImage.isVisible = url.isNotEmpty()
    }

    private fun viewImage() {
        Intent(context, ImageViewActivity::class.java).also {
            it.putExtra(Constants.EXTRA_IMAGE_TITLE, getString(R.string.question_image))
            it.putExtra(Constants.EXTRA_IMAGE_PATH, question.image)
            startActivity(it)
        }
    }

    private fun onBackPressed() {
        navController.navigateUp()
    }
}