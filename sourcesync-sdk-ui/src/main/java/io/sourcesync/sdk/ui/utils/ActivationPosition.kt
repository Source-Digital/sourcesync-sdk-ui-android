package io.sourcesync.sdk.ui.utils

data class ActivationPosition(
    val screenWidth: Int,
    val screenHeight: Int,
    val activationPosition: Alignment? = null
)

data class Alignment(
    val activationHorizontalAlignment: ActivationHorizontalAlignment? = null,
    val activationVerticalAlignment: ActivationVerticalAlignment? = null
)

enum class ActivationHorizontalAlignment {
    LEFT,
    CENTER,
    RIGHT
}

enum class ActivationVerticalAlignment {
    TOP,
    CENTER,
    BOTTOM
}

object StringToEnum {
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
