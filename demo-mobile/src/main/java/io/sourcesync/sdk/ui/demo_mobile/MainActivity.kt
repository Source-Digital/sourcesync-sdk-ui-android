package io.sourcesync.sdk.ui.demo_mobile

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import io.sourcesync.sdk.ui.divkit.ActivationView
import org.json.JSONException

class MainActivity : AppCompatActivity() {
    private var activationView: ActivationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create container for activation views
        val container = FrameLayout(this)
        setContentView(container)

        activationView = ActivationView(this)
        container.addView(activationView)

        val previewTemplate = TemplateLoader.loadTemplate(this,"div_preview.json")
        val detailsTemplate = TemplateLoader.loadTemplate(this,"div_details.json")
        try {
            activationView!!.showPreview(previewTemplate) { _: View? ->
                activationView!!.showDetail(detailsTemplate)
                { activationView!!.hideDetails() }
            }
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }
}