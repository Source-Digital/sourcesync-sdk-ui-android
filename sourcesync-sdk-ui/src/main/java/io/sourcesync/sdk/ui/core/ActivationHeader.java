package io.sourcesync.sdk.ui.core;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.sourcesync.sdk.ui.utils.LayoutUtils;

public class ActivationHeader extends FrameLayout {

    private Runnable onClose;

    // Constructor used when creating view from code
    public ActivationHeader(Context context, Runnable onClose) {
        super(context);
        this.onClose = onClose;
        initializeView();
    }

    // Constructor for inflation from XML layout
    public ActivationHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView();
    }

    // Constructor with default style attribute
    public ActivationHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView();
    }

    // Setter for onClose callback (needed for XML inflation)
    public void setOnCloseListener(Runnable onClose) {
        this.onClose = onClose;
    }

    private void initializeView() {
        // Make sure this view doesn't use any unnecessary space
        setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        ));

        // Create close button with improved styling
        ImageButton closeButton = new ImageButton(getContext());

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // This would be done in background
            handler.post(() -> {
                closeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            });
        });

        closeButton.setColorFilter(Color.WHITE); // White X for better contrast
        closeButton.setBackgroundColor(Color.argb(191, 38, 38, 38)); // Semi-transparent dark gray

        // Create shape drawable for rounded corners
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(LayoutUtils.dpToPx(getContext(), 6)); // Rounded corners
        shape.setColor(Color.argb(191, 38, 38, 38)); // Semi-transparent dark gray
        shape.setStroke(LayoutUtils.dpToPx(getContext(), 1), Color.argb(76, 255, 255, 255)); // Subtle white border

        closeButton.setBackground(shape);

        // Add shadow for better visibility
        closeButton.setElevation(LayoutUtils.dpToPx(getContext(), 2));
        closeButton.setTranslationZ(LayoutUtils.dpToPx(getContext(), 1));

        closeButton.setOnClickListener(v -> {
            if (onClose != null) {
                onClose.run();
            }
        });

        int buttonSize = LayoutUtils.dpToPx(getContext(), 28);
        LayoutParams buttonParams = new LayoutParams(buttonSize, buttonSize);

        // Position at top-right
        buttonParams.gravity = Gravity.TOP | Gravity.END;

        addView(closeButton, buttonParams);

        // Remove any padding
        closeButton.setPadding(0, 0, 0, 0);
    }
}