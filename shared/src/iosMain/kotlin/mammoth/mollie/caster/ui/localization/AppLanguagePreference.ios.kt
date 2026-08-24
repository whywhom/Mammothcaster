package mammoth.mollie.caster.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults

private const val LANGUAGE_KEY = "app_language"

@Composable
actual fun rememberAppLanguagePreference(): AppLanguagePreference = remember {
    val defaults = NSUserDefaults.standardUserDefaults
    val saved = defaults.stringForKey(LANGUAGE_KEY)?.toAppLanguage()
    AppLanguagePreference(saved ?: languageFromCode(NSLocale.currentLocale.languageCode)) { language ->
        defaults.setObject(language.name, forKey = LANGUAGE_KEY)
    }
}

private fun String.toAppLanguage(): AppLanguage? = AppLanguage.entries.firstOrNull { it.name == this }
