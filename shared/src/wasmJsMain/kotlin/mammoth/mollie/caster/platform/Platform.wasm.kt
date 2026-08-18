package mammoth.mollie.caster.platform

import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun browserCurrentTimeMillis(): Double

actual fun currentTimeMillis(): Long = browserCurrentTimeMillis().toLong()
actual val platformName: String = "Web"
