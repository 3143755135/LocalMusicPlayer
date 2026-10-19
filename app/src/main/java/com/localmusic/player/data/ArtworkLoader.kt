package com.localmusic.player.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections

/**
 * 封面提取：优先用系统的缩略图接口，取不到再回退到读取音频内嵌图片。
 * 结果按「uri + 目标尺寸」做内存缓存，失败过的 uri 也会记住，避免反复解码。
 */
class ArtworkLoader(context: Context) {

    private val appContext = context.applicationContext

    private val memoryCache = object : LruCache<String, Bitmap>(8 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }
    private val failedKeys: MutableSet<String> = Collections.synchronizedSet(mutableSetOf())

    suspend fun load(uriString: String, targetSizePx: Int): Bitmap? = withContext(Dispatchers.IO) {
        if (uriString.isEmpty() || targetSizePx <= 0) return@withContext null
        val key = "$uriString@$targetSizePx"

        memoryCache.get(key)?.let { return@withContext it }
        if (failedKeys.contains(key)) return@withContext null

        val bitmap = runCatching { extract(Uri.parse(uriString), targetSizePx) }.getOrNull()
        if (bitmap == null) {
            failedKeys.add(key)
            null
        } else {
            memoryCache.put(key, bitmap)
            bitmap
        }
    }

    private fun extract(uri: Uri, targetSizePx: Int): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                appContext.contentResolver.loadThumbnail(uri, Size(targetSizePx, targetSizePx), null)
            }.getOrNull()?.let { return it }
        }

        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(appContext, uri)
            val bytes = retriever.embeddedPicture ?: return null
            decodeSampled(bytes, targetSizePx)
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun decodeSampled(bytes: ByteArray, targetSizePx: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = inSampleSizeFor(bounds.outWidth, bounds.outHeight, targetSizePx)
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    private fun inSampleSizeFor(width: Int, height: Int, target: Int): Int {
        var sample = 1
        var longest = maxOf(width, height)
        while (longest / 2 >= target) {
            longest /= 2
            sample *= 2
        }
        return sample
    }
}
