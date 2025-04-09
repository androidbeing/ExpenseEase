package com.dolphin.expenseease.data.model


data class Alert(
    val title: String,
    val message: String,
    val positiveButtonText: String = "Yes",
    val negativeButtonText: String = "No"
)