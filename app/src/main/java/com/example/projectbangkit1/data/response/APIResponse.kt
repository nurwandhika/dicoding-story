package com.example.projectbangkit1.data.response

import com.google.gson.annotations.SerializedName

data class APIResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null,
)