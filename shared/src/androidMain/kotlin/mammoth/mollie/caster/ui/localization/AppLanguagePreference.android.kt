package mammoth.mollie.caster.ui.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import androidx.core.content.edit

private const val LANGUAGE_PREFERENCES = "molliecaster_preferences"
private const val LANGUAGE_KEY = "app_language"

@Composable
actual fun rememberAppLanguagePreference(): AppLanguagePreference {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val preferences = context.getSharedPreferences(LANGUAGE_PREFERENCES, Context.MODE_PRIVATE)
        val saved = preferences.getString(LANGUAGE_KEY, null)?.toAppLanguage()
        AppLanguagePreference(saved ?: languageFromCode(Locale.getDefault().language)) { language ->
            preferences.edit { putString(LANGUAGE_KEY, language.name) }
        }
    }
}

private fun String.toAppLanguage(): AppLanguage? = AppLanguage.entries.firstOrNull { it.name == this }
