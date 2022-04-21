package com.example.legoev3android.utils

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.legoev3android.R

sealed class ConnectionState(
    val stringId: Int,
    val imageId: Int,
    val connectionButtonVisibility: Int,
    val connectionButtonText: String,
    val textSubtext: String,
    val textHeaderAppearance: Int,
    val textSubtextAppearance: Int
) {
    object Error : ConnectionState(
        R.string.controller_error,
        R.drawable.teal_error,
        View.VISIBLE,
        "CONNECT",
        "An error has occurred",
        R.style.TextTealShadow,
        R.style.TextLightShadow
    ) {
        override fun getTextHeaderLayoutParams(
            layoutParams: ViewGroup.LayoutParams
        ): ViewGroup.LayoutParams {
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            return layoutParams
        }

        override fun handleIconAnimation(
            imageViewBinding: ImageView
        ) {
            if (imageViewBinding.animation != null) {
                imageViewBinding.animation.cancel()
                imageViewBinding.animation.reset()
            }
        }

        override fun handleBackgroundAnimation(
            imageViewBinding: ImageView,
            connectedBackground: Boolean
        ) {
            imageViewBinding
                .animate()
                .alpha(if (connectedBackground) 0f else 1f)
                .duration = if (connectedBackground) 2000 else 500
        }
    }

    object Disconnected : ConnectionState(
        R.string.controller_disconnected,
        R.drawable.teal_bluetooth_off,
        View.VISIBLE,
        "CONNECT",
        "Disconnected from device",
        R.style.TextTealShadow,
        R.style.TextLightShadow
    ) {
        override fun getTextHeaderLayoutParams(
            layoutParams: ViewGroup.LayoutParams
        ): ViewGroup.LayoutParams {
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            return layoutParams
        }

        override fun handleIconAnimation(
            imageViewBinding: ImageView
        ) {
            if (imageViewBinding.animation != null) {
                imageViewBinding.animation.cancel()
                imageViewBinding.animation.reset()
            }
        }

        override fun handleBackgroundAnimation(
            imageViewBinding: ImageView,
            connectedBackground: Boolean
        ) {
            imageViewBinding
                .animate()
                .alpha(if (connectedBackground) 0f else 1f)
                .duration = if (connectedBackground) 2000 else 500
        }
    }

    object Connecting : ConnectionState(
        R.string.controller_connecting,
        R.drawable.teal_loading,
        View.GONE,
        "",
        "Attempting to connect to device",
        R.style.TextTealShadow,
        R.style.TextLightShadow
    ) {
        override fun getTextHeaderLayoutParams(
            layoutParams: ViewGroup.LayoutParams
        ): ViewGroup.LayoutParams {
            layoutParams.width = 0
            return layoutParams
        }

        override fun handleIconAnimation(
            imageViewBinding: ImageView
        ) {
                // Define animation which will be applied to the loading image view
                val rotateAnimation = RotateAnimation(
                    0f,
                    360f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                rotateAnimation.duration = 3600
                rotateAnimation.repeatCount = Animation.INFINITE
                imageViewBinding.animation = rotateAnimation
                rotateAnimation.start()
                imageViewBinding.startAnimation(imageViewBinding.animation)
        }

        override fun handleBackgroundAnimation(
            imageViewBinding: ImageView,
            connectedBackground: Boolean
        ) {
            imageViewBinding
                .animate()
                .alpha(if (connectedBackground) 0f else 1f)
                .duration = if (connectedBackground) 2000 else 500
        }

    }

    object Connected : ConnectionState(
        R.string.controller_connected,
        R.drawable.darkgrey_white_stroke_battery_100,
        View.VISIBLE,
        "DISCONNECT",
        "Connected to device",
        R.style.TextDarkShadow,
        R.style.TextDarkShadow
    ) {
        override fun getTextHeaderLayoutParams(
            layoutParams: ViewGroup.LayoutParams
        ): ViewGroup.LayoutParams {
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            return layoutParams
        }

        override fun handleIconAnimation(
            imageViewBinding: ImageView
        ) {
            if (imageViewBinding.animation != null) {
                imageViewBinding.animation.cancel()
                imageViewBinding.animation.reset()
            }
        }

        override fun handleBackgroundAnimation(
            imageViewBinding: ImageView,
            connectedBackground: Boolean
        ) {
            imageViewBinding
                .animate()
                .alpha(if (connectedBackground) 1f else 0f)
                .duration = if (connectedBackground) 500 else 2000
        }
    }

    abstract fun getTextHeaderLayoutParams(
        layoutParams: ViewGroup.LayoutParams
    ): ViewGroup.LayoutParams

    abstract fun handleIconAnimation(
        imageViewBinding: ImageView
    )

    abstract fun handleBackgroundAnimation(
        imageViewBinding: ImageView,
        connectedBackground: Boolean
    )
}