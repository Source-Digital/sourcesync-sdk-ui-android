package io.sourcesync.sdk.ui.core;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Manager class for circular progress functionality
 */
public class CircularProgressManager {
    // Constants for positioning
    private static final float PROGRESS_TOP_MARGIN = 20f; // Top padding for progress
    private static final float PROGRESS_RIGHT_MARGIN = 30f; // Right padding for progress
    private static final float PROGRESS_SIZE = 70f; // Size of circular progress
    private static final float PROGRESS_STROKE_WIDTH = 8f; // Progress stroke width

    private FrameLayout progressContainerView;
    private CircularProgressView progressView;
    private GifImageView centerImageView;
    private ValueAnimator progressAnimator;

    private final FrameLayout parentView;
    private ProgressCompleteListener progressCompleteListener;

    /**
     * Interface to listen for progress completion
     */
    public interface ProgressCompleteListener {
        void onProgressComplete();
    }

    /**
     * Constructor
     *
     * @param parentView The parent view to add the progress indicator to
     */
    public CircularProgressManager(@NonNull FrameLayout parentView) {
        this.parentView = parentView;
    }

    /**
     * Sets the listener for progress completion
     *
     * @param listener The listener
     */
    public void setProgressCompleteListener(ProgressCompleteListener listener) {
        this.progressCompleteListener = listener;
    }

    /**
     * Creates and shows a circular progress indicator
     *
     * @param centerImage Optional image to display in the center of the progress circle
     */
    public void setupCircularProgress(@Nullable GifDrawable centerImage) {
        Context context = parentView.getContext();

        // Convert dp to pixels
        float density = context.getResources().getDisplayMetrics().density;
        int progressSizePx = (int) (PROGRESS_SIZE * density);
        int progressStrokeWidthPx = (int) (PROGRESS_STROKE_WIDTH * density);

        // Create container for progress
        progressContainerView = new FrameLayout(context);
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                progressSizePx, progressSizePx);

        // Position at top right with margins
        containerParams.topMargin = (int) (PROGRESS_TOP_MARGIN * density);
        containerParams.rightMargin = (int) (PROGRESS_RIGHT_MARGIN * density);
        containerParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;

        parentView.addView(progressContainerView, containerParams);

        // Create progress view
        progressView = new CircularProgressView(context, progressStrokeWidthPx);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        progressContainerView.addView(progressView, progressParams);

        // Add center image if provided
        if (centerImage != null) {
            centerImageView = new GifImageView(context);
            centerImageView.setImageDrawable(centerImage);
            centerImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            int imageSize = (int) (progressSizePx * 0.8f);
            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                    imageSize, imageSize);
            imageParams.gravity = android.view.Gravity.CENTER;

            progressContainerView.addView(centerImageView, imageParams);
        }

    }

    /**
     * Starts the progress animation
     *
     * @param duration Duration of the progress animation in seconds
     */
    public void startProgressAnimation(long duration) {
        if (progressView == null) return;

        // Cancel any existing animator
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }

        // Reset progress
        progressView.setProgress(1.0f);

        // Create and start progress animator
        progressAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        progressAnimator.setDuration(duration * 1000);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            progressView.setProgress(progress);
        });

        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // When countdown completes
                progressContainerView.animate()
                        .alpha(0f)
                        .setDuration(100)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                removeProgressIndicator();
                                if (progressCompleteListener != null) {
                                    progressCompleteListener.onProgressComplete();
                                }
                            }
                        })
                        .start();
            }
        });

        progressAnimator.start();
    }

    /**
     * Removes the progress indicator from the parent view
     */
    public void removeProgressIndicator() {
        if (progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator = null;
        }

        if (progressContainerView != null) {
            parentView.removeView(progressContainerView);
            progressContainerView = null;
        }
        progressView = null;
        centerImageView = null;
    }

    /**
     * Shows or hides the progress indicator
     *
     * @param visible True to show, false to hide
     */
    public void setVisibility(boolean visible) {
        if (progressContainerView != null) {
            progressContainerView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Pauses the progress animation
     */
    public void pauseProgress() {
        if (progressAnimator != null) {
            progressAnimator.pause();
        }
    }

    /**
     * Resumes the progress animation
     */
    public void resumeProgress() {
        if (progressAnimator != null) {
            progressAnimator.resume();
        }
    }

    /**
     * Returns the progress container view
     *
     * @return The progress container view or null if not created
     */
    @Nullable
    public FrameLayout getProgressContainerView() {
        return progressContainerView;
    }

    /**
     * Custom view for circular progress indicator
     */
    @SuppressLint("ViewConstructor")
    public static class CircularProgressView extends View {
        private final Paint trackPaint;
        private final Paint progressPaint;
        private final RectF arcBounds = new RectF();
        private float progress = 1.0f; // 1.0 = full, 0.0 = empty
        private final float strokeWidth;

        public CircularProgressView(Context context, float strokeWidth) {
            super(context);
            this.strokeWidth = strokeWidth;

            trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            trackPaint.setColor(Color.LTGRAY);
            trackPaint.setStyle(Paint.Style.STROKE);
            trackPaint.setStrokeWidth(strokeWidth);
            trackPaint.setAlpha(75); // 30% opacity

            progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            progressPaint.setColor(Color.GREEN);
            progressPaint.setStyle(Paint.Style.STROKE);
            progressPaint.setStrokeCap(Paint.Cap.ROUND);
            progressPaint.setStrokeWidth(strokeWidth);
        }

        public void setProgress(float progress) {
            this.progress = progress;
            invalidate();
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            float width = getWidth();
            float height = getHeight();

            // Update arc bounds
            arcBounds.set(
                    strokeWidth / 2f,
                    strokeWidth / 2f,
                    width - strokeWidth / 2f,
                    height - strokeWidth / 2f
            );

            // Draw track (background circle)
            canvas.drawArc(arcBounds, 0, 360, false, trackPaint);

            // Draw progress
            float sweepAngle = progress * 360f;
            canvas.drawArc(arcBounds, -90, sweepAngle, false, progressPaint);
        }
    }
}
