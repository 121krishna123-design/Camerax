package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.media.ExifInterface
import com.example.model.AspectRatioMode
import com.example.model.BokehShape
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
        
        // 3. Apply color filter
        val filteredBitmap = applyColorFilter(croppedBitmap, filter)
        
        // 4. Apply portrait bokeh if portrait mode
        val processedBitmap = if (mode == CameraMode.PORTRAIT) {
            applyPortraitBokehSimulation(filteredBitmap, portraitSettings)
        } else {
            filteredBitmap
        }
        
        // 5. Apply Vivo T3 Watermark
        val finalBitmap = if (watermarkSettings.enabled) {
            applyVivoWatermark(processedBitmap, watermarkSettings, mode)
        } else {
            processedBitmap
        }
        
        // 6. Save final image to app media folder
        val outputDir = File(context.filesDir, "vivo_photos")
        if (!outputDir.exists()) outputDir.mkdirs()
        
        val fileName = "VIVO_T3_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}_${UUID.randomUUID().toString().take(4)}.jpg"
        val outputFile = File(outputDir, fileName)
        
        FileOutputStream(outputFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        
        // Clean up temp raw file if different
        if (rawFile.exists() && rawFile.absolutePath != outputFile.absolutePath) {
            rawFile.delete()
        }

        CapturedItem(
            id = UUID.randomUUID().toString(),
            uri = outputFile.toURI().toString(),
            filePath = outputFile.absolutePath,
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

    private fun decodeRotatedBitmap(filePath: String, isFrontCamera: Boolean): Bitmap {
        val bitmap = BitmapFactory.decodeFile(filePath) ?: createFallbackBitmap()
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
                // Saturated vibrance with rich warm tones
                cm.set(floatArrayOf(
                    1.2f, 0f, 0f, 0f, 10f,
                    0f, 1.25f, 0f, 0f, 10f,
                    0f, 0f, 1.15f, 0f, 5f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.VIVO_TEXTURED -> {
                // High contrast cinematic texture
                cm.set(floatArrayOf(
                    1.3f, 0f, 0f, 0f, -15f,
                    0f, 1.2f, 0f, 0f, -15f,
                    0f, 0f, 1.1f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.CYBERPUNK -> {
                // Neon cyan and magenta punch
                cm.set(floatArrayOf(
                    1.4f, 0f, 0.2f, 0f, 20f,
                    0f, 0.9f, 0.1f, 0f, -10f,
                    0.2f, 0.1f, 1.5f, 0f, 30f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.BLACK_GOLD -> {
                // Warm amber highlights, dark slate shadows
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
                // Warm 35mm nostalgic faded shadows
                cm.set(floatArrayOf(
                    1.15f, 0.05f, 0f, 0f, 20f,
                    0.05f, 1.1f, 0f, 0f, 15f,
                    0f, 0.05f, 0.95f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.FRENCH_RETRO -> {
                // Soft pastel tones with slight desaturation
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
                // Teal shadows, Orange skin/highlights
                cm.set(floatArrayOf(
                    1.3f, -0.1f, -0.1f, 0f, 20f,
                    -0.05f, 1.1f, 0.1f, 0f, 0f,
                    -0.1f, 0.1f, 1.3f, 0f, 25f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.NOIR_BW -> {
                // Deep black and white high contrast
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
        // Soft vignette and depth focus simulation based on f-stop
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawBitmap(src, 0f, 0f, null)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val apertureFactor = (4.0f - portraitSettings.apertureFStop.coerceIn(0.95f, 4.0f)) / 3.0f
        
        if (apertureFactor > 0.1f) {
            // Draw gentle optical blur border vignette
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
                // Vivo Badge + vivo T3 5G | 50MP OIS Camera
                val primaryText = "vivo T3 5G"
                val secondaryText = if (mode == CameraMode.HIGH_RES_50MP) "50MP ULTRA HD | OIS" else "50MP OIS Camera"
                val dateText = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date())

                textPaint.textSize = 28f * scale
                subTextPaint.textSize = 18f * scale

                // Draw Vivo Logo Circle Badge
                val badgeRadius = 14f * scale
                val badgeX = padding + badgeRadius
                val badgeY = bottomY - 14f * scale
                
                // Outer gold badge ring
                paint.color = Color.parseColor("#E5A93C")
                paint.style = Paint.Style.FILL
                canvas.drawCircle(badgeX, badgeY, badgeRadius, paint)
                
                // Inner blue badge
                paint.color = Color.parseColor("#0C2040")
                canvas.drawCircle(badgeX, badgeY, badgeRadius * 0.85f, paint)

                // "v" initial inside badge
                val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#F5BA42")
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textSize = 16f * scale
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("v", badgeX, badgeY + 5.5f * scale, badgeTextPaint)

                // Main model text
                val textX = badgeX + badgeRadius + (12f * scale)
                canvas.drawText(primaryText, textX, bottomY - 14f * scale, textPaint)

                // Pipe separator and subtitle
                val oisText = " | $secondaryText"
                val primaryWidth = textPaint.measureText(primaryText)
                val goldPaint = Paint(subTextPaint).apply {
                    color = Color.parseColor("#F5BA42")
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(oisText, textX + primaryWidth, bottomY - 14f * scale, goldPaint)

                // Timestamp / Custom author on second line if enabled
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
                // Modern bottom bar style
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
                // Film border frame at bottom
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
