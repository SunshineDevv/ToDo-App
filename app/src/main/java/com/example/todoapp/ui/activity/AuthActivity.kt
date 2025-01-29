package com.example.todoapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.ActivityAuthBinding
import com.example.todoapp.ui.fragment.security.SecurePreferencesHelper
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private var binding: ActivityAuthBinding? = null

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding?.root)
        setupActionBar()
        binding?.mainAuth?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        setupTabLayout()
        changeVisualOfActivity()
    }

    private fun setupTabLayout() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding?.tabLayout?.apply {
            addTab(newTab().setText("Log in"))
            addTab(newTab().setText("Sign up"))
        }

        binding?.tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        if (navController.currentDestination?.id != R.id.logInFragment) {
                            navController.navigate(R.id.logInFragment)
                        }
                    }

                    1 -> {
                        if (navController.currentDestination?.id != R.id.signUpFragment) {
                            navController.navigate(R.id.signUpFragment)
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.logInFragment -> binding?.tabLayout?.selectTab(binding?.tabLayout?.getTabAt(0))
                R.id.signUpFragment -> binding?.tabLayout?.selectTab(binding?.tabLayout?.getTabAt(1))
            }
        }
    }


    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val success = SecurePreferencesHelper.getSuccess(this)
        if (success == "true" || success == "") {
            if (currentUser != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    private fun changeVisualOfActivity() {
        navController = findNavController()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.twoAuthFragment, R.id.forgetPassFragment -> {
                    binding?.toolbar?.visibility = View.VISIBLE
                    toggleFullScreenMode(true)
                }

                else -> {
                    toggleFullScreenMode(false)
                }
            }
        }
    }

    private fun toggleFullScreenMode(isFullScreen: Boolean) {
        binding?.apply {
            if (isFullScreen) {
                val fragmentParams = fragmentContainerView.layoutParams as ConstraintLayout.LayoutParams
                fragmentParams.topToBottom = R.id.toolbar
                fragmentParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                fragmentContainerView.layoutParams = fragmentParams

                tabLayout.visibility = View.GONE
                imageView.visibility = View.GONE
                textViewMyNotes.visibility = View.GONE
            } else {
                val fragmentParams = fragmentContainerView.layoutParams as ConstraintLayout.LayoutParams
                fragmentParams.topToBottom = R.id.tabLayout
                fragmentParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                fragmentContainerView.layoutParams = fragmentParams

                tabLayout.visibility = View.VISIBLE
                textViewMyNotes.visibility = View.VISIBLE
                imageView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupActionBar() {
        val navController = findNavController()

        val toolbar = AppBarConfiguration(
            topLevelDestinationIds = setOf(R.id.logInFragment)
        )

        setSupportActionBar(binding?.toolbar)

        binding?.toolbar?.setupWithNavController(navController, toolbar)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateToolbars(destination)
        }
    }

    private fun findNavController(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        return navHostFragment.navController
    }

    private fun updateToolbars(destination: NavDestination) {
        when (destination.id) {
            R.id.twoAuthFragment -> {
                supportActionBar?.apply {
                    title = "2FA Authentication"
                    setDisplayHomeAsUpEnabled(true)
                    setHomeButtonEnabled(true)
                }

                binding?.toolbar?.setNavigationOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }
            }

            R.id.forgetPassFragment -> {
                supportActionBar?.apply {
                    title = "Recovering password"
                    setDisplayHomeAsUpEnabled(true)
                    setHomeButtonEnabled(true)
                }

                binding?.toolbar?.setNavigationOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
            else -> {
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(false)
                    setHomeButtonEnabled(false)
                }
                binding?.toolbar?.visibility = View.INVISIBLE
            }
        }
    }


}