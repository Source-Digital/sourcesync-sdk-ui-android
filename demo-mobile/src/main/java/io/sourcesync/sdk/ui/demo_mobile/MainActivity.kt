package io.sourcesync.sdk.ui.demo_mobile

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var activationViewLayout: ActivationViewLayout? = null
    private var container: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create container
        container = FrameLayout(this)
        setContentView(container)

        showMainButtons()
    }

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

    private fun hideActivationViewLayout() {
        activationViewLayout?.stopTimer()
        activationViewLayout = null
        showMainButtons()
    }

    override fun onDestroy() {
        super.onDestroy()
        activationViewLayout?.stopTimer()
    }
}