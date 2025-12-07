package com.example.preexamen22.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @Entity: Indica que esta clase representa una TABLA en la base de datos.
 * Cada instancia de esta clase será una FILA en esa tabla.
 *
 * tableName = "ciudadano": Define el nombre específico de la tabla.
 */
@Entity(tableName = "ciudadano")
data class CiudadanoEntity(
    /**
     * @PrimaryKey: Marca este campo como la LLAVE PRIMARIA.
     * Es el identificador único de cada registro.
     *
     * autoGenerate = true: SQLite generará este ID automáticamente (1, 2, 3...)
     * por lo que no necesitamos asignarlo manualmente.
     */
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,

    // Columnas de la tabla para guardar los datos del ciudadano
    val fecha_nac: String, // Guarda la fecha como texto (ej: "01/01/2000")
    val paterno: String,
    val materno: String,
    val nombre: String,
    val qr: String,        // Guarda la imagen del QR codificada en Base64 (texto largo)
    val codigo: String     // El código generado (ej: "LPA-01012000")
)