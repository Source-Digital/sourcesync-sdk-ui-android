package io.sourcesync.sdk.ui.demo_mobile

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import io.sourcesync.sdk.ui.models.ActivationHorizontalAlignment
import io.sourcesync.sdk.ui.models.ActivationVerticalAlignment
import io.sourcesync.sdk.ui.models.Alignment
import io.sourcesync.sdk.ui.view.ActivationConfig
import io.sourcesync.sdk.ui.view.ActivationView
import org.json.JSONException
import org.json.JSONObject

/**
 * Demo layout showcasing ActivationView integration with timer-based activation display
 * Demonstrates preview-to-details interaction pattern for SDK evaluation
 */

class ActivationViewLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    // UI Components
    private var activationPreview: ActivationView? = null    // Preview activation view
    private var activationDetails: ActivationView? = null    // Details activation view
    private var timerText: TextView? = null                  // Timer display
    private var backButton: ImageView? = null                // Navigation back button
    private var countDownTimer: CountDownTimer? = null       // 30-second demo timer
    private var isActivationViewSetup = false                // Setup state tracking


    /**
     * Callback for back button navigation
     */
    var onBackClickListener: (() -> Unit)? = null

    /**
     * Initializes demo layout components
     */
    init {
        setupLayout()
    }

    /**
     * Sets up demo layout with background, back button, and timer
     */
    private fun setupLayout() {
        Log.d("ActivationViewLayout", "Setting up layout...")

        setBackgroundColor(resources.getColor(R.color.gray_400,null))
        setupBackButton()
        setupTimer()
    }

    /**
     * Creates and positions back button at top-left corner
     */
    private fun setupBackButton() {
        backButton = ImageView(context).apply {
            id = generateViewId()
            setImageResource(android.R.drawable.ic_menu_revert)
            setBackgroundResource(android.R.drawable.btn_default)
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
            setOnClickListener {
                Log.d("ActivationViewLayout", "Back button clicked")
                onBackClickListener?.invoke()
            }
        }

        // Position back button at top-left
        val backButtonParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(ALIGN_PARENT_TOP)
            addRule(ALIGN_PARENT_START)
            topMargin = 16.dpToPx()
            leftMargin = 16.dpToPx()
        }

        addView(backButton, backButtonParams)
    }

    /**
     * Creates and positions timer text at top-center
     */
    private fun setupTimer() {
        timerText = TextView(context).apply {
            id = generateViewId()
            text = "Timer: 0s"
            textSize = 18f
            setTextColor(android.graphics.Color.BLACK)
        }

        // Position timer text at top-center
        val timerParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(ALIGN_PARENT_TOP)
            addRule(CENTER_HORIZONTAL)
            topMargin = 16.dpToPx()
        }

        addView(timerText, timerParams)
    }

    /**
     * Starts 30-second demo timer with activation milestones
     * Shows activation at 1s, hides at 20s, finishes at 30s
     */
    fun startTimer() {
        Log.d("ActivationViewLayout", "Starting timer...")

        countDownTimer = object : CountDownTimer(30000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val secondsElapsed = 30 - (millisUntilFinished / 1000)
                timerText?.text = "Timer: ${secondsElapsed}s"
                Log.d("ActivationViewLayout", "Timer: ${secondsElapsed}s")

                // Setup activation view at 5 seconds
                if (secondsElapsed == 1L && !isActivationViewSetup) {
                    Log.d("ActivationViewLayout", "Setting up activation view at 5 seconds")
                    setupActivationViews()
                }

                // Hide activation view at 20 seconds
                if (secondsElapsed == 20L && isActivationViewSetup) {
                    Log.d("ActivationViewLayout", "Hiding activation view at 20 seconds")
                    hideActivationView()
                }
            }

            override fun onFinish() {
                timerText?.text = "Timer: 30s - Finished"
                Log.d("ActivationViewLayout", "Timer finished")
                // Ensure activation view is hidden when timer finishes
                if (isActivationViewSetup) {
                    hideActivationView()
                }
            }
        }

        countDownTimer?.start()
    }

    /**
     * Creates preview activation view with left-top positioning
     * @param previewTemplate JSON template for preview display
     * @param onClickListener Click handler for transitioning to details
     */
    private fun initializeActivationPreview(
        previewTemplate: JSONObject,
        onClickListener: OnClickListener
    ) {
        val previewAlignment =
            Alignment(ActivationHorizontalAlignment.LEFT, ActivationVerticalAlignment.TOP)

        val previewConfig = ActivationConfig.Builder(context)
            .setPositionAlignment(previewAlignment)
            .setClickHandler {
                onClickListener.onClick(activationPreview)
                activationPreview?.hide()
            }.build()


        activationPreview =
            ActivationView.createFromJson(context, previewTemplate, previewConfig)

        addView(activationPreview, activationPreview?.getViewLayoutParams())
        isActivationViewSetup = true
    }

    /**
     * Creates details activation view with right-top positioning
     * Includes outside click handling and close functionality
     * @param detailsTemplate JSON template for details display
     */
    private fun initializeActivationDetails(detailsTemplate: JSONObject) {
        val detailsAlignment = Alignment(
            ActivationHorizontalAlignment.RIGHT,
            ActivationVerticalAlignment.TOP
        )

        val detailsConfig = ActivationConfig.Builder(context)
            .setPositionAlignment(detailsAlignment)
            .setUrlActionHandler {
                Log.d("ActivationViewLayout", "onActionTriggered!")
            }
            .setOutsideClickHandler {
                activationDetails?.hide()
            }
            .setDetailsCloseHandler {
                Log.d("ActivationViewLayout", "Details action triggered, hiding details")
                activationDetails?.hide()
            }.build()

        activationDetails = ActivationView.createFromJson(context, detailsTemplate, detailsConfig)
        addView(activationDetails, activationDetails?.getViewLayoutParams())
        Log.d("ActivationViewLayout", "Activation view setup completed")
    }

    /**
     * Loads templates and initializes activation views with interaction flow
     * Loads div_preview1.json and div_details1.json from assets
     */
    private fun setupActivationViews() {
        Log.d("ActivationViewLayout", "Setting up activation view...")
        try {
            val previewTemplate: JSONObject =
                TemplateLoader.loadTemplate(context, "div_preview1.json")

            val detailsTemplate: JSONObject =
                TemplateLoader.loadTemplate(context, "div_details1.json")

            initializeActivationPreview(previewTemplate){
                initializeActivationDetails(detailsTemplate)
            }
        } catch (e: JSONException) {
            Log.e("ActivationViewLayout", "Error setting up activation view", e)
            throw RuntimeException(e)
        }
    }

    /**
     * Cancels and clears the countdown timer
     */
    fun stopTimer() {
        Log.d("ActivationViewLayout", "Stopping timer...")
        countDownTimer?.cancel()
        countDownTimer = null
    }

    /**
     * Hides and cleans up all activation views, resets setup state
     */
    private fun hideActivationView() {
        Log.d("ActivationViewLayout", "Hiding activation view...")

        activationPreview?.cleanup()
        activationDetails?.cleanup()

        isActivationViewSetup = false
    }

    /**
     * Resets timer to initial state without starting
     */
    fun resetTimer() {
        Log.d("ActivationViewLayout", "Resetting timer...")
        stopTimer()
        isActivationViewSetup = false
        hideActivationView()
        timerText?.text = "Timer: 0s"
    }

    /**
     * Resets and immediately restarts the demo timer
     */
    fun restartTimer() {
        Log.d("ActivationViewLayout", "Restarting timer...")
        resetTimer()
        startTimer()
    }

    /**
     * Cleans up timer when view is detached from window
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d("ActivationViewLayout", "View detached, cleaning up timer")
        stopTimer()
    }

    /**
     * Converts density-independent pixels to actual pixels
     * @return Pixel value for current screen density
     */
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}