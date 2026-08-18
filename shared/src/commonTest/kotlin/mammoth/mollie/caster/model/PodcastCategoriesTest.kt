package mammoth.mollie.caster.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PodcastCategoriesTest {
    @Test
    fun subscriptionCategoriesOnlyContainSubscribedPodcastCategories() {
        val subscribed = podcast(
            id = "subscribed",
            subscribed = true,
            PodcastCategory("technology", "Tech"),
            PodcastCategory("independent", "Independent"),
        )
        val unsubscribed = podcast(
            id = "unsubscribed",
            subscribed = false,
            PodcastCategory("comedy", "Comedy"),
        )

        val categories = PodcastCategories.fromSubscriptions(listOf(unsubscribed, subscribed))

        assertEquals(listOf("technology", "independent"), categories.map(PodcastCategory::key))
        assertEquals("Technology", categories.first().displayName)
    }

    @Test
    fun catalogContainsEveryAppleTopLevelCategoryAndAi() {
        val keys = PodcastCategories.all.mapTo(mutableSetOf(), PodcastCategory::key)

        assertTrue(
            setOf(
                "arts", "business", "comedy", "education", "fiction", "government", "history",
                "health", "kids-family", "leisure", "music", "news", "religion-spirituality",
                "science", "society-culture", "sports", "technology", "true-crime", "tv-film",
                "artificial-intelligence",
            ).all(keys::contains),
        )
    }

    private fun podcast(
        id: String,
        subscribed: Boolean,
        vararg categories: PodcastCategory,
    ) = Podcast(
        id = PodcastId(id),
        feedUrl = "https://example.com/$id.xml",
        title = id,
        categories = categories.toList(),
        isSubscribed = subscribed,
    )
}
