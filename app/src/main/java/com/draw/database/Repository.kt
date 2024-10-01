package com.draw.database

import android.content.Context
import androidx.lifecycle.LiveData
import com.draw.model.Animation

class Repository(context: Context) {
    private val animationDatabase = AnimationDatabase.getDatabase(context)
    fun getAllAnimations(): LiveData<List<Animation>> {
        return animationDatabase.animationDao().getAll()
    }


    suspend fun insertAnimation(animation: Animation) {
        animationDatabase.animationDao().insert(animation)
    }

    suspend fun deleteAnimation(animation: Animation) {
        animationDatabase.animationDao().delete(animation)
    }

    suspend fun updateAnimation(animation: Animation) {
        animationDatabase.animationDao().update(animation)
    }

}

