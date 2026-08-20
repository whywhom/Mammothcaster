package mammoth.mollie.caster.ui.format

internal fun formatDuration(millis: Long): String {
    val seconds = millis.coerceAtLeast(0) / 1000
    val hours = seconds / 3600
    val minutes = seconds / 60 % 60
    val remaining = seconds % 60
    return if (hours > 0) "$hours:${minutes.toString().padStart(2, '0')}:${remaining.toString().padStart(2, '0')}" else "$minutes:${remaining.toString().padStart(2, '0')}"
}

internal fun formatPlaybackSpeed(speed: Float): String =
    if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()

internal fun formatDate(millis: Long?): String? = millis?.let {
    var z = it / 86_400_000L + 719_468L
    val era = if (z >= 0) z / 146_097L else (z - 146_096L) / 146_097L
    val dayOfEra = z - era * 146_097L
    val yearOfEra = (dayOfEra - dayOfEra / 1_460L + dayOfEra / 36_524L - dayOfEra / 146_096L) / 365L
    var year = yearOfEra + era * 400L
    val dayOfYear = dayOfEra - (365L * yearOfEra + yearOfEra / 4L - yearOfEra / 100L)
    val monthPart = (5L * dayOfYear + 2L) / 153L
    val day = dayOfYear - (153L * monthPart + 2L) / 5L + 1L
    val month = monthPart + if (monthPart < 10) 3 else -9
    if (month <= 2) year++
    "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}
