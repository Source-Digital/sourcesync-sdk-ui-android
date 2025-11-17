package io.sourcesync.sdk.ui.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.yandex.div.core.images.BitmapSource
import com.yandex.div.core.images.CachedBitmap
import com.yandex.div.core.images.DivImageDownloadCallback
import com.yandex.div.core.images.DivImageLoader
import com.yandex.div.core.images.LoadReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * DivKit image loader implementation using Picasso for efficient image loading and caching
 */
class PicassoDivImageLoader(
    context: Context,
    httpClientBuilder: OkHttpClient.Builder?,
) : DivImageLoader {

    /**
     * Creates image loader with default HTTP client configuration
     * @param context Android context for cache and resources
     */
    constructor(context: Context) : this(context, null)

    private val appContext = context.applicationContext
    private val picasso by lazy { createPicasso() }
    private val targets = TargetList()
    private val httpClient = (httpClientBuilder ?: OkHttpClient.Builder())
        .cache(Cache(context.cacheDir, DISK_CACHE_SIZE))
        .build()
    private val coroutineScope = (context as? LifecycleOwner)?.lifecycleScope ?: MainScope()

    val isIdle: Boolean
        get() = targets.size == 0

    /**
     * Creates configured Picasso instance with disk caching
     * @return Picasso instance with OkHttp3 downloader
     */
    private fun createPicasso(): Picasso {
        return Picasso.Builder(appContext)
            .downloader(OkHttp3Downloader(appContext, DISK_CACHE_SIZE))
            .build()
    }

    /**
     * Loads image from URL and returns bitmap via callback
     * @param imageUrl URL of image to load
     * @param callback DivImageDownloadCallback for success/error handling
     * @return LoadReference for cancelling the request
     */
    override fun loadImage(imageUrl: String, callback: DivImageDownloadCallback): LoadReference {
        val imageUri = imageUrl.toUri()
        val target = DownloadCallbackAdapter(imageUri, callback)
        targets.addTarget(target)

        picasso.load(imageUri).into(target)

        return LoadReference {
            picasso.cancelRequest(target)
            targets.removeTarget(target)
        }
    }

    /**
     * Loads image directly into ImageView with automatic sizing
     * @param imageUrl URL of image to load
     * @param imageView Target ImageView for displaying image
     * @return LoadReference for cancelling the request
     */

    override fun loadImage(imageUrl: String, imageView: ImageView): LoadReference {
        val target = ImageViewAdapter(imageView)
        targets.addTarget(target)
        picasso.load(imageUrl.toUri()).into(target)
        return LoadReference {
            picasso.cancelRequest(target)
            targets.removeTarget(target)
        }
    }

    /**
     * Downloads image as byte array with bitmap decoding
     * @param imageUrl URL of image to download
     * @param callback DivImageDownloadCallback for receiving bitmap and bytes
     * @return LoadReference for cancelling the request
     */
    override fun loadImageBytes(imageUrl: String, callback: DivImageDownloadCallback): LoadReference {
        var loadReference: LoadReference = EMPTY_LOAD_REFERENCE
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val response = runCatching {
                    val request = Request.Builder().url(imageUrl).build()
                    val call = httpClient.newCall(request)
                    loadReference = LoadReference {
                        call.cancel()
                    }
                    call.execute()
                }.getOrNull() ?: return@withContext null
                val source =
                    response.cacheResponse?.let { BitmapSource.MEMORY } ?: BitmapSource.NETWORK
                val bytes = response.body?.bytes() ?: return@withContext null
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let {
                    CachedBitmap(it, bytes, imageUrl.toUri(), source)
                }
            }?.let {
                callback.onSuccess(it)
            } ?: callback.onError()
        }

        return loadReference
    }

    private companion object {
        const val DISK_CACHE_SIZE = 16_777_216L
        val EMPTY_LOAD_REFERENCE = LoadReference { }
    }

    /**
     * Picasso Target adapter for DivImageDownloadCallback integration
     */
    private inner class DownloadCallbackAdapter(
        private val imageUri: Uri,
        private val callback: DivImageDownloadCallback
    ) : Target {

        /**
         * Handles successful bitmap loading and cache source detection
         * @param bitmap Loaded bitmap image
         * @param from Picasso load source (network, disk, memory)
         */
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            callback.onSuccess(CachedBitmap(bitmap, imageUri, BitmapSource.DISK))
            targets.removeTarget(this)
        }

        /**
         * Handles image loading failures
         * @param e Exception that occurred during loading
         * @param errorDrawable Optional error placeholder drawable
         */
        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
            callback.onError()
            targets.removeTarget(this)
        }

        /**
         * Called when loading starts, handles placeholder display
         * @param placeHolderDrawable Optional placeholder drawable
         */
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) = Unit
    }

    /**
     * Picasso Target adapter for direct ImageView loading
     */
    private inner class ImageViewAdapter(
        private val imageView: ImageView
    ) : Target {

        /**
         * Sets loaded bitmap directly on ImageView
         * @param bitmap Loaded bitmap image
         * @param from Picasso load source
         */
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            imageView.setImageBitmap(bitmap)
            targets.removeTarget(this)
        }

        /**
         * Handles image loading failures for ImageView
         * @param e Exception that occurred during loading
         * @param errorDrawable Optional error placeholder drawable
         */
        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
            targets.removeTarget(this)
        }

        /**
         * Handles placeholder display during loading
         * @param placeHolderDrawable Optional placeholder drawable
         */
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) = Unit
    }

    /**
     * Thread-safe collection for managing active Picasso targets
     */
    inner class TargetList {
        private val activeTargets = ArrayList<Target>()
        val size get() = activeTargets.size

        /**
         * Adds target to active list to prevent garbage collection
         * @param target Picasso Target to track
         */
        fun addTarget(target: Target) {
            activeTargets.add(target)
        }

        /**
         * Removes target from active list when loading completes
         * @param target Picasso Target to stop tracking
         */
        fun removeTarget(target: Target) {
            activeTargets.remove(target)
        }

        /**
         * Clears all active targets for cleanup
         */
        fun clean() {
            activeTargets.clear()
        }
    }
}