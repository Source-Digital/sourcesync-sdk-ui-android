package io.sourcesync.sdk.ui.demo_mobile

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import io.sourcesync.sdk.ui.divkit.ActivationView
import org.json.JSONException

class ActivationViewLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var activationView: ActivationView? = null
    private var timerText: TextView? = null
    private var backButton: ImageView? = null
    private var countDownTimer: CountDownTimer? = null
    private var isActivationViewSetup = false

    // Callback for back button click
    var onBackClickListener: (() -> Unit)? = null

    init {
        setupLayout()
    }


    private fun setupLayout() {
        Log.d("ActivationViewLayout", "Setting up layout...")

        setBackgroundColor(resources.getColor(R.color.gray_400,null))
        setupBackButton()
        setupTimer()
    }

    private fun setupBackButton() {
        backButton = ImageView(context).apply {
            id = View.generateViewId()
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

    private fun setupTimer() {
        timerText = TextView(context).apply {
            id = View.generateViewId()
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
                    setupActivationView()
                    isActivationViewSetup = true
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

    private fun setupActivationView() {
        Log.d("ActivationViewLayout", "Setting up activation view...")

        try {
            activationView = ActivationView(context)

            // Create layout parameters for top-right positioning
            val layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(ALIGN_PARENT_TOP)
                addRule(ALIGN_PARENT_END)
                topMargin = 16.dpToPx()
                rightMargin = 16.dpToPx()
            }

            addView(activationView, layoutParams)

            val previewTemplate = TemplateLoader.loadTemplate(context, "div_preview.json")
            val detailsTemplate = TemplateLoader.loadTemplate(context, "div_details.json")

            activationView?.showPreview(previewTemplate) { _: View? ->
                Log.d("ActivationViewLayout", "Preview clicked, showing details")
                activationView?.showDetail(
                    detailsTemplate,
                    widthPercentage = 0.55f,
                    onActionTriggered = {

                    }, onClose = {
                        Log.d("ActivationViewLayout", "Details action triggered, hiding details")
                        activationView?.hideDetails()
                    })
            }

            Log.d("ActivationViewLayout", "Activation view setup completed")

        } catch (e: JSONException) {
            Log.e("ActivationViewLayout", "Error setting up activation view", e)
            throw RuntimeException(e)
        }
    }

    fun stopTimer() {
        Log.d("ActivationViewLayout", "Stopping timer...")
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun hideActivationView() {
        Log.d("ActivationViewLayout", "Hiding activation view...")
        activationView?.let {
            removeView(it)
            Log.d("ActivationViewLayout", "Activation view removed from layout")
        }
        activationView = null
        isActivationViewSetup = false
    }

    fun resetTimer() {
        Log.d("ActivationViewLayout", "Resetting timer...")
        stopTimer()
        isActivationViewSetup = false
        hideActivationView()
        timerText?.text = "Timer: 0s"
    }

    fun restartTimer() {
        Log.d("ActivationViewLayout", "Restarting timer...")
        resetTimer()
        startTimer()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d("ActivationViewLayout", "View detached, cleaning up timer")
        stopTimer()
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}