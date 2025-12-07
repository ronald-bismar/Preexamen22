package com.example.preexamen22.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * @Database: Anotación principal de Room.
 * - entities: Lista de todas las tablas (Entities).
 * - version: Número de versión de la BD. Si cambias la estructura (agregas tablas/columnas), debes aumentar este número.
 * - exportSchema: Si es true
 */
@Database(entities = [CiudadanoEntity::class], version = 1, exportSchema = false)
abstract class RegistroCivil : RoomDatabase() {

    // Método abstracto para obtener el DAO. Room generará el código de implementación automáticamente.
    abstract fun carnetDao(): CiudadanoDao

    /**
     * PatrónSingleton: Garantiza que solo exista UNA instancia de la base de datos
     * en toda la aplicación.
     */
    companion object {
        // @Volatile: Asegura que los cambios en esta variable sean visibles inmediatamente para todos los hilos.
        @Volatile
        private var INSTANCE: RegistroCivil? = null

        fun getDatabase(context: Context): RegistroCivil {
            // Si la instancia ya existe, la retornamos.
            return INSTANCE ?: synchronized(this) {
                // Si no existe, entramos en un bloque sincronizado para evitar condiciones de carrera en hilos múltiples.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RegistroCivil::class.java,
                    "registro_civil" // Nombre del archivo de base de datos
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}