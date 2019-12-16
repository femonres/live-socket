package com.whitecloud.livesocket

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("showWhen")
fun showWhen(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}