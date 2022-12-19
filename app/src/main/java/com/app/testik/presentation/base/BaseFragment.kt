package com.app.testik.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.app.testik.presentation.activity.MainActivity

abstract class BaseFragment<T: ViewBinding> : Fragment() {

    protected val navController: NavController
        get() = findNavController()

    protected val binding
        get() = _binding!!

    private var _binding: T? = null

    private val currentBackStackEntry: NavBackStackEntry by lazy {
        checkNotNull(navController.currentBackStackEntry)
    }

    abstract fun createBinding(inflater: LayoutInflater): T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = createBinding(inflater).also { _binding = it }.root

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun <T> observeResult(key: String, resultHandler: (T) -> Unit) {
        currentBackStackEntry.savedStateHandle
            .getLiveData<T>(key)
            .observe(viewLifecycleOwner) {
                it?.let {
                    resultHandler(it)
                    currentBackStackEntry.savedStateHandle[key] = null
                }
            }
    }

    protected fun setResult(key: String, result: Any) {
        navController.previousBackStackEntry?.savedStateHandle?.set(key, result)
    }

    protected fun setLoadingState(isLoading: Boolean) {
        (activity as? MainActivity)?.setLoadingState(isLoading)
    }

    protected fun setNavbarItem(@IdRes destination: Int, bundle: Bundle = bundleOf()) {
        (activity as? MainActivity)?.setNavbarItem(destination, bundle)
    }
}