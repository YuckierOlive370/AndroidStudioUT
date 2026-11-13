package com.example.practica14.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.practica14.MarcadorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarcadorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(marcador: MarcadorEntity): Long

    @Query("SELECT * FROM marcadores ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<MarcadorEntity>>

    @Query("DELETE FROM marcadores WHERE id = :id")
    suspend fun eliminarPorId(id: Long)
}