package com.draw.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.model.Animation
import com.draw.model.DrawInfo

class AnimationViewModel(context: Context) : ViewModel() {
    private val repository = Repository(context)
    fun getAllAnimations(): LiveData<List<Animation>> {
        return repository.getAllAnimations()
    }
    suspend fun insertAnimation(animation: Animation) {
        repository.insertAnimation(animation)
    }

    suspend fun deleteAnimation(animation: Animation) {
        repository.deleteAnimation(animation)
    }
    suspend fun updateAnimation(animation: Animation) {
        repository.updateAnimation(animation)
    }

}
