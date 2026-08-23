package com.example.todoapp.presentation.activity

interface ToolbarManager {
    fun showBackButton(show: Boolean)
    fun enableDrawer(enabled: Boolean)
    fun setNavigationIcon(isBackArrow: Boolean)
}