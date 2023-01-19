package com.app.testik.presentation.activity

import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
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
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.GetCurrentUserUseCase
import com.app.testik.domain.usecase.GetTestInfoUseCase
import com.app.testik.presentation.base.BaseActivity
import com.app.testik.util.setAppLocale
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.Source
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

//    @Inject
//    lateinit var preferencesUseCase: PreferencesUseCase

    @Inject
    lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @Inject
    lateinit var getTestInfoUseCase: GetTestInfoUseCase

    @Inject
    lateinit var firebaseDynamicLinks: FirebaseDynamicLinks

    var testToInsert: TestPassedModel? = null
    var testToShow: String? = null

    private lateinit var navController: NavController

    override fun createBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment).findNavController()
        binding.bottomNavigationView.setupWithNavController(navController)

        getLink()
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

    private fun getLink() {
        firebaseDynamicLinks.getDynamicLink(intent).addOnSuccessListener(this) { pendingDynamicLinkData ->

            pendingDynamicLinkData?.link?.let { uri ->
                val link = uri.toString()

                when {
                    link.contains("test/") -> {
                        val testId = link.substring(link.lastIndexOf("/") + 1)
                        handleTestLink(testId)
                    }
                }
            }
        }
    }

    private fun handleTestLink(testId: String) {
        lifecycleScope.launch {
            getTestInfoUseCase(testId = testId, source = Source.SERVER).onSuccess {
                if (!it.isLinkEnabled) {
                    showSnackbar(R.string.test_link_disabled)
                    return@launch
                }

                if (getCurrentUserUseCase() == null) {
                    showSnackbar(R.string.log_in_to_get_test)
                    testToShow = testId
                } else {
                    navigateToTest(testId)
                }
            }.onError {
                showSnackbar(R.string.error_occurred)
            }
        }
    }

    private fun navigateToTest(testId: String) {
        val bundle = bundleOf("testId" to testId)
        navController.navigate(R.id.testInfoFragment, bundle)
    }

    private fun showSnackbar(@StringRes message: Int) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}