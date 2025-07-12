package io.sourcesync.sdk.ui.divkit

import android.annotation.SuppressLint
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.yandex.div.DivDataTag
import com.yandex.div.core.Div2Context
import com.yandex.div.core.DivConfiguration
import com.yandex.div.core.view2.Div2View
import com.yandex.div2.DivData

@SuppressLint("ViewConstructor")
class ActivationPreview
    (context: Context, previewData: DivData, config: DivConfiguration) : FrameLayout(context) {
    private lateinit var divView: Div2View
    private var onContentClickListener: (() -> Unit)? = null

    init {
        initializeView(previewData, config)
    }

    /**
     * Initializes the DivView with the provided data and configuration
     * @param previewData The DivData to display
     * @param config The DivConfiguration to use
     */

    private fun initializeView(previewData: DivData, config: DivConfiguration) {

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
        
        divView.setData(previewData, DivDataTag("SourceSync-ActivationPreview"))
    
        
        // Set up intelligent touch handling
        setupTouchHandling()

        addView(divView)
    }
    
    private fun setupTouchHandling() {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Check if touch hits actual DivKit content
                    val hitView = findViewAtPoint(divView, event.x, event.y)
                    val hasContent = hasContentAtPoint(hitView, event.x, event.y)
                    
                    if (hasContent) {
                        // Trigger content click callback
                        onContentClickListener?.invoke()
                        true
                    } else {
                        // Pass through to underlying views
                        false
                    }
                }
                else -> false
            }
        }
    }
    
    private fun findViewAtPoint(view: View, x: Float, y: Float): View? {
        if (!view.isShown || !isPointInView(view, x, y)) {
            return null
        }
        
        if (view is ViewGroup) {
            // Check children in reverse order (top to bottom)
            for (i in view.childCount - 1 downTo 0) {
                val child = view.getChildAt(i)
                val childX = x - child.left
                val childY = y - child.top
                val hitChild = findViewAtPoint(child, childX, childY)
                if (hitChild != null) {
                    return hitChild
                }
            }
        }
        
        return view
    }
    
    private fun isPointInView(view: View, x: Float, y: Float): Boolean {
        return x >= 0 && x < view.width && y >= 0 && y < view.height
    }
    
    private fun hasContentAtPoint(hitView: View?, x: Float, y: Float): Boolean {
        if (hitView == null || hitView == this || hitView == divView) {
            return false
        }
        
        // If we hit a child view of DivKit, check if it's actually interactive or visible content
        return when {
            // If the view is clickable, it's interactive content
            hitView.isClickable -> true
            
            // If the view has a non-transparent background, it's likely content
            hitView.background != null -> true
            
            // If it's a text view or image view, it's likely content
            hitView.javaClass.simpleName.contains("Text", ignoreCase = true) -> true
            hitView.javaClass.simpleName.contains("Image", ignoreCase = true) -> true
            hitView.javaClass.simpleName.contains("Button", ignoreCase = true) -> true
            
            // If the view has specific DivKit class names that indicate content
            hitView.javaClass.name.contains("div", ignoreCase = true) && 
            !hitView.javaClass.name.contains("container", ignoreCase = true) -> true
            
            else -> false
        }
    }
    
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let { event ->
            val hitView = findViewAtPoint(divView, event.x, event.y)
            val hasContent = hasContentAtPoint(hitView, event.x, event.y)
            
            // Only intercept if there's actual content at the touch point
            return hasContent
        }
        return super.onInterceptTouchEvent(ev)
    }
    
    fun setOnContentClickListener(listener: () -> Unit) {
        this.onContentClickListener = listener
    }
}