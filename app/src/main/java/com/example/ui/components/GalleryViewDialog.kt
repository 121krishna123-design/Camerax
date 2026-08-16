package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.model.CapturedItem
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CameraBackground
import com.example.ui.theme.CameraGlass
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VivoGold
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryViewDialog(
    item: CapturedItem,
    galleryList: List<CapturedItem>,
    onItemSelect: (CapturedItem) -> Unit,
    onDelete: (CapturedItem) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showInfoSheet by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CameraBackground)
                .testTag("gallery_fullscreen_viewer")
        ) {
            // Fullscreen Photo display
            Image(
                painter = rememberAsyncImagePainter(File(item.filePath)),
                contentDescription = "Captured Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Top Bar with Back, Info, Share, Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(CameraGlass)
                    .padding(horizontal = 8.dp, vertical = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "vivo T3 5G",
                        color = VivoGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault()).format(Date(item.dateAdded)),
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Row {
                    IconButton(onClick = { showInfoSheet = true }) {
                        Icon(Icons.Default.Info, contentDescription = "EXIF Info", tint = TextPrimary)
                    }

                    IconButton(
                        onClick = {
                            try {
                                val file = File(item.filePath)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Photo"))
                            } catch (_: Exception) {}
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = TextPrimary)
                    }

                    IconButton(onClick = { onDelete(item) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed)
                    }
                }
            }

            // Bottom Thumbnail Strip
            if (galleryList.size > 1) {
                Surface(
                    color = CameraGlass,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(galleryList) { thumb ->
                            val isSelected = thumb.id == item.id
                            Image(
                                painter = rememberAsyncImagePainter(File(thumb.filePath)),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onItemSelect(thumb) },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // EXIF Info Modal Bottom Sheet
            if (showInfoSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showInfoSheet = false },
                    sheetState = rememberModalBottomSheetState(),
                    containerColor = CameraSurfaceElevated
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "IMAGE EXIF DETAILS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = VivoGold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        ExifRow(label = "Device", value = "vivo T3 5G (V2334)")
                        ExifRow(label = "Sensor", value = "Sony IMX882 (1/1.95\", OIS)")
                        ExifRow(label = "Lens & Focal", value = item.exifLens)
                        ExifRow(label = "Aperture", value = item.exifAperture)
                        ExifRow(label = "Exposure Time", value = item.exifShutter)
                        ExifRow(label = "ISO Speed", value = item.exifIso)
                        ExifRow(label = "Resolution", value = "${item.width} x ${item.height} (${(item.width * item.height / 1000000f).let { String.format(Locale.US, "%.1f MP", it) }})")
                        ExifRow(label = "Capture Mode", value = item.mode.title)
                        ExifRow(label = "Color Profile", value = item.filter.displayName)
                        ExifRow(label = "Watermark", value = if (item.watermarkApplied) "vivo T3 5G 50MP OIS" else "None")
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExifRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
