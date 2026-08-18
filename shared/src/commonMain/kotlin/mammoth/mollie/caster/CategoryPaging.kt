package mammoth.mollie.caster

internal const val CATEGORY_PAGE_SIZE = 50

internal fun nextCategoryVisibleCount(current: Int, total: Int): Int =
    (current + CATEGORY_PAGE_SIZE).coerceAtMost(total)
