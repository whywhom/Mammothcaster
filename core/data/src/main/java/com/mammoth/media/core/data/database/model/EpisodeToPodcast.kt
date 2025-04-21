package com.mammoth.media.core.data.database.model

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import java.util.Objects

class EpisodeToPodcast {
    @Embedded
    lateinit var episode: Episode

    @Relation(parentColumn = "podcast_uri", entityColumn = "uri")
    lateinit var _podcasts: List<Podcast>

    @get:Ignore
    val podcast: Podcast
        get() = _podcasts[0]

    /**
     * Allow consumers to destructure this class
     */
    operator fun component1() = episode
    operator fun component2() = podcast

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is EpisodeToPodcast -> episode == other.episode && _podcasts == other._podcasts
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(episode, _podcasts)
}
