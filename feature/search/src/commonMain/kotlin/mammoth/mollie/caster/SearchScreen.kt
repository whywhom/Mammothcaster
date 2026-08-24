package mammoth.mollie.caster

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mammoth.mollie.caster.data.LibraryState
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.model.PodcastCategories
import mammoth.mollie.caster.model.PodcastCategory
import mammoth.mollie.caster.ui.components.EmptyHint
import mammoth.mollie.caster.ui.components.PodcastArtwork
import mammoth.mollie.caster.ui.components.SectionTitle
import mammoth.mollie.caster.ui.theme.AetherTheme
import mammoth.mollie.caster.util.normalizeFeedUrl
import molliecaster.shared.generated.resources.*
import mammoth.mollie.caster.ui.localization.stringResource
import mammoth.mollie.caster.ui.localization.localizedCategoryName

@Composable
fun SearchScreen(
    state: LibraryState,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onCategory: (PodcastCategory) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val normalized = query.trim().lowercase()
    val submitSearch = {
        if (normalized.isNotBlank() && !state.appleSearchLoading) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            onSearch(query)
        }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.search_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth().onPreviewKeyEvent { event ->
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                            submitSearch()
                            true
                        } else false
                    },
                    singleLine = true,
                    label = { Text(stringResource(Res.string.search_apple_podcasts)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.78f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { submitSearch() },
                        onDone = { submitSearch() },
                    ),
                )
                Button(
                    onClick = submitSearch,
                    enabled = normalized.isNotBlank() && !state.appleSearchLoading,
                    shape = RoundedCornerShape(24.dp),
                ) { Text(if (state.appleSearchLoading) stringResource(Res.string.searching) else stringResource(Res.string.search_apple_podcasts)) }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(stringResource(Res.string.browse_all_categories))
                Text(
                    stringResource(Res.string.category_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CategoryGrid(
                    categories = PodcastCategories.all,
                    selectedKey = null,
                    onSelect = onCategory,
                )
            }
        }
        if (normalized.isBlank()) item {
            Text(stringResource(Res.string.search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Results are deliberately separate from the search form so the query survives detail navigation. */
@Composable
fun SearchResultsScreen(
    state: LibraryState,
    query: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onPodcast: (Podcast) -> Unit,
    onPreview: (Podcast) -> Unit,
) {
    val normalized = query.trim().lowercase()
    val resultsCurrent = state.appleSearchQuery.equals(query.trim(), ignoreCase = true)
    val searchError = state.appleSearchError
    val directoryPodcasts = state.appleSearchResults + state.appleCategoryResults
    val localResults = state.podcasts.filter { podcast ->
        normalized in podcast.title.lowercase() || normalized in podcast.author.lowercase()
    }.map { podcast ->
        if (!podcast.artworkUrl.isNullOrBlank()) podcast else {
            directoryPodcasts.firstOrNull { directoryPodcast ->
                normalizeFeedUrl(directoryPodcast.feedUrl) == normalizeFeedUrl(podcast.feedUrl) ||
                    normalizeFeedUrl(directoryPodcast.canonicalFeedUrl) == normalizeFeedUrl(podcast.canonicalFeedUrl)
            }?.artworkUrl?.let { artworkUrl -> podcast.copy(artworkUrl = artworkUrl) } ?: podcast
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            LibraryDetailHeader("Search results", stringResource(Res.string.back_to_search), onBack)
            Text(
                "Results for \"${query.trim()}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        when {
            !resultsCurrent || state.appleSearchLoading -> item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    Text(stringResource(Res.string.searching_apple_podcasts), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            searchError != null -> item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(searchError, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
                }
            }
            else -> {
                if (localResults.isNotEmpty()) {
                    item { Text(stringResource(Res.string.in_your_library), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(localResults) { PodcastSearchRow(it, onPodcast) }
                }
                if (state.appleSearchResults.isNotEmpty()) {
                    item { Text(stringResource(Res.string.apple_podcasts), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(state.appleSearchResults) { PodcastSearchRow(it, onPreview) }
                }
                if (localResults.isEmpty() && state.appleSearchResults.isEmpty()) {
                    item { Text(stringResource(Res.string.no_apple_podcasts)) }
                }
            }
        }
    }
}

@Composable
fun CategorySearchResultsScreen(
    state: LibraryState,
    category: PodcastCategory,
    visibleCount: Int,
    onVisibleCountChange: (Int) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onPodcast: (Podcast) -> Unit,
    onPreview: (Podcast) -> Unit,
) {
    val resultsCurrent = state.appleCategoryKey == category.key
    val categoryError = state.appleCategoryError
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            LibraryDetailHeader(localizedCategoryName(category), stringResource(Res.string.back_to_search), onBack)
            Text(
                stringResource(Res.string.category_results),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        when {
            !resultsCurrent || state.appleCategoryLoading -> item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    Text(stringResource(Res.string.searching_apple_podcasts), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            categoryError != null -> item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(categoryError, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
                }
            }
            state.appleCategoryResults.isEmpty() -> item {
                EmptyHint(stringResource(Res.string.no_category_podcasts))
            }
            else -> {
                item {
                    Text(
                        stringResource(Res.string.showing_results, minOf(visibleCount, state.appleCategoryResults.size), state.appleCategoryResults.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                items(state.appleCategoryResults.take(visibleCount)) { podcast ->
                    PodcastSearchRow(
                        podcast,
                        if (state.podcasts.any { it.id == podcast.id }) onPodcast else onPreview,
                    )
                }
                if (visibleCount < state.appleCategoryResults.size) {
                    item {
                        val remaining = state.appleCategoryResults.size - visibleCount
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onVisibleCountChange(nextCategoryVisibleCount(visibleCount, state.appleCategoryResults.size))
                            },
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Text(stringResource(Res.string.show_more, minOf(CATEGORY_PAGE_SIZE, remaining)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryGrid(
    categories: List<PodcastCategory>,
    selectedKey: String?,
    onSelect: (PodcastCategory) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columnCount = if (maxWidth < 600.dp) 2 else 4
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            categories.chunked(columnCount).forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowCategories.forEach { category ->
                        val selected = selectedKey == category.key
                        Surface(
                            modifier = Modifier.weight(1f).clickable { onSelect(category) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else AetherTheme.colors.glass,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            border = BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            ),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 18.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Text(
                                    localizedCategoryName(category),
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    repeat(columnCount - rowCategories.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
fun PodcastSearchRow(podcast: Podcast, onPodcast: (Podcast) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onPodcast(podcast) },
        color = AetherTheme.colors.glass,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PodcastArtwork(podcast.artworkUrl, podcast.title, 64); Spacer(Modifier.width(14.dp))
            Column {
                Text(podcast.title, style = MaterialTheme.typography.titleMedium)
                Text(podcast.author, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
