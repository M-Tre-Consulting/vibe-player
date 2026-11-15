package com.vibe.core.playback

import android.util.Log
import com.vibe.core.model.PlayedTrackRecord
import com.vibe.core.model.Queue
import com.vibe.core.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Single source of truth for Vibe's playback queue across all engines.
 * Manages currently playing track, user-queued items, context items, and play history.
 */
class QueueManager {

    companion object {
        private const val TAG = "VIBE_QUEUE"
    }

    private val _queue = MutableStateFlow(Queue())
    val queue: StateFlow<Queue> = _queue.asStateFlow()

    private var originalContextQueue: List<Track> = emptyList()
    private var isShuffleEnabled: Boolean = false

    fun setQueue(newQueue: Queue) {
        _queue.value = newQueue
    }

    fun setContextQueue(currentTrack: Track, contextTracks: List<Track>, contextName: String? = null) {
        val cleanCurrentId = currentTrack.id.substringAfterLast(":")
        val upcoming = if (contextTracks.isNotEmpty()) {
            val index = contextTracks.indexOfFirst { it.id.substringAfterLast(":") == cleanCurrentId }
            if (index != -1) contextTracks.drop(index + 1) else contextTracks.filterNot { it.id.substringAfterLast(":") == cleanCurrentId }
        } else emptyList()

        originalContextQueue = upcoming
        val effectiveUpcoming = if (isShuffleEnabled) upcoming.shuffled() else upcoming

        val prevPlaying = _queue.value.currentlyPlaying
        val history = if (prevPlaying != null && prevPlaying.id.substringAfterLast(":") != cleanCurrentId) {
            listOf(PlayedTrackRecord(prevPlaying)) + _queue.value.recentHistory.take(19)
        } else _queue.value.recentHistory

        _queue.value = Queue(
            currentlyPlaying = currentTrack,
            contextName = contextName,
            userQueue = _queue.value.userQueue, // Retain user-added queue items!
            contextQueue = effectiveUpcoming,
            recentHistory = history
        )
        Log.i(TAG, "Queue initialized: current='${currentTrack.name}', upcomingContext=${effectiveUpcoming.size}, userQueue=${_queue.value.userQueue.size}, shuffle=$isShuffleEnabled")
    }

    fun addToUserQueue(track: Track) {
        _queue.update { q ->
            q.copy(userQueue = q.userQueue + track)
        }
        Log.i(TAG, "Added '${track.name}' to userQueue (size=${_queue.value.userQueue.size})")
    }

    fun moveUserQueueItem(fromIndex: Int, toIndex: Int) {
        _queue.update { q ->
            val list = q.userQueue.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
                Log.i(TAG, "Moved userQueue item '${item.name}' from $fromIndex to $toIndex")
                q.copy(userQueue = list)
            } else q
        }
    }

    fun moveContextQueueItem(fromIndex: Int, toIndex: Int) {
        _queue.update { q ->
            val list = q.contextQueue.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
                Log.i(TAG, "Moved contextQueue item '${item.name}' from $fromIndex to $toIndex")
                q.copy(contextQueue = list)
            } else q
        }
    }

    fun removeTrack(track: Track) {
        _queue.update { q ->
            val uFiltered = q.userQueue.filterNot { it.id.substringAfterLast(":") == track.id.substringAfterLast(":") }
            val cFiltered = q.contextQueue.filterNot { it.id.substringAfterLast(":") == track.id.substringAfterLast(":") }
            Log.i(TAG, "Removed '${track.name}' from queue")
            q.copy(userQueue = uFiltered, contextQueue = cFiltered)
        }
    }

    fun clearQueue() {
        _queue.update { q ->
            Log.i(TAG, "Cleared userQueue and contextQueue")
            q.copy(userQueue = emptyList(), contextQueue = emptyList())
        }
    }

    fun peekNext(): Track? {
        val q = _queue.value
        return q.userQueue.firstOrNull() ?: q.contextQueue.firstOrNull()
    }

    fun hasUpcoming(): Boolean {
        val q = _queue.value
        return q.userQueue.isNotEmpty() || q.contextQueue.isNotEmpty()
    }

    fun advanceToNext(): Track? {
        val q = _queue.value
        val nextTrack = q.userQueue.firstOrNull() ?: q.contextQueue.firstOrNull() ?: return null

        val current = q.currentlyPlaying
        val history = if (current != null) {
            listOf(PlayedTrackRecord(current)) + q.recentHistory.take(19)
        } else q.recentHistory

        val fromUserQueue = q.userQueue.isNotEmpty()
        val newUserQueue = if (fromUserQueue) q.userQueue.drop(1) else q.userQueue
        val newContextQueue = if (!fromUserQueue && q.contextQueue.isNotEmpty()) q.contextQueue.drop(1) else q.contextQueue

        _queue.value = q.copy(
            currentlyPlaying = nextTrack,
            userQueue = newUserQueue,
            contextQueue = newContextQueue,
            recentHistory = history
        )
        Log.i(TAG, "advanceToNext: '${nextTrack.name}' is now playing. Remaining user=${newUserQueue.size}, context=${newContextQueue.size}")
        return nextTrack
    }

    fun advanceToPrevious(): Track? {
        val q = _queue.value
        val prevRecord = q.recentHistory.firstOrNull() ?: return null

        val current = q.currentlyPlaying
        val newUserQueue = if (current != null) listOf(current) + q.userQueue else q.userQueue
        val newHistory = q.recentHistory.drop(1)

        _queue.value = q.copy(
            currentlyPlaying = prevRecord.track,
            userQueue = newUserQueue,
            recentHistory = newHistory
        )
        Log.i(TAG, "advanceToPrevious: Restored '${prevRecord.track.name}' from history")
        return prevRecord.track
    }

    fun playTrackFromQueue(track: Track) {
        val q = _queue.value
        val current = q.currentlyPlaying
        val history = if (current != null && current.id.substringAfterLast(":") != track.id.substringAfterLast(":")) {
            listOf(PlayedTrackRecord(current)) + q.recentHistory.take(19)
        } else q.recentHistory

        val cleanId = track.id.substringAfterLast(":")
        _queue.value = q.copy(
            currentlyPlaying = track,
            userQueue = q.userQueue.filterNot { it.id.substringAfterLast(":") == cleanId },
            contextQueue = q.contextQueue.filterNot { it.id.substringAfterLast(":") == cleanId },
            recentHistory = history
        )
        Log.i(TAG, "playTrackFromQueue: '${track.name}'")
    }

    fun setShuffle(enabled: Boolean) {
        isShuffleEnabled = enabled
        _queue.update { q ->
            if (enabled) {
                if (originalContextQueue.isEmpty()) {
                    originalContextQueue = q.contextQueue
                }
                q.copy(contextQueue = q.contextQueue.shuffled())
            } else {
                if (originalContextQueue.isNotEmpty()) {
                    val restored = originalContextQueue.filter { orig -> q.contextQueue.any { it.id.substringAfterLast(":") == orig.id.substringAfterLast(":") } }
                    originalContextQueue = emptyList()
                    q.copy(contextQueue = restored)
                } else q
            }
        }
    }
}
