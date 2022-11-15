package com.app.testik.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<T: ViewBinding> : AppCompatActivity() {

    protected val binding
        get() = _binding!!

    private var _binding: T? = null

    abstract fun createBinding(inflater: LayoutInflater): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createBinding(layoutInflater).also { _binding = it }
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}