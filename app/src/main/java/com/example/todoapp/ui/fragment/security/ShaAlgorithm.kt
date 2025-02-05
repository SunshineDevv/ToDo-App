package com.example.todoapp.ui.fragment.security

enum class ShaAlgorithm(val algorithm: String, val sizeOfSecret: Int) {
    SHA1("HmacSHA1", 16),
    SHA256("HmacSHA256", 32),
    SHA512("HmacSHA512", 64)
}