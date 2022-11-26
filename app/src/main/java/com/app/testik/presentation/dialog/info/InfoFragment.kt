package com.app.testik.presentation.dialog.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.fragment.navArgs
import com.app.testik.databinding.FragmentInfoBinding
import com.app.testik.presentation.base.BaseBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InfoFragment : BaseBottomSheetDialogFragment<FragmentInfoBinding>() {

    private val args: InfoFragmentArgs by navArgs()

    override fun createBinding(inflater: LayoutInflater) = FragmentInfoBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            tvText.text = args.text
        }
    }
}