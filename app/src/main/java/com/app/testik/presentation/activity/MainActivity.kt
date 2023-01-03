package com.app.testik.presentation.activity

import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.app.testik.R
import com.app.testik.data.repository.PreferencesRepositoryImpl
import com.app.testik.databinding.ActivityMainBinding
import com.app.testik.di.UtilsModule
import com.app.testik.domain.model.TestPassedModel
import com.app.testik.presentation.base.BaseActivity
import com.app.testik.util.setAppLocale
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

//    @Inject
//    lateinit var preferencesUseCase: PreferencesUseCase

    var testToInsert: TestPassedModel? = null

    private lateinit var navController: NavController

    override fun createBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment).findNavController()
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()
        checkTime()
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

    fun setNavbarItem(@IdRes destination: Int) {
        binding.bottomNavigationView.menu.findItem(destination).also {
            NavigationUI.onNavDestinationSelected(it, navController)
        }
    }

    private fun checkTime() {
        val isAutoTimeEnabled = Settings.Global.getInt(
            contentResolver, Settings.Global.AUTO_TIME, 0
        ) == 1

        if (!isAutoTimeEnabled) {
            AlertDialog.Builder(this)
                .setTitle(R.string.time_settings)
                .setMessage(R.string.enable_auto_time)
                .setPositiveButton(R.string.go_to_settings) { _, _ -> startActivity(Intent(Settings.ACTION_DATE_SETTINGS), null) }
                .setCancelable(false)
                .show()
        }
    }
}