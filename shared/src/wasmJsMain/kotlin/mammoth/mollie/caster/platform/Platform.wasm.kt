package mammoth.mollie.caster.platform

import kotlin.js.Date

actual fun currentTimeMillis(): Long = Date.now().toLong()
actual val platformName: String = "Web"
