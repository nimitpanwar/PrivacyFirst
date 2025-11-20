package com.secure.privacyfirst.network

import com.google.gson.annotations.SerializedName

// Request models
data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

data class AddUrlRequest(
    @SerializedName("url")
    val url: String
)

data class UpdateUrlRequest(
    @SerializedName("oldUrl")
    val oldUrl: String,
    @SerializedName("newUrl")
    val newUrl: String
)

data class DeleteUrlRequest(
    @SerializedName("url")
    val url: String
)

// Response models with proper validation and defaults
data class LoginResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("expiresIn")
    val expiresIn: String = "1h"
) {
    init {
        require(token.isNotBlank()) { "Token cannot be empty" }
    }
}

data class WhitelistResponse(
    @SerializedName("urls")
    val urls: List<String> = emptyList(),
    @SerializedName("count")
    val count: Int = 0
) {
    init {
        require(count >= 0) { "Count cannot be negative" }
        require(urls.size == count) { "URLs list size must match count" }
    }
}

data class ApiErrorResponse(
    @SerializedName("message")
    val message: String = "Unknown error"
)

data class SuccessResponse(
    @SerializedName("message")
    val message: String = "Success",
    @SerializedName("doc")
    val doc: WhitelistDoc? = null
)

data class WhitelistDoc(
    @SerializedName("url")
    val url: String,
    @SerializedName("addedBy")
    val addedBy: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("_id")
    val id: String
)
