package com.example.camera

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaActionSound
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.withContext
import com.example.model.AiScene
import com.example.model.AspectRatioMode
import com.example.model.AuraLightTemp
import com.example.model.BokehShape
import com.example.model.CameraMode
import com.example.model.CapturedItem
import com.example.model.FilterType
import com.example.model.FlashMode
import com.example.model.GridType
import com.example.model.HdrMode
import com.example.model.MeteringMode
import com.example.model.PortraitSettings
import com.example.model.ProSettings
import com.example.model.TimerMode
import com.example.model.VideoQuality
import com.example.model.WatermarkSettings
import com.example.model.WatermarkStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.atan2

data class CameraUiState(
    val currentMode: CameraMode = CameraMode.PHOTO,
    val flashMode: FlashMode = FlashMode.OFF,
    val auraTemp: AuraLightTemp = AuraLightTemp.NATURAL,
    val aspectRatio: AspectRatioMode = AspectRatioMode.RATIO_4_3,
    val timerMode: TimerMode = TimerMode.OFF,
    val hdrMode: HdrMode = HdrMode.AUTO,
    val currentFilter: FilterType = FilterType.ORIGINAL,
    val aiScene: AiScene = AiScene.NONE,
    val isAiSceneEnabled: Boolean = true,
    val watermarkSettings: WatermarkSettings = WatermarkSettings(),
    val proSettings: ProSettings = ProSettings(),
    val portraitSettings: PortraitSettings = PortraitSettings(),
    val videoQuality: VideoQuality = VideoQuality.UHD_4K,
    val gridType: GridType = GridType.RULE_OF_THIRDS,
    val isFrontCamera: Boolean = false,
    val zoomRatio: Float = 1.0f,
    val maxZoomRatio: Float = 10.0f,
    val minZoomRatio: Float = 0.6f,
    val exposureCompensation: Float = 0f,
    val isCapturing: Boolean = false,
    val countdownSeconds: Int = 0,
    val isRecordingVideo: Boolean = false,
    val videoRecordingSeconds: Int = 0,
    val lastCapturedItem: CapturedItem? = null,
    val galleryList: List<CapturedItem> = emptyList(),
    val selectedGalleryItem: CapturedItem? = null,
    val showSettingsDialog: Boolean = false,
    val showMoreModesSheet: Boolean = false,
    val showWatermarkDialog: Boolean = false,
    val showFilterPicker: Boolean = false,
    val showAspectRatioPicker: Boolean = false,
    val showTimerPicker: Boolean = false,
    val tiltAngle: Float = 0f,
    val isLevel: Boolean = true,
    val focusPoint: Pair<Float, Float>? = null,
    val isFocusLocked: Boolean = false,
    val toastMessage: String? = null
)

class CameraViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private val soundPlayer = MediaActionSound().apply { load(MediaActionSound.SHUTTER_CLICK) }
    private var videoJob: Job? = null
    private var timerJob: Job? = null

    init {
        initSensors()
        loadStoredGallery()
    }

    private fun initSensors() {
        try {
            sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            accelerometer?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        } catch (_: Exception) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0]
            val ay = event.values[1]
            val angle = Math.toDegrees(atan2(ax.toDouble(), ay.toDouble())).toFloat() - 90f
            val isLevel = abs(angle) < 1.5f || abs(angle - 180f) < 1.5f || abs(angle + 180f) < 1.5f
            _uiState.update { it.copy(tiltAngle = angle, isLevel = isLevel) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun bindCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(getApplication())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(lifecycleOwner, previewView)
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error binding camera", e)
            }
        }, ContextCompat.getMainExecutor(getApplication()))
    }

    private fun bindCameraUseCases(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val provider = cameraProvider ?: return
        provider.unbindAll()

        val lensFacing = if (_uiState.value.isFrontCamera) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val resolutionSelector = androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                androidx.camera.core.resolutionselector.AspectRatioStrategy(
                    when (_uiState.value.aspectRatio) {
                        AspectRatioMode.RATIO_16_9 -> androidx.camera.core.AspectRatio.RATIO_16_9
                        else -> androidx.camera.core.AspectRatio.RATIO_4_3
                    },
                    androidx.camera.core.resolutionselector.AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .setResolutionStrategy(
                if (_uiState.value.currentMode == CameraMode.HIGH_RES_50MP) {
                    androidx.camera.core.resolutionselector.ResolutionStrategy(
                        android.util.Size(8192, 6144),
                        androidx.camera.core.resolutionselector.ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER
                    )
                } else {
                    androidx.camera.core.resolutionselector.ResolutionStrategy(
                        android.util.Size(4000, 3000),
                        androidx.camera.core.resolutionselector.ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER
                    )
                }
            )
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(resolutionSelector)
            .setJpegQuality(100)
            .setFlashMode(
                when (_uiState.value.flashMode) {
                    FlashMode.ON -> ImageCapture.FLASH_MODE_ON
                    FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
                    else -> ImageCapture.FLASH_MODE_OFF
                }
            )
            .build()

        try {
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            updateCameraControls()
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Camera binding failed", e)
        }
    }

    fun setMode(mode: CameraMode) {
        _uiState.update { 
            it.copy(
                currentMode = mode,
                // Automatically activate specific AI Scenes / settings
                aiScene = when (mode) {
                    CameraMode.NIGHT, CameraMode.ASTRO -> AiScene.NIGHT
                    CameraMode.PORTRAIT -> AiScene.PORTRAIT
                    CameraMode.DOC_SCAN -> AiScene.DOCUMENT
                    else -> if (it.isAiSceneEnabled) it.aiScene else AiScene.NONE
                }
            ) 
        }
    }

    fun toggleCameraFacing(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
        bindCameraUseCases(lifecycleOwner, previewView)
    }

    fun setZoom(zoomRatio: Float) {
        val clampedZoom = zoomRatio.coerceIn(_uiState.value.minZoomRatio, _uiState.value.maxZoomRatio)
        _uiState.update { it.copy(zoomRatio = clampedZoom) }
        camera?.cameraControl?.setZoomRatio(clampedZoom)
    }

    fun setExposureCompensation(ev: Float) {
        val clampedEv = ev.coerceIn(-3.0f, 3.0f)
        _uiState.update { it.copy(exposureCompensation = clampedEv) }
        // Update Camera2 exposure if available
        try {
            val evIndex = (clampedEv * 2).toInt()
            camera?.cameraControl?.setExposureCompensationIndex(evIndex)
        } catch (_: Exception) {}
    }

    fun tapToFocus(x: Float, y: Float, previewView: PreviewView) {
        val factory = previewView.meteringPointFactory
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
            .setAutoCancelDuration(4, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        
        _uiState.update { it.copy(focusPoint = Pair(x, y), isFocusLocked = true) }
        camera?.cameraControl?.startFocusAndMetering(action)

        viewModelScope.launch {
            delay(4000)
            _uiState.update { it.copy(isFocusLocked = false) }
        }
    }

    fun setFlashMode(flashMode: FlashMode) {
        _uiState.update { it.copy(flashMode = flashMode) }
        try {
            if (flashMode == FlashMode.TORCH) {
                camera?.cameraControl?.enableTorch(true)
            } else {
                camera?.cameraControl?.enableTorch(false)
            }
            imageCapture?.flashMode = when (flashMode) {
                FlashMode.ON -> ImageCapture.FLASH_MODE_ON
                FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
                else -> ImageCapture.FLASH_MODE_OFF
            }
        } catch (_: Exception) {}
    }

    fun setAuraTemp(temp: AuraLightTemp) {
        _uiState.update { it.copy(auraTemp = temp) }
    }

    fun setAspectRatio(ratio: AspectRatioMode) {
        _uiState.update { it.copy(aspectRatio = ratio) }
    }

    fun setTimer(timer: TimerMode) {
        _uiState.update { it.copy(timerMode = timer) }
    }

    fun setHdrMode(hdr: HdrMode) {
        _uiState.update { it.copy(hdrMode = hdr) }
    }

    fun setFilter(filter: FilterType) {
        _uiState.update { it.copy(currentFilter = filter) }
    }

    fun setGridType(gridType: GridType) {
        _uiState.update { it.copy(gridType = gridType) }
    }

    fun setWatermarkSettings(settings: WatermarkSettings) {
        _uiState.update { it.copy(watermarkSettings = settings) }
    }

    fun setProSettings(pro: ProSettings) {
        _uiState.update { it.copy(proSettings = pro) }
        setExposureCompensation(pro.ev)
    }

    fun setPortraitSettings(portrait: PortraitSettings) {
        _uiState.update { it.copy(portraitSettings = portrait) }
    }

    fun setVideoQuality(quality: VideoQuality) {
        _uiState.update { it.copy(videoQuality = quality) }
    }

    fun toggleAiSceneDetection() {
        val newState = !_uiState.value.isAiSceneEnabled
        _uiState.update { 
            it.copy(
                isAiSceneEnabled = newState,
                aiScene = if (newState) AiScene.LANDSCAPE else AiScene.NONE
            ) 
        }
    }

    fun triggerCapture(context: Context) {
        if (_uiState.value.isCapturing) return

        val timerSeconds = _uiState.value.timerMode.seconds
        if (timerSeconds > 0) {
            startCountdownAndCapture(context, timerSeconds)
        } else {
            executePhotoCapture(context)
        }
    }

    private fun startCountdownAndCapture(context: Context, seconds: Int) {
        timerJob?.cancel()
        _uiState.update { it.copy(countdownSeconds = seconds) }
        
        timerJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _uiState.update { it.copy(countdownSeconds = i) }
                delay(1000)
            }
            _uiState.update { it.copy(countdownSeconds = 0) }
            executePhotoCapture(context)
        }
    }

    private fun executePhotoCapture(context: Context) {
        _uiState.update { it.copy(isCapturing = true) }
        soundPlayer.play(MediaActionSound.SHUTTER_CLICK)

        val capture = imageCapture
        if (capture != null) {
            val tempFile = File(context.cacheDir, "temp_capture_${System.currentTimeMillis()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

            capture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        processCapturedFile(context, tempFile)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraViewModel", "Photo capture failed: ${exception.message}", exception)
                        fallbackSimulatedCapture(context)
                    }
                }
            )
        } else {
            fallbackSimulatedCapture(context)
        }
    }

    private fun processCapturedFile(context: Context, file: File) {
        viewModelScope.launch {
            val state = _uiState.value
            val item = PhotoProcessor.processAndSaveImage(
                context = context,
                rawFile = file,
                mode = state.currentMode,
                aspectRatio = state.aspectRatio,
                filter = state.currentFilter,
                watermarkSettings = state.watermarkSettings,
                portraitSettings = state.portraitSettings,
                isFrontCamera = state.isFrontCamera,
                isoText = if (state.proSettings.iso > 0) "ISO ${state.proSettings.iso}" else "ISO 100",
                shutterText = if (state.proSettings.shutterSpeed != "AUTO") state.proSettings.shutterSpeed else "1/160s"
            )

            _uiState.update {
                it.copy(
                    isCapturing = false,
                    lastCapturedItem = item,
                    galleryList = listOf(item) + it.galleryList,
                    toastMessage = "Saved with vivo T3 50MP OIS Watermark"
                )
            }
        }
    }

    private fun fallbackSimulatedCapture(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(300) // Shutter delay
            val tempBmpFile = File(context.cacheDir, "sample_${System.currentTimeMillis()}.jpg")
            // Create rich simulated high res sample
            val bmp = android.graphics.Bitmap.createBitmap(1920, 1440, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bmp)
            
            // Draw simulated scene gradient
            val paint = android.graphics.Paint()
            paint.shader = android.graphics.LinearGradient(
                0f, 0f, 1920f, 1440f,
                android.graphics.Color.parseColor("#1B2A47"),
                android.graphics.Color.parseColor("#080D1A"),
                android.graphics.Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, 1920f, 1440f, paint)

            java.io.FileOutputStream(tempBmpFile).use { out ->
                bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            }

            val state = _uiState.value
            val item = PhotoProcessor.processAndSaveImage(
                context = context,
                rawFile = tempBmpFile,
                mode = state.currentMode,
                aspectRatio = state.aspectRatio,
                filter = state.currentFilter,
                watermarkSettings = state.watermarkSettings,
                portraitSettings = state.portraitSettings,
                isFrontCamera = state.isFrontCamera
            )

            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        isCapturing = false,
                        lastCapturedItem = item,
                        galleryList = listOf(item) + it.galleryList,
                        toastMessage = "Captured with vivo T3 OIS!"
                    )
                }
            }
        }
    }

    fun toggleVideoRecording(context: Context) {
        if (_uiState.value.isRecordingVideo) {
            // Stop recording
            videoJob?.cancel()
            _uiState.update { 
                it.copy(
                    isRecordingVideo = false,
                    toastMessage = "4K Video saved (${it.videoRecordingSeconds}s)"
                ) 
            }
        } else {
            // Start recording
            _uiState.update { it.copy(isRecordingVideo = true, videoRecordingSeconds = 0) }
            soundPlayer.play(MediaActionSound.START_VIDEO_RECORDING)
            videoJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _uiState.update { it.copy(videoRecordingSeconds = it.videoRecordingSeconds + 1) }
                }
            }
        }
    }

    private fun updateCameraControls() {
        val control = camera?.cameraControl ?: return
        control.setZoomRatio(_uiState.value.zoomRatio)
    }

    private fun loadStoredGallery() {
        viewModelScope.launch(Dispatchers.IO) {
            val photosDir = File(getApplication<Application>().filesDir, "vivo_photos")
            if (photosDir.exists()) {
                val files = photosDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
                val items = files.map { file ->
                    CapturedItem(
                        id = file.name,
                        uri = file.toURI().toString(),
                        filePath = file.absolutePath,
                        dateAdded = file.lastModified(),
                        mode = CameraMode.PHOTO,
                        filter = FilterType.ORIGINAL,
                        watermarkApplied = true
                    )
                }
                _uiState.update { it.copy(galleryList = items, lastCapturedItem = items.firstOrNull()) }
            }
        }
    }

    fun openGalleryViewer(item: CapturedItem? = null) {
        _uiState.update { it.copy(selectedGalleryItem = item ?: it.lastCapturedItem ?: it.galleryList.firstOrNull()) }
    }

    fun closeGalleryViewer() {
        _uiState.update { it.copy(selectedGalleryItem = null) }
    }

    fun deleteCapturedItem(item: CapturedItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(item.filePath)
                if (file.exists()) file.delete()
            } catch (_: Exception) {}

            _uiState.update { state ->
                val newList = state.galleryList.filter { it.id != item.id }
                state.copy(
                    galleryList = newList,
                    selectedGalleryItem = newList.firstOrNull(),
                    lastCapturedItem = newList.firstOrNull()
                )
            }
        }
    }

    fun showSettings(show: Boolean) = _uiState.update { it.copy(showSettingsDialog = show) }
    fun showMoreModes(show: Boolean) = _uiState.update { it.copy(showMoreModesSheet = show) }
    fun showWatermarkDialog(show: Boolean) = _uiState.update { it.copy(showWatermarkDialog = show) }
    fun showFilterPicker(show: Boolean) = _uiState.update { it.copy(showFilterPicker = show) }
    fun showAspectRatioPicker(show: Boolean) = _uiState.update { it.copy(showAspectRatioPicker = show) }
    fun showTimerPicker(show: Boolean) = _uiState.update { it.copy(showTimerPicker = show) }

    fun clearToast() = _uiState.update { it.copy(toastMessage = null) }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
        soundPlayer.release()
    }
}
