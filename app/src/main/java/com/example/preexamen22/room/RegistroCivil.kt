package com.example.preexamen22.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CiudadanoEntity::class], version = 1, exportSchema = false)
abstract class RegistroCivil : RoomDatabase() {

    abstract fun carnetDao(): CiudadanoDao

    companion object {
        @Volatile
        private var INSTANCE: RegistroCivil? = null

        fun getDatabase(context: Context): RegistroCivil {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RegistroCivil::class.java,
                    "registro_civil"
                ).build()
                INSTANCE = instance
                instance
            }
        }


    }
}