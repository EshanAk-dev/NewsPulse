package com.example.newsapplication

import java.io.Serializable
import java.util.UUID

data class News(
    var title: String = "",
    var category: String = "",
    var description: String = "",
    val imageUrl: String = "",
    val newsId: String = UUID.randomUUID().toString(),
    val reporterEmail: String = "",
    var status: String = "" // Add the published status
) : Serializable