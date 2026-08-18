# Screen contract

## Primary navigation

Use Home, Search, Library, and Downloads as primary destinations. Player is a contextual destination presented through a mini-player and full-player route. Podcast detail and category browsing are secondary routes.

## Home sections

- Recommended: editorial or deterministic seed strategy, clearly separable from personalized data.
- Popular: source-backed ranking or an explicit local fallback; do not fabricate popularity metrics.
- Latest: podcasts or episodes ordered by real publication/update time.
- Categories: Technology, Business, Artificial Intelligence, Health, News, Comedy, Education, Science, Society & Culture, Arts, Sports, History, Music, True Crime, Kids & Family, and Government.

## State expectations

Every network-backed screen has initial loading, refresh-in-progress, content, empty, partial-data, and retryable error representations. User-owned actions use optimistic updates only when rollback is defined.

## Responsive expectations

- Compact: bottom navigation, single-pane navigation, full-screen player.
- Medium: navigation rail when useful and room for richer lists/player sheet.
- Expanded/desktop/web: rail or sidebar plus list/detail panes where it reduces navigation churn.
- Maintain sensible maximum content widths; do not stretch long descriptions across the entire desktop viewport.

