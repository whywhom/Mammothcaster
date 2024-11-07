package com.mammoth.podcast.data.network

import java.io.IOException
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.internal.closeQuietly

/**
 * Suspending wrapper around an OkHttp [Call], using [Call.enqueue].
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    enqueue(
        object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response) {
                    // If we have a response but we're cancelled while resuming, we need to
                    // close() the unused response
                    if (response.body != null) {
                        response.closeQuietly()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }
        }
    )

    continuation.invokeOnCancellation {
        try {
            cancel()
        } catch (t: Throwable) {
            // Ignore cancel exception
        }
    }
}
