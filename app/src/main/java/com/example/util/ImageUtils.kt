package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlin.math.roundToInt

object ImageUtils {

    const val SIGNATURE_MAX_PIXEL_WIDTH = 400
    const val SIGNATURE_MAX_PIXEL_HEIGHT = 160
    const val SIGNATURE_PDF_BOX_WIDTH = 120f
    const val SIGNATURE_PDF_BOX_HEIGHT = 40f

    /**
     * Calculates proportional dimensions that fit inside [maxWidth] x [maxHeight]
     * without stretching or distorting aspect ratio.
     */
    fun calculateFitSize(
        srcWidth: Float,
        srcHeight: Float,
        maxWidth: Float,
        maxHeight: Float,
        allowUpscale: Boolean = true
    ): Pair<Float, Float> {
        if (srcWidth <= 0f || srcHeight <= 0f || maxWidth <= 0f || maxHeight <= 0f) {
            return 0f to 0f
        }
        val rawScale = minOf(maxWidth / srcWidth, maxHeight / srcHeight)
        val scale = if (allowUpscale) rawScale else minOf(1f, rawScale)
        return (srcWidth * scale) to (srcHeight * scale)
    }

    /**
     * Computes a power-of-2 subsampling factor for memory-safe decoding.
     */
    fun calculateInSampleSize(
        srcWidth: Int,
        srcHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    /**
     * Decodes an image from [uri], fixes EXIF rotation, and downscales it proportionally
     * to fit within [maxWidth] x [maxHeight] pixels.
     */
    fun decodeSampledSignatureFromUri(
        context: Context,
        uri: Uri,
        maxWidth: Int = SIGNATURE_MAX_PIXEL_WIDTH,
        maxHeight: Int = SIGNATURE_MAX_PIXEL_HEIGHT
    ): Bitmap? {
        return try {
            val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
                readExifOrientation(stream)
            } ?: ExifInterface.ORIENTATION_NORMAL

            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return null

            val isRotated90Or270 = orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                orientation == ExifInterface.ORIENTATION_ROTATE_270 ||
                orientation == ExifInterface.ORIENTATION_TRANSPOSE ||
                orientation == ExifInterface.ORIENTATION_TRANSVERSE

            val reqW = if (isRotated90Or270) maxHeight else maxWidth
            val reqH = if (isRotated90Or270) maxWidth else maxHeight

            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, reqW, reqH)
            }
            val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return null

            val oriented = applyExifOrientation(decoded, orientation)
            downscaleToMaxPixels(oriented, maxWidth, maxHeight)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Decodes a signature image from [filePath], applies EXIF rotation if present,
     * and ensures it is no larger than [maxWidth] x [maxHeight] pixels.
     */
    fun decodeSampledSignatureFromFile(
        filePath: String,
        maxWidth: Int = SIGNATURE_MAX_PIXEL_WIDTH,
        maxHeight: Int = SIGNATURE_MAX_PIXEL_HEIGHT
    ): Bitmap? {
        return try {
            val orientation = try {
                ExifInterface(filePath).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } catch (_: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(filePath, boundsOpts)
            if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return null

            val isRotated90Or270 = orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                orientation == ExifInterface.ORIENTATION_ROTATE_270 ||
                orientation == ExifInterface.ORIENTATION_TRANSPOSE ||
                orientation == ExifInterface.ORIENTATION_TRANSVERSE

            val reqW = if (isRotated90Or270) maxHeight else maxWidth
            val reqH = if (isRotated90Or270) maxWidth else maxHeight

            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, reqW, reqH)
            }
            val decoded = BitmapFactory.decodeFile(filePath, decodeOpts) ?: return null
            val oriented = applyExifOrientation(decoded, orientation)
            downscaleToMaxPixels(oriented, maxWidth, maxHeight)
        } catch (_: Exception) {
            null
        }
    }

    private fun readExifOrientation(stream: java.io.InputStream): Int {
        return try {
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }
        val transformed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (transformed != bitmap) {
            bitmap.recycle()
        }
        return transformed
    }

    private fun downscaleToMaxPixels(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        if (bitmap.width <= maxWidth && bitmap.height <= maxHeight) {
            return bitmap
        }
        val (fitW, fitH) = calculateFitSize(
            srcWidth = bitmap.width.toFloat(),
            srcHeight = bitmap.height.toFloat(),
            maxWidth = maxWidth.toFloat(),
            maxHeight = maxHeight.toFloat(),
            allowUpscale = false
        )
        val targetW = fitW.roundToInt().coerceAtLeast(1)
        val targetH = fitH.roundToInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        if (scaled != bitmap) {
            bitmap.recycle()
        }
        return scaled
    }
}
