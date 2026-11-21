package com.cinegram.cine.profiles

/**
 * Um perfil local. O Telegram continua sendo uma conta só:
 * o perfil é a lente que separa progresso, histórico e favoritos.
 */
data class Profile(
    val id: String,
    val name: String,
    val avatar: Int,
    val kid: Boolean = false,
    val pin: String? = null,
)
