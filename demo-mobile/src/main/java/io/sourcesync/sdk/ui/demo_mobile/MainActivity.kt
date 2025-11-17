package io.sourcesync.sdk.ui.demo_mobile

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * Main demo activity showcasing SourceSync SDK UI integration
 * Provides simple navigation between main menu and activation demo
 */
class MainActivity : AppCompatActivity() {

    private var activationViewLayout: ActivationViewLayout? = null  // Demo layout instance
    private var container: FrameLayout? = null                      // Root container view

    /**
     * Initializes activity with root container and displays main menu
     * @param savedInstanceState Saved instance state for activity restoration
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create container
        container = FrameLayout(this)
        setContentView(container)

        showMainButtons()
    }

    /**
     * Displays main menu with demo launch button
     * Clears container and shows navigation options
     */
    private fun showMainButtons() {
        container?.removeAllViews()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val launchButton = Button(this).apply {
            text = "Show Activation View Layout"
            setOnClickListener {
                showActivationViewLayout()
            }
        }

        layout.addView(launchButton)
        container?.addView(layout)
    }

    /**
     * Launches activation demo layout and starts timer
     * Replaces main menu with ActivationViewLayout instance
     */
    private fun showActivationViewLayout() {
        container?.removeAllViews()

        activationViewLayout = ActivationViewLayout(this).apply {
            onBackClickListener = {
                hideActivationViewLayout()
            }
        }

        container?.addView(activationViewLayout)
        activationViewLayout?.startTimer()
    }

    /**
     * Hides activation demo and returns to main menu
     * Stops timer and cleans up demo layout resources
     */
    private fun hideActivationViewLayout() {
        activationViewLayout?.stopTimer()
        activationViewLayout = null
        showMainButtons()
    }

    /**
     * Cleans up timer resources when activity is destroyed
     * Ensures proper resource cleanup to prevent memory leaks
     */
    override fun onDestroy() {
        super.onDestroy()
        activationViewLayout?.stopTimer()
    }
}