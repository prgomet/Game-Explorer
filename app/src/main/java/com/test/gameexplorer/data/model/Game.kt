package com.test.gameexplorer.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Game(
    val id: Int,
    val name: String,
    val backgroundImage: String?,
    val rating: Double,
    val released: String?,
    val genres: List<String>,
) : Parcelable
