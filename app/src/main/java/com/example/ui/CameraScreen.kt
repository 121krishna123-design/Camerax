package com.example.ui

import android.Manifest
import android.content.Context
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.camera.CameraUiState
import com.example.camera.CameraViewModel
import com.example.model.AspectRatioMode
import com.example.model.CameraMode
import com.example.model.FlashMode
import com.example.ui.components.AuraLightScreen
import com.example.ui.components.CameraSettingsDialog
import com.example.ui.components.CameraTopBar
import com.example.ui.components.CaptureBottomBar
import com.example.ui.components.FilterPickerBar
import com.example.ui.components.GalleryViewDialog
import com.example.ui.components.GridAndFocusOverlay
import com.example.ui.components.ModeSelector
import com.example.ui.components.MoreModesSheet
import com.example.ui.components.PortraitControlsView
import com.example.ui.components.ProControlsView
import com.example.ui.components.WatermarkCustomizerDialog
import com.example.ui.components.ZoomSelector
import com.example.ui.theme.CameraBackground
import com.example.ui.theme.CameraGlass
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VivoGold
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var expandedFlash by remember { mutableStateOf(false) }
    var expandedRatio by remember { mutableStateOf(false) }
    var expandedTimer by remember { mutableStateOf(false) }

    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }

    // Auto-dismiss toast messages
    LaunchedEffect(uiState.toastMessage) {
        if (uiState.toastMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearToast()
        }
    }

    if (!cameraPermissionState.status.isGranted) {
        // Permission Request UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CameraBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "VIVO T3 5G CAMERA",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = VivoGold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please grant camera permission to unlock 50MP Sony IMX882 OIS photography and Super Night 2.0.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = VivoGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_grant_camera_permission")
                ) {
                    Text("Enable Camera", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        // Full Camera View
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(CameraBackground)
        ) {
            // 1. CameraX Preview View with Pinch-to-Zoom and Tap-to-Focus
            val previewRatio = when (uiState.aspectRatio) {
                AspectRatioMode.RATIO_4_3 -> 3f / 4f
                AspectRatioMode.RATIO_16_9 -> 9f / 16f
                AspectRatioMode.RATIO_1_1 -> 1f
                AspectRatioMode.FULL -> 9f / 20f
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            viewModel.setZoom(uiState.zoomRatio * zoom)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            previewViewInstance?.let { pv ->
                                viewModel.tapToFocus(offset.x, offset.y, pv)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            previewViewInstance = this
                            viewModel.bindCamera(lifecycleOwner, this)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(previewRatio)
                )

                // 2. Composition Grid Lines & Tap-to-Focus Overlay
                GridAndFocusOverlay(
                    gridType = uiState.gridType,
                    showLevelMeter = uiState.proSettings.showLevelMeter || uiState.currentMode == CameraMode.PRO,
                    tiltAngle = uiState.tiltAngle,
                    isLevel = uiState.isLevel,
                    focusPoint = uiState.focusPoint,
                    isFocusLocked = uiState.isFocusLocked,
                    exposureCompensation = uiState.exposureCompensation,
                    onExposureChange = { viewModel.setExposureCompensation(it) },
                    aiScene = uiState.aiScene
                )

                // 3. Aura Light Screen Rim (Selfie Soft Fill Light)
                if (uiState.flashMode == FlashMode.AURA_LIGHT || (uiState.isFrontCamera && uiState.flashMode != FlashMode.OFF)) {
                    AuraLightScreen(
                        auraTemp = uiState.auraTemp,
                        onTempSelect = { viewModel.setAuraTemp(it) }
                    )
                }
            }

            // 4. Top Overlay Controls Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            ) {
                CameraTopBar(
                    uiState = uiState,
                    onFlashToggle = {
                        val next = when (uiState.flashMode) {
                            FlashMode.OFF -> FlashMode.AUTO
                            FlashMode.AUTO -> FlashMode.ON
                            FlashMode.ON -> FlashMode.TORCH
                            FlashMode.TORCH -> FlashMode.OFF
                            FlashMode.AURA_LIGHT -> FlashMode.OFF
                        }
                        viewModel.setFlashMode(next)
                    },
                    onFlashSelect = {
                        viewModel.setFlashMode(it)
                        expandedFlash = false
                    },
                    onHdrToggle = {
                        val next = when (uiState.hdrMode) {
                            com.example.model.HdrMode.OFF -> com.example.model.HdrMode.AUTO
                            com.example.model.HdrMode.AUTO -> com.example.model.HdrMode.ON
                            com.example.model.HdrMode.ON -> com.example.model.HdrMode.OFF
                        }
                        viewModel.setHdrMode(next)
                    },
                    on50MpToggle = {
                        if (uiState.currentMode == CameraMode.HIGH_RES_50MP) {
                            viewModel.setMode(CameraMode.PHOTO)
                        } else {
                            viewModel.setMode(CameraMode.HIGH_RES_50MP)
                        }
                    },
                    onAspectRatioSelect = {
                        viewModel.setAspectRatio(it)
                        expandedRatio = false
                    },
                    onTimerSelect = {
                        viewModel.setTimer(it)
                        expandedTimer = false
                    },
                    onFilterClick = { viewModel.showFilterPicker(!uiState.showFilterPicker) },
                    onSettingsClick = { viewModel.showSettings(true) },
                    onToggleExpandFlash = {
                        expandedFlash = !expandedFlash
                        expandedRatio = false
                        expandedTimer = false
                    },
                    onToggleExpandRatio = {
                        expandedRatio = !expandedRatio
                        expandedFlash = false
                        expandedTimer = false
                    },
                    onToggleExpandTimer = {
                        expandedTimer = !expandedTimer
                        expandedFlash = false
                        expandedRatio = false
                    },
                    expandedFlash = expandedFlash,
                    expandedRatio = expandedRatio,
                    expandedTimer = expandedTimer
                )
            }

            // 5. Bottom Controls (Contextual Panels, Zoom, Mode Carousel, Shutter)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Filter Picker Bar
                AnimatedVisibility(
                    visible = uiState.showFilterPicker,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    FilterPickerBar(
                        currentFilter = uiState.currentFilter,
                        onFilterSelect = { viewModel.setFilter(it) },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Pro Manual Controls Bar (when in PRO mode)
                if (uiState.currentMode == CameraMode.PRO) {
                    ProControlsView(
                        proSettings = uiState.proSettings,
                        onProSettingsChange = { viewModel.setProSettings(it) },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Portrait Bokeh Controls Bar (when in PORTRAIT mode)
                if (uiState.currentMode == CameraMode.PORTRAIT) {
                    PortraitControlsView(
                        portraitSettings = uiState.portraitSettings,
                        onPortraitSettingsChange = { viewModel.setPortraitSettings(it) },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Smooth Zoom Pill Selector
                ZoomSelector(
                    currentZoom = uiState.zoomRatio,
                    onZoomChange = { viewModel.setZoom(it) },
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Mode Carousel Selector
                ModeSelector(
                    currentMode = uiState.currentMode,
                    onModeSelect = { viewModel.setMode(it) },
                    onMoreClick = { viewModel.showMoreModes(true) }
                )

                // Primary Shutter & Gallery Thumbnail Bottom Bar
                CaptureBottomBar(
                    uiState = uiState,
                    onShutterClick = {
                        if (uiState.currentMode == CameraMode.VIDEO) {
                            viewModel.toggleVideoRecording(context)
                        } else {
                            viewModel.triggerCapture(context)
                        }
                    },
                    onGalleryClick = { viewModel.openGalleryViewer() },
                    onFlipCameraClick = {
                        previewViewInstance?.let { pv ->
                            viewModel.toggleCameraFacing(lifecycleOwner, pv)
                        }
                    }
                )
            }

            // Toast / Banner notification
            AnimatedVisibility(
                visible = uiState.toastMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CameraGlass,
                    modifier = Modifier.testTag("camera_toast")
                ) {
                    Text(
                        text = uiState.toastMessage ?: "",
                        color = VivoGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            // Dialogs & Sheets
            if (uiState.showSettingsDialog) {
                CameraSettingsDialog(
                    gridType = uiState.gridType,
                    onGridTypeChange = { viewModel.setGridType(it) },
                    videoQuality = uiState.videoQuality,
                    onVideoQualityChange = { viewModel.setVideoQuality(it) },
                    onWatermarkClick = {
                        viewModel.showSettings(false)
                        viewModel.showWatermarkDialog(true)
                    },
                    onDismiss = { viewModel.showSettings(false) }
                )
            }

            if (uiState.showWatermarkDialog) {
                WatermarkCustomizerDialog(
                    currentSettings = uiState.watermarkSettings,
                    onSave = {
                        viewModel.setWatermarkSettings(it)
                        viewModel.showWatermarkDialog(false)
                    },
                    onDismiss = { viewModel.showWatermarkDialog(false) }
                )
            }

            if (uiState.showMoreModesSheet) {
                MoreModesSheet(
                    onModeSelect = { viewModel.setMode(it) },
                    onDismiss = { viewModel.showMoreModes(false) }
                )
            }

            val selectedGallery = uiState.selectedGalleryItem
            if (selectedGallery != null) {
                GalleryViewDialog(
                    item = selectedGallery,
                    galleryList = uiState.galleryList,
                    onItemSelect = { viewModel.openGalleryViewer(it) },
                    onDelete = { viewModel.deleteCapturedItem(it) },
                    onDismiss = { viewModel.closeGalleryViewer() }
                )
            }
        }
    }
}
