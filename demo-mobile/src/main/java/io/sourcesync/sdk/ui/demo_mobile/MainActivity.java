package io.sourcesync.sdk.ui.demo_mobile;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.sourcesync.sdk.ui.core.ActivationView;
import pl.droidsonroids.gif.GifDrawable;

public class MainActivity extends AppCompatActivity {

    ActivationView activationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create container for activation views
        FrameLayout container = new FrameLayout(this);
        setContentView(container);

        // Create activation
        activationView = new ActivationView(this);
        container.addView(activationView);

        // Load a template
        JSONObject previewTemplate = TemplateLoader.loadTemplate(this, "preview_template_5");
        JSONObject detailsTemplate = TemplateLoader.loadTemplate(this, "details_template_5");
        GifDrawable gifDrawable;
        try {
             gifDrawable = new GifDrawable(getAssets(), "activation_img.gif");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Error handling
        if (previewTemplate != null) {
            try {
                activationView.showPreview(previewTemplate, true, 20, gifDrawable,
                        v -> activationView.showDetail(detailsTemplate, () -> MainActivity.this.activationView.hideDetail()));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}