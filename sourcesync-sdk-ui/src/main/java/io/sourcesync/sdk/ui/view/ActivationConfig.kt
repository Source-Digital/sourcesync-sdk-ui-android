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
 * Configuration builder for Activation views
 */
class ActivationConfig private constructor(
    val divConfiguration: DivConfiguration,
    val onPreviewClickHandler: View.OnClickListener?,
    val onOutsideClickHandler: Runnable?,
    val activationPosition: ActivationPosition
) {
    class Builder(private val context: Context) {
        private var visualErrorsEnabled = true
        private var divUrlHandler: CustomUrlHandler? = null

        private var onClickHandler: View.OnClickListener? = null
        private var onUrlActionTriggered: Runnable? = null
        private var onDetailsCloseClicked: Runnable? = null
        private var onOutsideClickHandler: Runnable? = null

        private var positionAlignment: Alignment? = null
        fun setClickHandler(onClickListener: View.OnClickListener) = apply {
            this.onClickHandler = onClickListener
        }

        fun setUrlActionHandler(handler: Runnable) = apply {
            this.onUrlActionTriggered = handler
        }

        fun setOutsideClickHandler(handler: Runnable) = apply {
            this.onOutsideClickHandler = handler
        }

        fun setDetailsCloseHandler(handler: Runnable) = apply {
            this.onDetailsCloseClicked = handler
        }

        fun setPositionAlignment(position: Alignment) = apply {
            this.positionAlignment = position
        }

        fun setVisualErrorsEnabled(enabled: Boolean) = apply {
            this.visualErrorsEnabled = enabled
        }

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