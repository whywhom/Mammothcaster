package mammoth.mollie.caster.ui.localization

import kotlin.test.Test
import kotlin.test.assertEquals

class AppLanguageTest {
    @Test
    fun `Chinese system locale selects Chinese`() {
        assertEquals(AppLanguage.Chinese, languageFromCode("zh-CN"))
        assertEquals(AppLanguage.Chinese, languageFromCode("ZH_tw"))
    }

    @Test
    fun `English and unsupported locales select English`() {
        assertEquals(AppLanguage.English, languageFromCode("en-NZ"))
        assertEquals(AppLanguage.English, languageFromCode("ja-JP"))
        assertEquals(AppLanguage.English, languageFromCode(null))
    }
}
