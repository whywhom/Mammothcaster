package mammoth.mollie.caster.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import mammoth.mollie.caster.model.PodcastCategory
import molliecaster.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource as platformStringResource

enum class AppLanguage { English, Chinese }

class AppLanguagePreference(
    val initialLanguage: AppLanguage,
    private val saveLanguage: (AppLanguage) -> Unit,
) {
    fun save(language: AppLanguage) = saveLanguage(language)
}

/**
 * Reads a user-selected language when present.  Without a saved selection, the platform
 * implementation derives English or Chinese from its system locale and falls back to English.
 */
@Composable
expect fun rememberAppLanguagePreference(): AppLanguagePreference

fun languageFromCode(code: String?): AppLanguage =
    if (code?.lowercase()?.startsWith("zh") == true) AppLanguage.Chinese else AppLanguage.English

val LocalAppLanguage = compositionLocalOf { AppLanguage.English }

/** App-owned resource resolver so language selection works independently of the system locale. */
@Composable
fun stringResource(resource: StringResource, vararg formatArgs: Any): String {
    if (LocalAppLanguage.current == AppLanguage.English) return platformStringResource(resource, *formatArgs)
    return chineseStrings[resource]?.formatArguments(formatArgs) ?: platformStringResource(resource, *formatArgs)
}

@Composable
fun localizedCategoryName(category: PodcastCategory): String =
    if (LocalAppLanguage.current == AppLanguage.Chinese) chineseCategories[category.key] ?: category.displayName else category.displayName

private fun String.formatArguments(arguments: Array<out Any>): String =
    arguments.foldIndexed(this) { index, text, value ->
        text.replace("%${index + 1}\$d", value.toString()).replace("%${index + 1}\$s", value.toString())
    }

private val chineseStrings = mapOf(
    Res.string.app_name to "悦播客",
    Res.string.app_tagline to "灵韵之声",
    Res.string.more_actions to "更多操作",
    Res.string.refresh to "刷新",
    Res.string.add_rss_feed to "添加 RSS 订阅源",
    Res.string.opml_import_export to "OPML 导入 / 导出",
    Res.string.popular_podcasts to "热门播客",
    Res.string.recommended_for_you to "为你推荐",
    Res.string.latest_podcasts to "最新播客",
    Res.string.browse_categories to "浏览分类",
    Res.string.latest_episodes to "最新单集",
    Res.string.browse_all_categories to "浏览全部分类",
    Res.string.todays_resonance to "今日共鸣",
    Res.string.find_next_frequency to "发现你的下一档播客",
    Res.string.search_description to "按节目或作者搜索 Apple Podcasts 和你的资料库。",
    Res.string.search_hint to "按节目名称或作者搜索 Apple Podcasts。",
    Res.string.no_apple_podcasts to "没有找到 Apple Podcasts 节目",
    Res.string.category_results to "Apple Podcasts 分类结果",
    Res.string.searching_apple_podcasts to "正在搜索 Apple Podcasts…",
    Res.string.no_category_podcasts to "此分类中没有找到 Apple Podcasts 节目。",
    Res.string.showing_results to "显示 %1\$d / %2\$d",
    Res.string.show_more to "显示更多（还有 %1\$d 个）",
    Res.string.settings to "设置",
    Res.string.appearance to "外观",
    Res.string.playback to "播放",
    Res.string.downloads to "下载",
    Res.string.library_and_data to "资料库与数据",
    Res.string.dark_theme to "深色主题",
    Res.string.playback_speed to "播放速度",
    Res.string.sleep_timer to "睡眠定时器",
    Res.string.manage_downloads to "管理下载",
    Res.string.refresh_subscriptions to "刷新订阅",
    Res.string.refreshing_subscriptions to "正在刷新订阅…",
    Res.string.search_apple_podcasts to "搜索 Apple Podcasts",
    Res.string.searching to "正在搜索…",
    Res.string.in_your_library to "你的资料库",
    Res.string.apple_podcasts to "Apple Podcasts",
    Res.string.back_to_search to "返回搜索",
    Res.string.retry to "重试",
    Res.string.library to "资料库",
    Res.string.subscribe to "订阅",
    Res.string.cancel to "取消",
    Res.string.play to "播放",
    Res.string.pause to "暂停",
    Res.string.back to "返回",
    Res.string.newest to "最新",
    Res.string.oldest to "最早",
    Res.string.subscribed to "已订阅",
    Res.string.syncing to "正在同步…",
    Res.string.now_playing to "正在播放",
    Res.string.home to "首页",
    Res.string.search to "搜索",
    Res.string.language to "语言",
    Res.string.english to "英语",
    Res.string.chinese to "中文",
    Res.string.settings_description to "管理外观、播放、下载和资料库数据。",
    Res.string.using_dark_theme to "正在使用深色 Aether 主题",
    Res.string.using_light_theme to "正在使用浅色 Aether 主题",
    Res.string.active to "已启用",
    Res.string.off to "关闭",
    Res.string.minutes to "%1\$d 分钟",
    Res.string.turn_off_sleep_timer to "关闭睡眠定时器",
    Res.string.skip_interval to "快进与快退间隔",
    Res.string.skip_interval_summary to "前进和后退 15 秒",
    Res.string.downloads_mobile_and_wifi to "下载可使用 Wi-Fi 或移动数据",
    Res.string.downloads_wifi_only to "下载仅使用 Wi-Fi",
    Res.string.view_downloaded_episodes to "查看已下载的单集",
    Res.string.downloads_unavailable to "此平台暂不支持下载",
    Res.string.refresh_subscriptions_summary to "检查已订阅节目是否有新单集",
    Res.string.library_sync to "资料库同步",
    Res.string.library_sync_summary to "在播客应用之间转移订阅",
    Res.string.shows to "节目",
    Res.string.channels to "频道",
    Res.string.saved to "已收藏",
    Res.string.downloaded to "已下载",
    Res.string.recently_played to "最近播放",
    Res.string.category_description to "Apple Podcasts 顶级分类，另含 AI 分类",
    Res.string.back_to_library to "返回资料库",
    Res.string.data_management to "数据管理",
    Res.string.import_from_file to "从文件导入",
    Res.string.import_subscriptions_via_opml to "通过 OPML 导入订阅",
    Res.string.export_opml to "导出 OPML",
    Res.string.export_library to "导出资料库",
    Res.string.working to "正在处理…",
    Res.string.importing_subscriptions to "正在导入订阅…",
    Res.string.library_sync_description to "管理你的播客订阅，在播客应用之间安全迁移资料库。",
    Res.string.export_opml_description to "随身携带你的资料库。将当前订阅导出为 OPML 文件，以便备份或迁移。",
)

private val chineseCategories = mapOf(
    "arts" to "艺术", "business" to "商业", "comedy" to "喜剧", "education" to "教育",
    "fiction" to "小说", "government" to "政府", "history" to "历史", "health" to "健康与健身",
    "kids-family" to "儿童与家庭", "leisure" to "休闲", "music" to "音乐", "news" to "新闻",
    "religion-spirituality" to "宗教与灵性", "science" to "科学", "society-culture" to "社会与文化",
    "sports" to "体育", "technology" to "科技", "artificial-intelligence" to "人工智能",
    "true-crime" to "真实犯罪", "tv-film" to "电视与电影",
    "books" to "图书", "design" to "设计", "fashion-beauty" to "时尚与美容", "food" to "美食",
    "careers" to "职业", "entrepreneurship" to "创业", "investing" to "投资", "management" to "管理",
    "marketing" to "营销", "non-profit" to "非营利", "comedy-interviews" to "喜剧访谈", "improv" to "即兴喜剧",
    "stand-up" to "单口喜剧", "courses" to "课程", "how-to" to "实用指南", "language-learning" to "语言学习",
    "self-improvement" to "自我提升", "education-for-kids" to "儿童教育", "parenting" to "育儿",
    "pets-animals" to "宠物与动物", "stories-for-kids" to "儿童故事", "alternative-health" to "替代健康",
    "fitness" to "健身", "mental-health" to "心理健康", "nutrition" to "营养",
    "business-news" to "商业新闻", "daily-news" to "每日新闻", "politics" to "政治", "tech-news" to "科技新闻",
)
