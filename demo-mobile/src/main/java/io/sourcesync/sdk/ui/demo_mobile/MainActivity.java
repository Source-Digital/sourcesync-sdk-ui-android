package io.sourcesync.sdk.ui.demo_mobile;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

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
        JSONObject previewTemplate = TemplateLoader.loadTemplate(this, "preview_template_1");
        JSONObject detailsTemplate = TemplateLoader.loadTemplate(this, "details_template_1");
        GifDrawable gifDrawable;
        try {
             gifDrawable = new GifDrawable(getAssets(), "activation_img.gif");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Error handling
        if (previewTemplate != null) {
            activationView.showPreview(previewTemplate, true, 20, gifDrawable,
                    v -> activationView.showDetail(detailsTemplate, () -> MainActivity.this.activationView.hideDetail()));
        }
    }
}