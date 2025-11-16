package io.sourcesync.sdk.ui.view

import android.content.Context
import android.view.View
import com.yandex.div.core.DivConfiguration
import io.sourcesync.sdk.ui.models.ActivationPosition
import io.sourcesync.sdk.ui.helpers.CustomUrlHandler
import io.sourcesync.sdk.ui.helpers.PicassoDivImageLoader
import io.sourcesync.sdk.ui.models.Alignment
import io.sourcesync.sdk.ui.utils.createDivUrlHandler

/**
 * Configuration builder for ActivationView components with DivKit integration
 * Manages handlers, positioning, and visual settings for activation displays
 */
class ActivationConfig private constructor(
    val divConfiguration: DivConfiguration,
    val onPreviewClickHandler: View.OnClickListener?,
    val onOutsideClickHandler: Runnable?,
    val activationPosition: ActivationPosition
) {

    /**
     * Builder for creating ActivationConfig with fluent interface
     * @param context Android context for display metrics and resource access
     */
    class Builder(private val context: Context) {
        private var visualErrorsEnabled = true
        private var divUrlHandler: CustomUrlHandler? = null

        private var onClickHandler: View.OnClickListener? = null
        private var onUrlActionTriggered: Runnable? = null
        private var onDetailsCloseClicked: Runnable? = null
        private var onOutsideClickHandler: Runnable? = null
        private var positionAlignment: Alignment? = null

        /**
         * Sets click handler for preview interactions
         * @param onClickListener Listener to handle preview tap events
         * @return Builder instance for method chaining
         */

        fun setClickHandler(onClickListener: View.OnClickListener) = apply {
            this.onClickHandler = onClickListener
        }

        /**
         * Sets handler for URL action events (external links, custom schemes)
         * @param handler Runnable to execute when URL actions are triggered
         * @return Builder instance for method chaining
         */
        fun setUrlActionHandler(handler: Runnable) = apply {
            this.onUrlActionTriggered = handler
        }

        /**
         * Sets handler for outside click detection (typically for dismissing details)
         * @param handler Runnable to execute when clicking outside activation area
         * @return Builder instance for method chaining
         */
        fun setOutsideClickHandler(handler: Runnable) = apply {
            this.onOutsideClickHandler = handler
        }

        /**
         * Sets handler for details close button interactions
         * @param handler Runnable to execute when details close is triggered
         * @return Builder instance for method chaining
         */
        fun setDetailsCloseHandler(handler: Runnable) = apply {
            this.onDetailsCloseClicked = handler
        }

        /**
         * Sets positioning alignment for activation view placement
         * @param position Alignment configuration for horizontal/vertical positioning
         * @return Builder instance for method chaining
         */
        fun setPositionAlignment(position: Alignment) = apply {
            this.positionAlignment = position
        }

        /**
         * Enables or disables visual error indicators in DivKit rendering
         * @param enabled true to show visual errors, false to hide
         * @return Builder instance for method chaining
         */
        fun setVisualErrorsEnabled(enabled: Boolean) = apply {
            this.visualErrorsEnabled = enabled
        }

        /**
         * Builds ActivationConfig with current settings and screen metrics
         * Creates DivConfiguration with image loader and URL handler integration
         * @return Configured ActivationConfig instance ready for use
         */
        fun build(): ActivationConfig {
            val metrics = context.resources.displayMetrics

            // Create the URL handler
            divUrlHandler = context.createDivUrlHandler(onCloseAction = {
                onDetailsCloseClicked?.run()
            }, onExternalUrlAction = { uri ->
                onUrlActionTriggered?.run()
            }, onCustomSchemeAction = { uri ->
                onUrlActionTriggered?.run()
            })

            val divConfig = DivConfiguration.Builder(PicassoDivImageLoader(context)).apply {
                divUrlHandler?.let { actionHandler(it) }
                visualErrorsEnabled(visualErrorsEnabled)
            }.build()

            return ActivationConfig(
                divConfiguration = divConfig,
                onPreviewClickHandler = onClickHandler,
                onOutsideClickHandler = onOutsideClickHandler,
                activationPosition = ActivationPosition(screenWidth = metrics.widthPixels, screenHeight = metrics.heightPixels,positionAlignment)
            )
        }
    }
}