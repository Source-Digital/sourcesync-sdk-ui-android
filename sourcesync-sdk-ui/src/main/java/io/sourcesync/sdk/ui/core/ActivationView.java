package io.sourcesync.sdk.ui.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.sourcesync.sdk.ui.utils.TemplateStorage;
import pl.droidsonroids.gif.GifDrawable;

/**
 * A view representing an activation component with preview and detail views.
 */
public class ActivationView extends FrameLayout {
    private static final String TAG = "SDK:ActivationView";

    private ActivationPreview previewView;
    private ActivationDetail detailView;
    private Runnable onPreviewClickHandler;
    private CircularProgressManager progressManager;

    // Animation configuration
    private static final long ANIMATION_DURATION = 1500; // 1 second
    private long progressDuration = 10000; // 10 seconds

    private static final float DETAIL_WIDTH_PERCENTAGE = 0.55f; // 55% of screen width in landscape

    // Default templates
    private JSONObject defaultPreviewTemplate;
    private JSONObject defaultDetailTemplate;

    private final Handler handler = new Handler();

    /**
     * Constructor for the ActivationView
     * @param context The context
     */
    public ActivationView(Context context) {
        super(context);
        init();
    }

    private void init() {
        // Load default templates
        defaultPreviewTemplate = TemplateStorage.getDefaultPreviewTemplate();
        defaultDetailTemplate = TemplateStorage.getDefaultDetailTemplate();

        // Initialize progress manager
        progressManager = new CircularProgressManager(this);
        progressManager.setProgressCompleteListener(() -> {
            if (previewView != null) {
                previewView.setVisibility(GONE);
            }
            if (detailView != null) {
                detailView.setVisibility(GONE);
            }
        });
    }

    /**
     * Shows the preview view with given data.
     *
     * @param previewData JSON data for preview.
     * @param showProgress Whether to show the progress indicator.
     * @param progressDuration Duration of the progress countdown in milliseconds.
     * @param progressImage Optional image to display in the center of the progress circle.
     * @param onClickListener Listener to execute on click.
     */
    public void showPreview(
            JSONObject previewData,
            boolean showProgress,
            long progressDuration,
            @Nullable GifDrawable progressImage,
            OnClickListener onClickListener) throws JSONException {

        if (previewView != null) {
            removeView(previewView);
        }

        // Remove existing progress
        progressManager.removeProgressIndicator();

        // Update progress duration
        this.progressDuration = progressDuration;

        this.onPreviewClickHandler = () -> onClickListener.onClick(this);

        // Use default template if none provided
        JSONObject templateToUse = previewData;
        if (previewData.has("template") && previewData.getJSONArray("template").length() == 0) {
            templateToUse = defaultPreviewTemplate;
            progressManager.setupCircularProgress(progressImage);
        }

        try {
            previewView = new ActivationPreview(getContext(), templateToUse);
            previewView.setAlpha(0f); // Start fully transparent
            previewView.setOnClickListener(v -> {
                if (onPreviewClickHandler != null) {
                    onPreviewClickHandler.run();
                }
            });

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            // Position at the right side, either next to progress or at the edge
            params.gravity = Gravity.RIGHT | Gravity.TOP;

            // Set right margin to 20dp
            int rightMargin = (int) (50 * getResources().getDisplayMetrics().density);
            params.rightMargin = rightMargin;

            // Set top margin to align with progress if present, otherwise use default top margin
            params.topMargin = (int) (20 * getResources().getDisplayMetrics().density);

            // Add view with initial params
            addView(previewView, params);

            // Adjust position after layout is complete
            post(() -> {
                FrameLayout progressContainer = progressManager.getProgressContainerView();
                if (progressContainer != null) {
                    // Position preview to the left of progress circle
                    // Calculate the preview's right margin to position it to the left of progress
                    int newRightMargin = rightMargin + progressContainer.getWidth();

                    // Update the layout params
                    FrameLayout.LayoutParams updatedParams = (FrameLayout.LayoutParams) previewView.getLayoutParams();
                    updatedParams.rightMargin = newRightMargin;

                    // Center vertically with progress
                    int progressCenterY = (int) progressContainer.getY() + progressContainer.getHeight() / 2;
                    int previewHeight = previewView.getHeight();
                    int newTopMargin = progressCenterY - previewHeight / 2;
                    updatedParams.topMargin = Math.max(0, newTopMargin);

                    previewView.setLayoutParams(updatedParams);

                    Log.d(TAG, "Positioned preview to the left of progress: right margin = " + newRightMargin);
                } else {
                    // Position at the right edge if no progress
                    Log.d(TAG, "Positioned preview at right edge, no progress indicator");
                }
            });

            // Fade-in animation
            previewView.animate()
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            // Start progress animation if needed
                            if (showProgress && progressManager.getProgressContainerView() != null) {
                                progressManager.startProgressAnimation(progressDuration);
                            }
                        }
                    })
                    .start();

        } catch (Exception e) {
            Log.e(TAG, "Error creating preview view: " + e.getMessage());
        }
    }

    /**
     * Shows the preview with default settings
     * @param previewData The preview template data
     * @param onClickListener Click listener
     */
    public void showPreview(JSONObject previewData, OnClickListener onClickListener) throws JSONException {
        showPreview(previewData, true, 10000, null, onClickListener);
    }

    /**
     * Shows the detail view with given data.
     *
     * @param detailData JSON data for detail.
     * @param onClose Runnable to execute on close.
     */
    public void showDetail(JSONObject detailData, Runnable onClose) {
        if (detailView != null) {
            removeView(detailView);
        }

        // Hide preview with fade-out
        if (previewView != null) {
            previewView.animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            previewView.setVisibility(GONE);
                            progressManager.setVisibility(false);
                        }
                    })
                    .start();
        }

        // Use default template if none provided
        JSONObject detailsTemplateToUse = detailData;
        try {
            if (detailData.has("template") && detailData.getJSONArray("template").length() == 0) {
                detailsTemplateToUse = defaultDetailTemplate;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            JSONArray templateArray = detailsTemplateToUse.getJSONArray("template");
            detailView = new ActivationDetail(getContext(), templateArray, onClose);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    (int) (getWidth() * DETAIL_WIDTH_PERCENTAGE),
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            params.gravity = android.view.Gravity.END;

            detailView.setAlpha(0f);
            addView(detailView, params);

            // Animate the detail view appearance
            detailView.animate()
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION)
                    .start();

        } catch (JSONException e) {
            Log.e(TAG, "Error creating detail view: " + e.getMessage());
        }
    }

    /**
     * Hides the detail view and restores preview.
     */
    public void hideDetail() {
        if (detailView != null) {
//            detailView.animate()
//                    .alpha(0f)
//                    .setDuration(ANIMATION_DURATION)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
            removeView(detailView);
            detailView = null;
//
//                            // Restore preview
//                            if (previewView != null) {
//                                previewView.setVisibility(VISIBLE);
//                                progressManager.setVisibility(true);
//                                previewView.animate()
//                                        .alpha(1f)
//                                        .setDuration(ANIMATION_DURATION)
//                                        .start();
//                            }
//                        }
//                    })
//                    .start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // Clean up resources
        progressManager.removeProgressIndicator();
        handler.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }
}