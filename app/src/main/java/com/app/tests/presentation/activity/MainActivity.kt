package com.app.tests.presentation.activity

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.app.tests.R
import com.app.tests.data.repository.PreferencesRepositoryImpl
import com.app.tests.databinding.ActivityMainBinding
import com.app.tests.di.UtilsModule
import com.app.tests.presentation.base.BaseActivity
import com.app.tests.util.setAppLocale
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

//    @Inject
//    lateinit var preferencesUseCase: PreferencesUseCase

    override fun createBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
    }

    override fun attachBaseContext(newBase: Context) {
        val repository = PreferencesRepositoryImpl(UtilsModule.provideSharedPreferences(newBase))

        var lang = repository.getLanguage()
        if (lang.isEmpty()) {
            val defaultLanguage = Locale.getDefault().language
            repository.setLanguage(defaultLanguage)
            lang = repository.getLanguage()
        }

        super.attachBaseContext(ContextWrapper(newBase.setAppLocale(lang)))
    }

    fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }
}