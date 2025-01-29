// MainActivity.java
package io.sourcesync.sdk.demo;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.activationsdk.ActivationSDK;
import com.example.activationsdk.ActivationView;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private FrameLayout previewContainer;
    private FrameLayout detailContainer;
    private ActivationView previewView;
    private ActivationView detailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewContainer = findViewById(R.id.preview_container);
        detailContainer = findViewById(R.id.detail_container);
        Button toggleButton = findViewById(R.id.toggle_button);

        try {
            JSONObject activation = new JSONObject();
            activation.put("title", "My Awesome Title");

            // Create preview view
            previewView = ActivationSDK.preview(this, activation, previewContainer);

            // Create detail view
            detailView = ActivationSDK.detail(this, activation, detailContainer);

            toggleButton.setOnClickListener(v -> {
                if (previewContainer.getVisibility() == android.view.View.VISIBLE) {
                    previewContainer.setVisibility(android.view.View.GONE);
                    detailContainer.setVisibility(android.view.View.VISIBLE);
                } else {
                    previewContainer.setVisibility(android.view.View.VISIBLE);
                    detailContainer.setVisibility(android.view.View.GONE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
