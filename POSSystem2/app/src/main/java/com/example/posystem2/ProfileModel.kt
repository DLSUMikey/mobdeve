package com.example.posystem2

data class ProfileModel(
        var id: Int,
        val email: String,
        val password: String,
        val firstName: String,
        val lastName: String,
        val phoneNumber: String,
        val userType: String // "Employee" or "Admin"
)

