package com.mammoth.podcast

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.common.MediaItem
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaSessionService

class MediaPlayerService : MediaSessionService() {
    private lateinit var exoPlayer: ExoPlayer
    private var mediaSession: MediaSession? = null

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "media_playback_channel"
    }

    override fun onCreate() {
        super.onCreate()
        // init ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build()
        // init MediaSession
        mediaSession = MediaSession.Builder(this, exoPlayer).build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        player?.let { exoPlayer ->
            if (!exoPlayer.playWhenReady
                || exoPlayer.mediaItemCount == 0
                || exoPlayer.playbackState == Player.STATE_ENDED) {
                // Stop the service if not playing,
                stopSelf()
            } else {
                // continue playing in the background otherwise.
                Log.d(this@MediaPlayerService::class.simpleName, "continue playing in the background")
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val uri = intent?.getStringExtra("EPISODE_URI") ?: return START_NOT_STICKY
        playEpisode(uri)
        return START_STICKY
    }

    private fun playEpisode(uri: String) {
        // Build the media item.
        val mediaItem = MediaItem.fromUri(uri)
        // Set the media item to be played.
        exoPlayer.setMediaItem(mediaItem)
        // Prepare the player.
        exoPlayer.prepare()
        // Start the playback.
        exoPlayer.play()
    }

    private fun startForegroundServiceWithNotification() {
        // Connect ExoPlayer and MediaSession
        // Start a foreground service using the built-in notification provider
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotification(): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Podcast Playing")
            .setContentText("Currently playing episode")
            .setSmallIcon(R.drawable.ic_logo)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(NotificationCompat.Action(androidx.media3.session.R.drawable.media3_icon_pause, "Pause", getPendingIntent("ACTION_PAUSE")))
            .addAction(NotificationCompat.Action(androidx.media3.session.R.drawable.media3_icon_stop, "Stop", getPendingIntent("ACTION_STOP")))

        return notificationBuilder.build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MediaPlayerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onDestroy() {
        mediaSession?.run {
            exoPlayer
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}