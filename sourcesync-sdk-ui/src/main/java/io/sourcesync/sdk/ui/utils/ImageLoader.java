package io.sourcesync.sdk.ui.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private final HandlerThread handlerThread;
    private final Handler backgroundHandler;
    private final Handler mainHandler;
    private final AtomicBoolean isActive = new AtomicBoolean(true);

    public ImageLoader() {
        handlerThread = new HandlerThread("ImageLoaderThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void loadImage(String urlString, ImageView imageView, boolean preserveAspectRatio) {
        if (urlString == null || urlString.isEmpty()) {
            Log.e(TAG, "Invalid image URL: " + urlString);
            imageView.setBackgroundColor(Color.GRAY);
            return;
        }

        // Create weak references to prevent memory leaks
        final WeakReference<ImageView> imageViewRef = new WeakReference<>(imageView);
        final WeakReference<ViewGroup> containerRef = new WeakReference<>((ViewGroup) imageView.getParent());

        // Set a tag to identify this image view for this specific request
        imageView.setTag(urlString);

        // Start the download in a background thread
        backgroundHandler.post(() -> {
            if (!isActive.get()) return;

            try {
                URL url = new URL(urlString);
                InputStream inputStream = url.openStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                if (!isActive.get()) return;

                mainHandler.post(() -> {
                    // Get the image view from the weak reference
                    ImageView view = imageViewRef.get();
                    ViewGroup container = containerRef.get();

                    // Make sure the image view is still valid and matches our original request
                    if (view != null && urlString.equals(view.getTag())) {
                        view.setImageBitmap(bitmap);
                        view.setBackgroundColor(Color.TRANSPARENT);

                        // Apply aspect ratio if needed and if image size is valid
                        if (preserveAspectRatio && container != null && bitmap != null && bitmap.getWidth() > 0) {
                            // Calculate aspect ratio
                            final float aspectRatio = (float) bitmap.getHeight() / bitmap.getWidth();

                            // If the container already has layout, apply constraint now
                            if (container.getWidth() > 0) {
                                applyAspectRatio(container, aspectRatio);
                            } else {
                                // Otherwise wait for layout to happen
                                container.getViewTreeObserver().addOnGlobalLayoutListener(
                                        new ViewTreeObserver.OnGlobalLayoutListener() {
                                            @Override
                                            public void onGlobalLayout() {
                                                // Remove the listener to prevent multiple calls
                                                container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                                applyAspectRatio(container, aspectRatio);
                                            }
                                        });
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading image: " + urlString, e);

                if (!isActive.get()) return;

                mainHandler.post(() -> {
                    ImageView view = imageViewRef.get();
                    if (view != null && urlString.equals(view.getTag())) {
                        view.setBackgroundColor(Color.GRAY);
                    }
                });
            }
        });
    }

    // Helper method to apply aspect ratio to a container
    private void applyAspectRatio(ViewGroup container, float aspectRatio) {
        ViewGroup.LayoutParams params = container.getLayoutParams();
        if (params != null) {
            // Calculate the height based on the container width and aspect ratio
            int calculatedHeight = Math.round(container.getWidth() * aspectRatio);

            // Only change the height if it's different (to avoid layout loops)
            if (params.height != calculatedHeight) {
                params.height = calculatedHeight;
                container.setLayoutParams(params);

                // Request a layout update
                if (container.getParent() instanceof View) {
                    ((View) container.getParent()).requestLayout();
                }
            }
        }
    }

    public void shutdown() {
        isActive.set(false);
        handlerThread.quitSafely();
    }
}