package com.example.todoapp.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        enableEdgeToEdge()

        binding?.main?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    private fun setupActionBar() {
        val navController = findNavController()

        setSupportActionBar(binding?.toolbar)

        binding?.toolbar?.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateToolbars(destination)
        }
    }

    private fun updateToolbars(destination: NavDestination) {
        when (destination.id) {
            R.id.noteFragment -> {
                (supportActionBar)?.apply {
                    title = "ToDoApp"
                    setDisplayHomeAsUpEnabled(true)
                    setHomeButtonEnabled(true)
                }
            }

            else -> {
                (supportActionBar)?.apply {
                    title = "ToDoApp"
                    setDisplayHomeAsUpEnabled(false)
                }
            }
        }
    }

    private fun findNavController(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        return navHostFragment.navController
    }
}

