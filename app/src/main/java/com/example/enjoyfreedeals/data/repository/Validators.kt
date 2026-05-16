package com.example.enjoyfreedeals.data.repository

object Validators {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private val mobileRegex = Regex("^[6-9][0-9]{9}$")

    fun loginError(email: String, password: String): String? = when {
        email.isBlank() || password.isBlank() -> "Please enter your email and password."
        !emailRegex.matches(email) -> "Please enter a valid email address."
        else -> null
    }

    fun registrationError(name: String, email: String, mobile: String, password: String, confirmPassword: String): String? = when {
        name.isBlank() || email.isBlank() || mobile.isBlank() || password.isBlank() || confirmPassword.isBlank() -> "Please fill in all required fields."
        !emailRegex.matches(email) -> "Please enter a valid email address."
        !mobileRegex.matches(mobile) -> "Please enter a valid 10 digit mobile number."
        password.length < 6 -> "Password must be at least 6 characters."
        password != confirmPassword -> "Passwords do not match."
        else -> null
    }
}
