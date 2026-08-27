package mammoth.mollie.caster.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

private const val LANGUAGE_KEY = "app_language"

@Composable
actual fun rememberAppLanguagePreference(): AppLanguagePreference = remember {
    val defaults = NSUserDefaults.standardUserDefaults
    val saved = defaults.stringForKey(LANGUAGE_KEY)?.toAppLanguage()
    // NSLocale's class properties are not exported by the current Kotlin/Native Foundation bindings.
    // AppleLanguages is the same ordered system preference list used by iOS for localization.
    val systemLanguage = (defaults.objectForKey("AppleLanguages") as? List<*>)?.firstOrNull() as? String
    AppLanguagePreference(saved ?: languageFromCode(systemLanguage)) { language ->
        defaults.setObject(language.name, forKey = LANGUAGE_KEY)
    }
}

private fun String.toAppLanguage(): AppLanguage? = AppLanguage.entries.firstOrNull { it.name == this }
