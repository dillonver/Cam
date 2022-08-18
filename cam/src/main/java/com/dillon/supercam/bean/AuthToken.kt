package com.dillon.supercam.bean

data class AuthToken(
    var token: String? = null,
    var user: UserInfo? = null
)