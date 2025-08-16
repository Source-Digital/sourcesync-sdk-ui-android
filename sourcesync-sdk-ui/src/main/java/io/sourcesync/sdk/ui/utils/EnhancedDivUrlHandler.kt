package io.sourcesync.sdk.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import com.yandex.div.core.DivActionHandler
import com.yandex.div.core.DivViewFacade
import com.yandex.div.json.expressions.ExpressionResolver
import com.yandex.div2.DivAction
import androidx.core.net.toUri

/**
 * EnhancedDivUrlHandler
 *
 * A comprehensive DivKit URL handler for Android that manages all URL types including:
 * - Close actions (div-action://close)
 * - External URLs (http/https)
 * - Custom scheme URLs
 * - Deep link handling
 */
class EnhancedDivUrlHandler(
    private val context: Context,
    private val onCloseAction: () -> Unit,
    private val onExternalUrlAction: ((Uri) -> Unit)? = null,
    private val onCustomSchemeAction: ((Uri) -> Unit)? = null,
) : DivActionHandler() {

    companion object {
        private const val TAG = "EnhancedDivUrlHandler"
    }

    override fun handleAction(
        action: DivAction,
        view: DivViewFacade,
        resolver: ExpressionResolver
    ): Boolean {
        super.handleAction(action, view, resolver)
        val urlString = action.url?.evaluate(resolver) ?: return false
        return handleUrl(urlString.toString())
    }

    private fun handleUrl(urlString: String): Boolean {
        val uri = try {
            urlString.toUri()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to parse URL: $urlString", e)
            return false
        }
        val normalizedUrlString = urlString.lowercase()

        Log.d(TAG, "Handling URL: $normalizedUrlString")

        return when {
            // Handle close action
            normalizedUrlString.startsWith("div-action://close") -> {
                handleCloseAction()
                true
            }

            // Handle external URLs (http/https)
            normalizedUrlString.startsWith("http://") || normalizedUrlString.startsWith("https://") -> {
                handleExternalUrl(uri)
                true
            }

            // Handle custom schemes
            uri.scheme != null && !normalizedUrlString.startsWith("http") -> {
                handleCustomScheme(uri)
                true
            }

            else -> {
                Log.w(TAG, "⚠️ Unhandled URL: $normalizedUrlString")
                false
            }
        }
    }

    // MARK: - Private Action Handlers

    private fun handleCloseAction() {
        Log.d(TAG, "Executing close action")
        try {
            onCloseAction()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing close action", e)
        }
    }

    private fun handleExternalUrl(uri: Uri) {
        Log.d(TAG, "Opening external URL: $uri")

        try {
            // Use custom handler if provided, otherwise open with system
            onExternalUrlAction?.invoke(uri)
            openWithSystem(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open external URL: $uri", e)
        }
    }

    private fun handleCustomScheme(uri: Uri) {
        Log.d(TAG, "Handling custom scheme: $uri")
        onCustomSchemeAction?.invoke(uri)
        val scheme = uri.scheme?.lowercase() ?: return

        try {
            when (scheme) {
                "mailto" -> handleMailtoUrl(uri)
                "tel" -> handleTelephoneUrl(uri)
                "sms" -> handleSmsUrl(uri)
                "div-action" -> handleDivAction(uri)
                else -> {
                    // Use custom handler or fallback
                    attemptSystemOpen(uri)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling custom scheme: $uri", e)
        }
    }

    private fun handleMailtoUrl(uri: Uri) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            ContextCompat.startActivity(context, intent, null)
        } else {
            Log.e(TAG, "No email app available")
        }
    }

    private fun handleTelephoneUrl(uri: Uri) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            ContextCompat.startActivity(context, intent, null)
        } else {
            Log.e(TAG, "No phone app available")
        }
    }

    private fun handleSmsUrl(uri: Uri) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            ContextCompat.startActivity(context, intent, null)
        } else {
            Log.e(TAG, "No SMS app available")
        }
    }

    private fun handleDivAction(uri: Uri) {
        val path = uri.path?.lowercase() ?: ""
        val host = uri.host?.lowercase() ?: ""

        when ("$host$path") {
            "close", "/close" -> handleCloseAction()
            "refresh", "/refresh" -> handleRefreshAction()
            "back", "/back" -> handleBackAction()
            else -> Log.w(TAG, "Unknown div-action: $uri")
        }
    }

    private fun handleRefreshAction() {
        Log.d(TAG, "Refresh action triggered")
        // Add refresh logic here
    }

    private fun handleBackAction() {
        Log.d(TAG, "Back action triggered")
        // Add back navigation logic here
    }

    private fun openWithSystem(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            ContextCompat.startActivity(context, intent, null)
        } else {
            Log.e(TAG, "No app available to handle: $uri")
        }
    }

    private fun attemptSystemOpen(uri: Uri) {
        try {
            openWithSystem(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open custom scheme: $uri", e)
        }
    }
}