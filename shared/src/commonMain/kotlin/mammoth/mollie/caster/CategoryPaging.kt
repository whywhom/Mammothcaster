package mammoth.mollie.caster

const val CATEGORY_PAGE_SIZE = 50

fun nextCategoryVisibleCount(current: Int, total: Int): Int =
    (current + CATEGORY_PAGE_SIZE).coerceAtMost(total)
