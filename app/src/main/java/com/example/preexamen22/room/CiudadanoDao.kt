package com.example.preexamen22.room

import androidx.room.*

@Dao
interface CiudadanoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ciudadano: CiudadanoEntity)

    @Update
    suspend fun update(ciudadano: CiudadanoEntity)

    @Delete
    suspend fun delete(ciudadano: CiudadanoEntity)

    @Query("SELECT * FROM ciudadano ORDER BY codigo ASC")
    suspend fun getAllCarnets(): List<CiudadanoEntity>

    @Query("DELETE FROM ciudadano")
    suspend fun deleteAll()
}