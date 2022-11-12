package com.app.tests.presentation.activity

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.app.tests.R
import com.app.tests.data.repository.PreferencesRepositoryImpl
import com.app.tests.di.UtilsModule
import com.app.tests.util.setAppLocale
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

//    @Inject
//    lateinit var preferencesUseCase: PreferencesUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setupWithNavController(navHostFragment.findNavController())
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = PreferencesRepositoryImpl(UtilsModule.provideSharedPreferences(newBase)).getLanguage()
        super.attachBaseContext(ContextWrapper(newBase.setAppLocale(lang)))
    }
}
