package io.sourcesync.sdk.ui.models

/**
 * Defines activation positioning within screen boundaries
 * @param screenWidth Screen width in pixels for layout calculations
 * @param screenHeight Screen height in pixels for layout calculations
 * @param activationPosition Optional alignment configuration for positioning
 */
data class ActivationPosition(
    val screenWidth: Int,
    val screenHeight: Int,
    val activationPosition: Alignment? = null
)

/**
 * Configures horizontal and vertical alignment for activation views
 * @param activationHorizontalAlignment Horizontal positioning (left, center, right)
 * @param activationVerticalAlignment Vertical positioning (top, center, bottom)
 */
data class Alignment(
    val activationHorizontalAlignment: ActivationHorizontalAlignment? = null,
    val activationVerticalAlignment: ActivationVerticalAlignment? = null
)

/**
 * Horizontal alignment options for activation positioning
 */
enum class ActivationHorizontalAlignment {
    /** Align to left edge of container */
    LEFT,
    /** Center horizontally within container */
    CENTER,
    /** Align to right edge of container */
    RIGHT
}

/**
 * Vertical alignment options for activation positioning
 */
enum class ActivationVerticalAlignment {
    /** Align to top edge of container */
    TOP,
    /** Center vertically within container */
    CENTER,
    /** Align to bottom edge of container */
    BOTTOM
}

/**
 * Utility for converting string values to alignment enums
 */
object StringToEnum {
    /**
     * Converts string to horizontal alignment enum with fallback
     * @param position String representation of horizontal alignment
     * @return ActivationHorizontalAlignment enum, defaults to RIGHT if invalid
     */

    fun getHorizontalEnum(position: String): ActivationHorizontalAlignment {
        return when (position) {
            "left" -> {
                ActivationHorizontalAlignment.LEFT
            }
            "right" -> {
                ActivationHorizontalAlignment.RIGHT
            }
            "center" -> {
                ActivationHorizontalAlignment.CENTER
            }
            else -> {
                // Default to top if not specified
                ActivationHorizontalAlignment.RIGHT
            }
        }
    }

    /**
     * Converts string to vertical alignment enum with fallback
     * @param position String representation of vertical alignment
     * @return ActivationVerticalAlignment enum, defaults to TOP if invalid
     */
    fun getVerticalEnum(position: String): ActivationVerticalAlignment {
        return when (position) {
            "top" -> {
                ActivationVerticalAlignment.TOP
            }
            "bottom" -> {
                ActivationVerticalAlignment.BOTTOM
            }
            "center" -> {
                ActivationVerticalAlignment.CENTER
            }
            else -> {
                // Default to top if not specified
                ActivationVerticalAlignment.TOP
            }
        }
    }
}
