package com.epita.republic.domain

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

class SignalController {
    private val sharedFlow = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )

    suspend fun await() {
        Log.d("GAME", "Await Start")
        sharedFlow.first()
        Log.d("GAME", "Await End")
    }

    fun sendNotify() {
        Log.d("GAME", "Notify Start")
        sharedFlow.tryEmit(Unit)
        Log.d("GAME", "Notify End")
    }
}