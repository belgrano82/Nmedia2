package ru.netology.nmedia.dto

data class Post(
    var id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    var published: String,
    val likedByMe: Boolean,
    var likes: Int = 0,
)

