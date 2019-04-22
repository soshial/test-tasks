package lv.chi.giffoid.app

/**
 * An easily injectable data class with read-only settings
 * This class allows us change any setting only in 1 place over the whole app
 */
data class AppSettings(
    val searchGridColumns: Int,
    val searchBatchLimit: Int,
    val visibleThreshold: Int,
    val apiKey: CharSequence
)