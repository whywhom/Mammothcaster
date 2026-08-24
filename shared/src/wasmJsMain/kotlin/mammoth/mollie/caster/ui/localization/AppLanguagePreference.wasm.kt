package mammoth.mollie.caster.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.js.ExperimentalWasmJsInterop

private const val LANGUAGE_KEY = "molliecaster.app_language"

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => localStorage.getItem('molliecaster.app_language')")
private external fun storedLanguage(): String?

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("language => localStorage.setItem('molliecaster.app_language', language)")
private external fun saveStoredLanguage(language: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => navigator.language || ''")
private external fun systemLanguage(): String

@Composable
actual fun rememberAppLanguagePreference(): AppLanguagePreference = remember {
    val saved = storedLanguage()?.toAppLanguage()
    AppLanguagePreference(saved ?: languageFromCode(systemLanguage())) { language ->
        saveStoredLanguage(language.name)
    }
}

private fun String.toAppLanguage(): AppLanguage? = AppLanguage.entries.firstOrNull { it.name == this }
