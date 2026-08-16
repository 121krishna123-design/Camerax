package com.example.camera

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.model.AspectRatioMode
import com.example.model.CameraMode
import com.example.model.CapturedItem
import com.example.model.FilterType
import com.example.model.PortraitSettings
import com.example.model.WatermarkSettings
import com.example.model.WatermarkStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

object PhotoProcessor {

    suspend fun processAndSaveImage(
        context: Context,
        rawFile: File,
        mode: CameraMode,
        aspectRatio: AspectRatioMode,
        filter: FilterType,
        watermarkSettings: WatermarkSettings,
        portraitSettings: PortraitSettings,
        isFrontCamera: Boolean,
        isoText: String = "ISO 100",
        shutterText: String = "1/120s"
    ): CapturedItem = withContext(Dispatchers.IO) {
        // 1. Decode bitmap with orientation
        val rawBitmap = decodeRotatedBitmap(rawFile.absolutePath, isFrontCamera)
        
        // 2. Crop to aspect ratio if needed
        val croppedBitmap = cropToAspectRatio(rawBitmap, aspectRatio)
        
        // 3. Apply Vivo Sony IMX882 High Dynamic Range & Color Enhancement
        val enhancedBitmap = applyVivoT3SensorEnhancement(croppedBitmap, mode)

        // 4. Apply user color filter
        val filteredBitmap = applyColorFilter(enhancedBitmap, filter)
        
        // 5. Apply portrait bokeh if portrait mode
        val processedBitmap = if (mode == CameraMode.PORTRAIT) {
            applyPortraitBokehSimulation(filteredBitmap, portraitSettings)
        } else {
            filteredBitmap
        }
        
        // 6. Apply Vivo T3 Watermark
        val finalBitmap = if (watermarkSettings.enabled) {
            applyVivoWatermark(processedBitmap, watermarkSettings, mode)
        } else {
            processedBitmap
        }
        
        // 7. Save to BOTH App Internal Storage AND Public DCIM/Camera (Phone Gallery)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "IMG_${timestamp}_VIVOT3_${mode.name.lowercase(Locale.US)}.jpg"
        
        // Internal cache for in-app instant preview
        val internalDir = File(context.filesDir, "vivo_photos")
        if (!internalDir.exists()) internalDir.mkdirs()
        val internalFile = File(internalDir, fileName)
        FileOutputStream(internalFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        // Public Gallery Save (MediaStore DCIM/Camera)
        val savedPublicUri = saveToPublicGallery(context, finalBitmap, fileName)
        
        // Clean up temp raw file
        if (rawFile.exists() && rawFile.absolutePath != internalFile.absolutePath) {
            rawFile.delete()
        }

        CapturedItem(
            id = UUID.randomUUID().toString(),
            uri = savedPublicUri?.toString() ?: internalFile.toURI().toString(),
            filePath = internalFile.absolutePath,
            dateAdded = System.currentTimeMillis(),
            width = finalBitmap.width,
            height = finalBitmap.height,
            mode = mode,
            filter = filter,
            watermarkApplied = watermarkSettings.enabled,
            isVideo = false,
            exifIso = isoText,
            exifShutter = shutterText,
            exifAperture = if (mode == CameraMode.PORTRAIT) "f/${String.format(Locale.US, "%.1f", portraitSettings.apertureFStop)}" else "f/1.79",
            exifLens = if (mode == CameraMode.HIGH_RES_50MP) "26mm • 50MP Sony IMX882 OIS" else "26mm (vivo T3 5G)"
        )
    }

    private fun saveToPublicGallery(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    out.flush()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }

                // Explicit MediaScanner broadcast to force Android system gallery to index immediately
                try {
                    val projection = arrayOf(MediaStore.Images.Media.DATA)
                    resolver.query(uri, projection, null, null, null)?.use { cursor ->
                        val colIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        if (cursor.moveToFirst()) {
                            val path = cursor.getString(colIndex)
                            MediaScannerConnection.scanFile(context, arrayOf(path), arrayOf("image/jpeg"), null)
                        }
                    }
                } catch (_: Exception) {}
            }
            uri
        } catch (e: Exception) {
            Log.e("PhotoProcessor", "Failed to save to MediaStore", e)
            null
        }
    }

    private fun applyVivoT3SensorEnhancement(src: Bitmap, mode: CameraMode): Bitmap {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val cm = ColorMatrix()

        when (mode) {
            CameraMode.NIGHT, CameraMode.ASTRO -> {
                // Vivo Super Night 2.0: Deep dynamic range boost and shadow lift
                val brightness = 20f
                val contrast = 1.10f
                cm.set(floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast * 1.03f, 0f, brightness + 2f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            CameraMode.HIGH_RES_50MP -> {
                // 50MP Ultra HD Mode: Pristine clarity, subtle micro-contrast
                cm.setSaturation(1.10f)
                val contrast = 1.06f
                val t = (-0.5f * contrast + 0.5f) * 255f
                cm.postConcat(ColorMatrix(floatArrayOf(
                    contrast, 0f, 0f, 0f, t + 3f,
                    0f, contrast, 0f, 0f, t + 3f,
                    0f, 0f, contrast, 0f, t + 3f,
                    0f, 0f, 0f, 1f, 0f
                )))
            }
            CameraMode.PORTRAIT -> {
                // Natural warm skin tone rendering
                cm.set(floatArrayOf(
                    1.05f, 0f, 0f, 0f, 5f,
                    0f, 1.02f, 0f, 0f, 3f,
                    0f, 0f, 0.98f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            CameraMode.DOC_SCAN -> {
                // High contrast document scanning
                cm.setSaturation(0.2f)
                val contrast = 1.35f
                val t = (-0.5f * contrast + 0.5f) * 255f
                cm.postConcat(ColorMatrix(floatArrayOf(
                    contrast, 0f, 0f, 0f, t,
                    0f, contrast, 0f, 0f, t,
                    0f, 0f, contrast, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                )))
            }
            else -> {
                // Vivo Signature Vibrant Sony IMX882 Tuning
                cm.setSaturation(1.15f)
                val contrast = 1.06f
                val t = (-0.5f * contrast + 0.5f) * 255f
                cm.postConcat(ColorMatrix(floatArrayOf(
                    contrast, 0f, 0f, 0f, t + 3f,
                    0f, contrast, 0f, 0f, t + 3f,
                    0f, 0f, contrast, 0f, t + 3f,
                    0f, 0f, 0f, 1f, 0f
                )))
            }
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return output
    }

    private fun decodeRotatedBitmap(filePath: String, isFrontCamera: Boolean): Bitmap {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inMutable = true
        }
        val bitmap = BitmapFactory.decodeFile(filePath, options) ?: createFallbackBitmap()
        var rotation = 0
        try {
            val exif = ExifInterface(filePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (_: Exception) {}

        val matrix = Matrix()
        if (rotation != 0) {
            matrix.postRotate(rotation.toFloat())
        }
        if (isFrontCamera) {
            matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }

        return if (rotation != 0 || isFrontCamera) {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    private fun cropToAspectRatio(src: Bitmap, aspectRatio: AspectRatioMode): Bitmap {
        val targetRatio = when (aspectRatio) {
            AspectRatioMode.RATIO_4_3 -> 4f / 3f
            AspectRatioMode.RATIO_16_9 -> 16f / 9f
            AspectRatioMode.RATIO_1_1 -> 1f
            AspectRatioMode.FULL -> 20f / 9f
        }
        
        val width = src.width
        val height = src.height
        val currentRatio = width.toFloat() / height.toFloat()
        
        if (kotlin.math.abs(currentRatio - targetRatio) < 0.05f) {
            return src
        }

        val newWidth: Int
        val newHeight: Int
        val startX: Int
        val startY: Int

        if (currentRatio > targetRatio) {
            // Source is wider than target
            newHeight = height
            newWidth = (height * targetRatio).toInt()
            startX = (width - newWidth) / 2
            startY = 0
        } else {
            // Source is taller than target
            newWidth = width
            newHeight = (width / targetRatio).toInt()
            startX = 0
            startY = (height - newHeight) / 2
        }

        return Bitmap.createBitmap(src, max(0, startX), max(0, startY), min(newWidth, width), min(newHeight, height))
    }

    private fun applyColorFilter(src: Bitmap, filter: FilterType): Bitmap {
        if (filter == FilterType.ORIGINAL) return src

        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cm = ColorMatrix()

        when (filter) {
            FilterType.VIVO_VIVID -> {
                cm.set(floatArrayOf(
                    1.2f, 0f, 0f, 0f, 10f,
                    0f, 1.25f, 0f, 0f, 10f,
                    0f, 0f, 1.15f, 0f, 5f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.VIVO_TEXTURED -> {
                cm.set(floatArrayOf(
                    1.3f, 0f, 0f, 0f, -15f,
                    0f, 1.2f, 0f, 0f, -15f,
                    0f, 0f, 1.1f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.CYBERPUNK -> {
                cm.set(floatArrayOf(
                    1.4f, 0f, 0.2f, 0f, 20f,
                    0f, 0.9f, 0.1f, 0f, -10f,
                    0.2f, 0.1f, 1.5f, 0f, 30f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.BLACK_GOLD -> {
                cm.setSaturation(0.2f)
                val tint = ColorMatrix(floatArrayOf(
                    1.3f, 0f, 0f, 0f, 25f,
                    0f, 1.1f, 0f, 0f, 10f,
                    0f, 0f, 0.7f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(tint)
            }
            FilterType.VINTAGE_FILM -> {
                cm.set(floatArrayOf(
                    1.15f, 0.05f, 0f, 0f, 20f,
                    0.05f, 1.1f, 0f, 0f, 15f,
                    0f, 0.05f, 0.95f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.FRENCH_RETRO -> {
                cm.setSaturation(0.85f)
                val retro = ColorMatrix(floatArrayOf(
                    1.1f, 0f, 0f, 0f, 15f,
                    0f, 1.05f, 0f, 0f, 12f,
                    0f, 0f, 0.95f, 0f, 5f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(retro)
            }
            FilterType.CINE_TEAL_ORANGE -> {
                cm.set(floatArrayOf(
                    1.3f, -0.1f, -0.1f, 0f, 20f,
                    -0.05f, 1.1f, 0.1f, 0f, 0f,
                    -0.1f, 0.1f, 1.3f, 0f, 25f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.NOIR_BW -> {
                cm.setSaturation(0f)
                val contrast = ColorMatrix(floatArrayOf(
                    1.35f, 0f, 0f, 0f, -20f,
                    0f, 1.35f, 0f, 0f, -20f,
                    0f, 0f, 1.35f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(contrast)
            }
            else -> {}
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return output
    }

    private fun applyPortraitBokehSimulation(src: Bitmap, portraitSettings: PortraitSettings): Bitmap {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawBitmap(src, 0f, 0f, null)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val apertureFactor = (4.0f - portraitSettings.apertureFStop.coerceIn(0.95f, 4.0f)) / 3.0f
        
        if (apertureFactor > 0.1f) {
            val borderAlpha = (apertureFactor * 40).toInt().coerceIn(10, 80)
            paint.color = Color.argb(borderAlpha, 0, 0, 0)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = src.width * 0.08f * apertureFactor
            canvas.drawRect(0f, 0f, src.width.toFloat(), src.height.toFloat(), paint)
        }

        return output
    }

    private fun applyVivoWatermark(
        src: Bitmap,
        watermarkSettings: WatermarkSettings,
        mode: CameraMode
    ): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(src, 0f, 0f, null)

        val scale = src.width / 1080f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.WHITE
            setShadowLayer(4f * scale, 0f, 2f * scale, Color.argb(180, 0, 0, 0))
        }

        val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = Color.argb(220, 240, 240, 240)
            setShadowLayer(3f * scale, 0f, 2f * scale, Color.argb(160, 0, 0, 0))
        }

        val padding = 36f * scale
        val bottomY = src.height - padding

        when (watermarkSettings.style) {
            WatermarkStyle.CLASSIC_VIVO -> {
                val primaryText = "vivo T3 5G"
                val secondaryText = if (mode == CameraMode.HIGH_RES_50MP) "50MP ULTRA HD | OIS" else "50MP OIS Camera"
                val dateText = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date())

                textPaint.textSize = 28f * scale
                subTextPaint.textSize = 18f * scale

                val badgeRadius = 14f * scale
                val badgeX = padding + badgeRadius
                val badgeY = bottomY - 14f * scale
                
                paint.color = Color.parseColor("#E5A93C")
                paint.style = Paint.Style.FILL
                canvas.drawCircle(badgeX, badgeY, badgeRadius, paint)
                
                paint.color = Color.parseColor("#0C2040")
                canvas.drawCircle(badgeX, badgeY, badgeRadius * 0.85f, paint)

                val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#F5BA42")
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textSize = 16f * scale
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("v", badgeX, badgeY + 5.5f * scale, badgeTextPaint)

                val textX = badgeX + badgeRadius + (12f * scale)
                canvas.drawText(primaryText, textX, bottomY - 14f * scale, textPaint)

                val oisText = " | $secondaryText"
                val primaryWidth = textPaint.measureText(primaryText)
                val goldPaint = Paint(subTextPaint).apply {
                    color = Color.parseColor("#F5BA42")
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(oisText, textX + primaryWidth, bottomY - 14f * scale, goldPaint)

                val subline = if (watermarkSettings.customAuthor.isNotBlank() && watermarkSettings.customAuthor != "Shot on vivo T3 5G") {
                    "${watermarkSettings.customAuthor}  •  $dateText"
                } else {
                    dateText
                }
                if (watermarkSettings.showDateTime) {
                    canvas.drawText(subline, textX, bottomY + 8f * scale, subTextPaint)
                }
            }

            WatermarkStyle.ZEISS_STYLE -> {
                textPaint.textSize = 24f * scale
                subTextPaint.textSize = 16f * scale
                
                val dateText = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
                val mainTitle = "VIVO T3 5G  •  50MP SONY IMX882 OIS"
                canvas.drawText(mainTitle, padding, bottomY - 10f * scale, textPaint)
                if (watermarkSettings.showDateTime) {
                    canvas.drawText(dateText, padding, bottomY + 12f * scale, subTextPaint)
                }
            }

            WatermarkStyle.MINIMALIST -> {
                textPaint.textSize = 22f * scale
                canvas.drawText("vivo T3 5G | OIS", padding, bottomY, textPaint)
            }

            WatermarkStyle.FILM_BORDER -> {
                paint.color = Color.argb(160, 15, 17, 22)
                canvas.drawRect(0f, src.height - (60f * scale), src.width.toFloat(), src.height.toFloat(), paint)
                
                textPaint.textSize = 20f * scale
                textPaint.color = Color.parseColor("#E5A93C")
                canvas.drawText("VIVO T3 5G 50MP FILM SIMULATION", padding, bottomY - 6f * scale, textPaint)
            }

            WatermarkStyle.CUSTOM_AUTHOR -> {
                textPaint.textSize = 26f * scale
                val author = watermarkSettings.customAuthor.ifBlank { "Shot on vivo T3 5G" }
                canvas.drawText(author, padding, bottomY, textPaint)
            }
        }

        return result
    }

    private fun createFallbackBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.DKGRAY)
        return bmp
    }
}
