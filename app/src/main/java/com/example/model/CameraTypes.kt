package com.example.model

enum class CameraMode(val title: String, val subtitle: String) {
    NIGHT("NIGHT", "Super Night 2.0"),
    PORTRAIT("PORTRAIT", "Bokeh Flare"),
    PHOTO("PHOTO", "AI Scene"),
    HIGH_RES_50MP("50MP", "Ultra HD OIS"),
    VIDEO("VIDEO", "4K Ultra OIS"),
    PRO("PRO", "Manual Mode"),
    DOC_SCAN("DOCS", "Document Scanner"),
    ASTRO("ASTRO", "Starry Sky"),
    SLOW_MO("SLOW-MO", "120/240 fps"),
    TIME_LAPSE("TIME-LAPSE", "Speed up"),
    DUAL_VIEW("DUAL-VIEW", "Front + Rear"),
    LIGHT_PAINTING("LIGHT", "Light Trails")
}

enum class FlashMode {
    OFF,
    AUTO,
    ON,
    TORCH,
    AURA_LIGHT
}

enum class AuraLightTemp(val label: String, val kelvin: String) {
    WARM("Warm Aura", "3200K"),
    NATURAL("Natural Aura", "4500K"),
    COOL("Cool Aura", "6000K")
}

enum class AspectRatioMode(val label: String, val ratioNumerator: Float, val ratioDenominator: Float) {
    RATIO_4_3("4:3", 4f, 3f),
    RATIO_16_9("16:9", 16f, 9f),
    RATIO_1_1("1:1", 1f, 1f),
    FULL("FULL", 20f, 9f)
}

enum class TimerMode(val seconds: Int, val label: String) {
    OFF(0, "Off"),
    S3(3, "3s"),
    S5(5, "5s"),
    S10(10, "10s")
}

enum class HdrMode {
    OFF,
    AUTO,
    ON
}

enum class AiScene(val label: String, val iconName: String) {
    NONE("", ""),
    FOOD("Food & Gourmet", "restaurant"),
    LANDSCAPE("Vibrant Landscape", "landscape"),
    PORTRAIT("Smart Portrait", "face"),
    SUNSET("Golden Sunset", "wb_sunny"),
    MACRO("Super Macro", "filter_vintage"),
    NIGHT("Low Light Night", "nightlight"),
    DOCUMENT("Document", "description"),
    FLOWER("Flower & Foliage", "local_florist"),
    BLUE_SKY("Clear Blue Sky", "cloud"),
    BACKLIGHT("HDR Backlight", "flare")
}

enum class WatermarkStyle(val displayName: String) {
    CLASSIC_VIVO("vivo T3 5G | 50MP OIS"),
    MINIMALIST("vivo T3 | OIS Camera"),
    ZEISS_STYLE("vivo T3 • 50MP IMX882"),
    FILM_BORDER("VIVO FILM 50MP"),
    CUSTOM_AUTHOR("Shot by Creator")
}

enum class FilterType(val displayName: String, val category: String) {
    ORIGINAL("Original", "Standard"),
    VIVO_VIVID("Vivo Vivid", "Color"),
    VIVO_TEXTURED("Textured", "Color"),
    CYBERPUNK("Cyberpunk", "Stylized"),
    BLACK_GOLD("Black & Gold", "Stylized"),
    VINTAGE_FILM("Vintage 35mm", "Film"),
    FRENCH_RETRO("French Retro", "Film"),
    CINE_TEAL_ORANGE("Cine Teal", "Cinema"),
    NOIR_BW("Noir B&W", "Monochrome")
}

enum class BokehShape(val label: String) {
    CIRCLE("Circle"),
    STAR("Star"),
    HEART("Heart"),
    BUTTERFLY("Butterfly"),
    HEXAGON("Hexagon")
}

enum class GridType(val label: String) {
    NONE("Off"),
    RULE_OF_THIRDS("3x3 Grid"),
    GOLDEN_RATIO("Golden Ratio"),
    SPIRAL("Fibonacci"),
    CROSSHAIR("Center Cross")
}

enum class MeteringMode(val label: String) {
    MATRIX("Matrix (Multi)"),
    CENTER("Center-weighted"),
    SPOT("Spot Metering")
}

enum class VideoQuality(val label: String, val resolutionText: String) {
    UHD_4K("4K", "3840x2160 • 30fps"),
    FHD_1080P("1080P", "1920x1080 • 60fps"),
    HD_720P("720P", "1280x720 • 30fps")
}

data class WatermarkSettings(
    val enabled: Boolean = true,
    val style: WatermarkStyle = WatermarkStyle.CLASSIC_VIVO,
    val customAuthor: String = "vivo Photography",
    val showDateTime: Boolean = true,
    val showOisBadge: Boolean = true
)

data class ProSettings(
    val iso: Int = 0, // 0 is Auto, else 50..6400
    val shutterSpeed: String = "AUTO",
    val ev: Float = 0f, // -3.0 to +3.0
    val wbKelvin: Int = 0, // 0 is Auto, else 2800..8000
    val focusDistance: Float = 0f, // 0 is AF, 0.01 to 1.0 is MF
    val isManualFocus: Boolean = false,
    val meteringMode: MeteringMode = MeteringMode.MATRIX,
    val isRaw: Boolean = false,
    val showHistogram: Boolean = true,
    val showLevelMeter: Boolean = true,
    val showFocusPeaking: Boolean = false
)

data class PortraitSettings(
    val apertureFStop: Float = 1.8f, // f/0.95 to f/16
    val bokehShape: BokehShape = BokehShape.CIRCLE,
    val beautyLevel: Float = 0.4f,
    val skinToneLevel: Float = 0.5f
)

data class CapturedItem(
    val id: String,
    val uri: String,
    val filePath: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val width: Int = 1920,
    val height: Int = 1080,
    val mode: CameraMode = CameraMode.PHOTO,
    val filter: FilterType = FilterType.ORIGINAL,
    val watermarkApplied: Boolean = true,
    val isVideo: Boolean = false,
    val durationSeconds: Int = 0,
    val exifIso: String = "ISO 50",
    val exifShutter: String = "1/250s",
    val exifAperture: String = "f/1.79",
    val exifLens: String = "26mm (50MP Sony IMX882 OIS)"
)
