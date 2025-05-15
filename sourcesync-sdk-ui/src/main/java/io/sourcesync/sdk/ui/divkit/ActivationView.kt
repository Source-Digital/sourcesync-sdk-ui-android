package io.sourcesync.sdk.ui.divkit

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.yandex.div.core.DivConfiguration
import io.sourcesync.sdk.ui.utils.LayoutUtils.asTemplateAndCardParsed
import io.sourcesync.sdk.ui.utils.PicassoDivImageLoader
import org.json.JSONException
import org.json.JSONObject

/**
 * A view representing an activation component with preview and detail views.
 */
class ActivationView(private val context: Context) : FrameLayout(context) {
    private var previewView: ActivationPreview? = null
    private var detailView: ActivationDetail? = null
    private var onPreviewClickHandler: Runnable? = null
    private val handler = Handler()

    private fun createDivConfiguration(): DivConfiguration {
        return DivConfiguration.Builder(PicassoDivImageLoader(context))
            .visualErrorsEnabled(true)
            .build()
    }

    /**
     * Shows the preview view with given data.
     *
     * @param previewParentJson JSON data for preview.
     * @param onClickListener Listener to execute on click.
     */
    @Throws(JSONException::class)
    fun showPreview(
        previewParentJson: JSONObject,
        onClickListener: OnClickListener
    ) {
        if (previewView != null) {
            removeView(previewView)
        }

        this.onPreviewClickHandler = Runnable { onClickListener.onClick(this) }
        val previewData = previewParentJson.asTemplateAndCardParsed()

        try {
            previewView = ActivationPreview(context, previewData, createDivConfiguration())
            previewView!!.setOnClickListener { _: View? ->
                if (onPreviewClickHandler != null) {
                    previewView!!.visibility = GONE
                    onPreviewClickHandler!!.run()
                }
            }

            val params = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            // Add view with initial params
            addView(previewView, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating preview view: " + e.message)
        }
    }

    /**
     * Shows the detail view with given data.
     *
     * @param detailsParentJson JSON data for detail.
     * @param onClose Runnable to execute on close.
     */
    fun showDetail(detailsParentJson: JSONObject, onClose: Runnable?) {
        if (detailView != null) {
            removeView(detailView)
        }

        try {
            val detailsData = detailsParentJson.asTemplateAndCardParsed()
            detailView = ActivationDetail(context, detailsData,createDivConfiguration(), onClose)

            val params = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(detailView, params)
        } catch (e: JSONException) {
            Log.e(TAG, "Error creating detail view: " + e.message)
        }
    }

    /**
     * Hides the detail view and restores preview.
     */
    fun hideDetails() {
        if (detailView != null) {
            removeView(detailView)
            detailView = null
        }
    }

    override fun onDetachedFromWindow() {
        // Clean up resources
        handler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }

    companion object {
        private const val TAG = "SDK:ActivationView"
    }
}