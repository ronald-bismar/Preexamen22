package com.example.preexamen22.room

import androidx.room.*

/**
 * @Dao (Data Access Object): Objeto de Acceso a Datos.
 * Es una interfaz que define los métodos que usaremos para interactuar con la base de datos.
 * Room generará automáticamente el código SQL necesario para cada método.
 */
@Dao
interface CiudadanoDao {

    /**
     * @Insert: Inserta un nuevo registro en la tabla.
     * onConflict = OnConflictStrategy.REPLACE: Si intentamos insertar un registro que ya existe
     * (mismo ID), lo reemplaza en lugar de dar error.
     *
     * suspend: Indica que esta función es una "función de suspensión" (Corutina).
     * Debe ejecutarse en un hilo secundario (background thread) para no congelar la app.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ciudadano: CiudadanoEntity)

    /**
     * @Update: Actualiza un registro existente.
     * Busca un registro con la misma @PrimaryKey (id) y actualiza sus campos.
     */
    @Update
    suspend fun update(ciudadano: CiudadanoEntity)

    /**
     * @Delete: Elimina un registro específico de la base de datos.
     */
    @Delete
    suspend fun delete(ciudadano: CiudadanoEntity)

    /**
     * @Query: Permite escribir consultas SQL personalizadas.
     * "SELECT * FROM ciudadano": Selecciona todas las columnas de la tabla 'ciudadano'.
     * "ORDER BY codigo ASC": Ordena los resultados alfabéticamente por el código generado.
     */
    @Query("SELECT * FROM ciudadano ORDER BY codigo ASC")
    suspend fun getAllCarnets(): List<CiudadanoEntity>

    /**
     * Elimina TODOS los registros de la tabla 'ciudadano'.
     * Útil para limpiar la base de datos.
     */
    @Query("DELETE FROM ciudadano")
    suspend fun deleteAll()
}