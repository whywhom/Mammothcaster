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
        // 初始化 ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build()
        // 初始化 MediaSession
        mediaSession = MediaSession.Builder(this, exoPlayer).build()
        // 设置通知
//        startForegroundServiceWithNotification()
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
        // 连接 ExoPlayer 和 MediaSession
        // 启动前台服务，使用内置通知提供器
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