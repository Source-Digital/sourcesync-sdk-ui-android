package io.sourcesync.sdk.ui;

import android.util.Log;
import android.view.View;

public class FadeTransition implements ActivationTransition {
    private static final String TAG = "SourceSync.trans.fade";
    private static final long ANIMATION_DURATION = 250L;

    @Override
    public void exitDetail(View detailView, View previewView, Runnable onComplete) {
        Log.d(TAG, String.format("Starting exitDetail - preview parent: %s, visibility: %d",
            previewView.getParent(), previewView.getVisibility()));

        // Try forcing visibility
        previewView.setAlpha(1f);
        previewView.setVisibility(View.VISIBLE);

        Log.d(TAG, "Preview forced visible, running onComplete");
        if (onComplete != null) {
            onComplete.run();
        }
    }

    @Override
    public void enterDetail(View previewView, View detailView, Runnable onComplete) {
        previewView.setVisibility(View.GONE);
        detailView.setVisibility(View.VISIBLE);
        if (onComplete != null) onComplete.run();
    }
}
