package com.example.preexamen22.room

import android.R
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ciudadano")
data class CiudadanoEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int=0,
    val fecha_nac: String,
    val paterno: String,
    val materno: String,
    val nombre: String,
    val qr: String,
    val codigo: String
)