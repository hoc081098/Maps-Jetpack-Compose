// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.hoc081098.mapscompose.presentation.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import timber.log.Timber

private const val TAG = "BitmapHelper"

/**
 * Customizable set of parameters for generating a [BitmapDescriptor] for use a a map marker.
 */
@Immutable
data class BitmapParameters(
  @DrawableRes val id: Int,
  @ColorInt val iconColor: Int,
  @ColorInt val backgroundColor: Int? = null,
  val backgroundAlpha: Int = 168,
  val padding: Int = 16,
  @Px val width: Int? = null,
  @Px val height: Int? = null,
)

// Cache of [BitmapDescriptor]s
private val bitmapCache = mutableMapOf<BitmapParameters, BitmapDescriptor>()

/**
 * Returns a [BitmapDescriptor] for a given [BitmapParameters] set.
 */
fun vectorToBitmap(context: Context, parameters: BitmapParameters): BitmapDescriptor {
  return bitmapCache[parameters] ?: createBitmapDescriptor(context, parameters)
}

@ReadOnlyComposable
@Composable
fun vectorToBitmap(parameters: BitmapParameters): BitmapDescriptor =
  vectorToBitmap(LocalContext.current, parameters)

/**
 * Creates a customized [BitmapDescriptor] for use as a map marker.
 */
private fun createBitmapDescriptor(
  context: Context,
  parameters: BitmapParameters
): BitmapDescriptor {
  val vectorDrawable = ResourcesCompat.getDrawable(
    context.resources,
    parameters.id,
    null
  ) ?: return run {
    Timber.tag(TAG).e("Resource not found")
    BitmapDescriptorFactory.defaultMarker()
  }

  val padding = if (parameters.backgroundColor != null) parameters.padding else 0
  val halfPadding = padding / 2

  val bitmap = Bitmap.createBitmap(
    /* width = */ parameters.width ?: (vectorDrawable.intrinsicWidth + padding),
    /* height = */ parameters.height ?: (vectorDrawable.intrinsicHeight + padding),
    /* config = */ Bitmap.Config.ARGB_8888
  )
  val canvas = Canvas(bitmap)
  vectorDrawable.setBounds(
    /* left = */ halfPadding,
    /* top = */ halfPadding,
    /* right = */ canvas.width - halfPadding,
    /* bottom = */ canvas.height - halfPadding
  )
  DrawableCompat.setTint(vectorDrawable, parameters.iconColor)

  if (parameters.backgroundColor != null) {
    val fillPaint = Paint().apply {
      style = Paint.Style.FILL
      color = parameters.backgroundColor
      alpha = parameters.backgroundAlpha
    }

    val strokePaint = Paint().apply {
      style = Paint.Style.STROKE
      color = parameters.backgroundColor
      strokeWidth = 3f
    }

    canvas.drawCircle(
      canvas.width / 2.0f,
      canvas.height / 2.0f,
      canvas.width.toFloat() / 2,
      fillPaint
    )
    canvas.drawCircle(
      canvas.width / 2.0f,
      canvas.height / 2.0f,
      (canvas.width.toFloat() / 2) - 3,
      strokePaint
    )
  }
  vectorDrawable.draw(canvas)

  return BitmapDescriptorFactory.fromBitmap(bitmap).also { bitmapCache[parameters] = it }
}
