package com.epita.republic.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

class SignalController {
    private val sharedFlow = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1
    )

    suspend fun await() {
        sharedFlow.first()
    }

    fun notify() {
        sharedFlow.tryEmit(Unit)
    }
}