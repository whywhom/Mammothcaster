package mammoth.mollie.caster.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.Locale
import java.util.prefs.Preferences

private const val LANGUAGE_KEY = "app_language"

@Composable
actual fun rememberAppLanguagePreference(): AppLanguagePreference = remember {
    val preferences = Preferences.userRoot().node("mammoth/mollie/caster")
    val saved = preferences.get(LANGUAGE_KEY, null)?.toAppLanguage()
    AppLanguagePreference(saved ?: languageFromCode(Locale.getDefault().language)) { language ->
        preferences.put(LANGUAGE_KEY, language.name)
    }
}

private fun String.toAppLanguage(): AppLanguage? = AppLanguage.entries.firstOrNull { it.name == this }
