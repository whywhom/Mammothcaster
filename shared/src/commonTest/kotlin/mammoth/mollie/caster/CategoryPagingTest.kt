package mammoth.mollie.caster

import kotlin.test.Test
import kotlin.test.assertEquals

class CategoryPagingTest {
    @Test
    fun showMoreRevealsFiftyAtATimeAndThenTheRemainder() {
        assertEquals(100, nextCategoryVisibleCount(current = 50, total = 137))
        assertEquals(137, nextCategoryVisibleCount(current = 100, total = 137))
        assertEquals(40, nextCategoryVisibleCount(current = 0, total = 40))
    }
}
