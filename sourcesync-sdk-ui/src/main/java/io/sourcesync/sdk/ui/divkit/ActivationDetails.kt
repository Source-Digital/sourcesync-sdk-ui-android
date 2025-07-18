package io.sourcesync.sdk.ui.divkit

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.yandex.div.DivDataTag
import com.yandex.div.core.Div2Context
import com.yandex.div.core.DivConfiguration
import com.yandex.div.core.view2.Div2View
import com.yandex.div2.DivData

@SuppressLint("ViewConstructor")
class ActivationDetails(
    context: Context,
    detailsData: DivData,
    divConfig: DivConfiguration
) : FrameLayout(context) {
    private var divView: Div2View? = null

    init {
        initializeView(detailsData, divConfig)
    }

    private fun initializeView(
        detailsData: DivData,
        config: DivConfiguration
    ) {
        try {

            val themedContext = ContextThemeWrapper(
                context,
                context.applicationInfo.theme
            )

            divView = Div2View(
                Div2Context(
                    baseContext = themedContext,
                    configuration = config
                )
            )

            divView?.setData(detailsData, DivDataTag("SourceSync-ActivationDetails"))

            // Add content container to frame layout
            divView?.let { addView(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeView", e)
        }
    }

    /**
     * Safely cleanup all RecyclerViews in the view hierarchy
     */
    private fun clearRecyclerViews(view: View) {
        try {
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val child = view.getChildAt(i)
                    if (child is RecyclerView) {
                        try {
                            child.adapter = null
                            child.layoutManager = null
                        } catch (e: Exception) {
                            Log.w(TAG, "Error clearing RecyclerView: ${e.message}")
                        }
                    }
                    if (child is ViewGroup) {
                        clearRecyclerViews(child)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error in clearRecyclerViews: ${e.message}")
        }
    }

    /**
     * Safe cleanup method
     */
    fun safeCleanup() {
        try {
            divView?.let { view ->
                // Clear all RecyclerViews first
                clearRecyclerViews(view)
                // Then cleanup
                view.cleanup()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error during safe cleanup: ${e.message}")
        } finally {
            divView = null
        }
    }

    override fun onDetachedFromWindow() {
        safeCleanup()
        super.onDetachedFromWindow()
    }

    companion object {
        private const val TAG = "ActivationDetails"
    }
}