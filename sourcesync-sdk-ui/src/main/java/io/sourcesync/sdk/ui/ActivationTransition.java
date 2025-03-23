package io.sourcesync.sdk.ui;

import android.view.View;

public interface ActivationTransition {
    void enterDetail(View previewView, View detailView, Runnable onComplete);
    void exitDetail(View detailView, View previewView, Runnable onComplete);
}
