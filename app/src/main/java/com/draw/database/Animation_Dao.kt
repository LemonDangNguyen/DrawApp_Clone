package com.draw.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.draw.model.Animation

@Dao
interface Animation_Dao {
    @Insert
    suspend fun insert(animation: Animation):Long

    @Query("SELECT * FROM animations")
    fun getAll(): LiveData<List<Animation>>

    @Delete
    suspend fun delete(animation: Animation)

    @Update
    suspend fun update(animation: Animation)

}