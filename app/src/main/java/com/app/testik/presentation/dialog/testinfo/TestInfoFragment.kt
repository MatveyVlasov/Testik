package com.app.testik.presentation.dialog.testinfo

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
import com.app.testik.databinding.FragmentTestInfoBinding
import com.app.testik.domain.model.TestPassedModel
import com.app.testik.presentation.activity.ImageViewActivity
import com.app.testik.presentation.base.BaseBottomSheetDialogFragment
import com.app.testik.presentation.dialog.testinfo.model.TestInfoDialogEvent
import com.app.testik.presentation.dialog.testinfo.model.TestInfoDialogUIState
import com.app.testik.presentation.model.onSuccess
import com.app.testik.util.*
import com.app.testik.util.Constants.EXTRA_IMAGE_PATH
import com.app.testik.util.Constants.EXTRA_IMAGE_TITLE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestInfoFragment : BaseBottomSheetDialogFragment<FragmentTestInfoBinding>() {

    private val viewModel: TestInfoViewModel by viewModels()

    override fun createBinding(inflater: LayoutInflater) = FragmentTestInfoBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()
    }


    private fun initViews() {

        binding.apply {

        }
    }

    private fun initListeners() {
        binding.apply {

            ivImage.setOnClickListener { viewImage() }
            btnStart.setOnClickListener { viewModel.createTestPassed() }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        state.onSuccess {
                            renderUIState(it)
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun handleEvent(event: TestInfoDialogEvent) {
        when (event) {
            is TestInfoDialogEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is TestInfoDialogEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is TestInfoDialogEvent.SuccessTestCreation -> navigateToQuestionMain(event.test)
        }
    }

    private fun renderUIState(data: TestInfoDialogUIState) {
        binding.apply {
            tvCategory.setText(data.category.description)
            tvTitle.text = data.title
            tvAuthor.text = data.authorName
            tvQuestionsNum.text = resources.getQuantityString(R.plurals.questions_num, data.questionsNum, data.questionsNum)
            tvPointsNum.text = resources.getQuantityString(R.plurals.points_max, data.pointsMax, data.pointsMax)
            tvDescriptionTitle.isVisible = data.description.isNotEmpty()
            tvDescription.isVisible = data.description.isNotEmpty()
            tvDescription.text = data.description
        }

        loadImage(data.image)
    }

    private fun loadImage(url: String) {
        loadTestImage(context = requireContext(), imageView = binding.ivImage, url = url)

        binding.ivImage.isVisible = url.isNotEmpty()
    }

    private fun viewImage() {
        Intent(context, ImageViewActivity::class.java).also {
            it.putExtra(EXTRA_IMAGE_TITLE, getString(R.string.test_image))
            it.putExtra(EXTRA_IMAGE_PATH, viewModel.screenUIState.image)
            startActivity(it)
        }
    }

    private fun navigateToQuestionMain(test: TestPassedModel) {
        navController.navigate(
            TestInfoFragmentDirections.toQuestionMain(test)
        )
    }
}