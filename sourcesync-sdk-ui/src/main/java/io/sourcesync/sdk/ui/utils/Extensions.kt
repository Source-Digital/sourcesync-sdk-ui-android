package io.sourcesync.sdk.ui.utils

import android.content.Context
import android.net.Uri

/**
 * Extension function to easily create and configure the URL handler
 */
fun Context.createDivUrlHandler(
    onCloseAction: () -> Unit,
    onExternalUrlAction: ((Uri) -> Unit)? = null,
    onCustomSchemeAction: ((Uri) -> Unit)? = null,
): EnhancedDivUrlHandler {
    return EnhancedDivUrlHandler(
        context = this,
        onCloseAction = onCloseAction,
        onExternalUrlAction = onExternalUrlAction,
        onCustomSchemeAction = onCustomSchemeAction
    )
}