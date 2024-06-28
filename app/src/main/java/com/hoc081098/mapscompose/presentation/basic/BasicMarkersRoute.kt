package com.hoc081098.mapscompose.presentation.basic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.hoc081098.mapscompose.R
import com.hoc081098.mapscompose.presentation.markers.MarkersScreen
import com.hoc081098.mapscompose.presentation.markers.rememberCurrentLocationBitmapDescriptor
import com.hoc081098.mapscompose.presentation.models.StoreUiModel
import com.hoc081098.mapscompose.presentation.models.toGmsLatLng
import com.hoc081098.mapscompose.presentation.utils.bitmapDescriptorFactory
import kotlinx.serialization.Serializable

@Serializable
data object BasicMarkersRoute

@Composable
fun BasicMarkersScreen(
  modifier: Modifier = Modifier
) {
  var selectedStore by remember { mutableStateOf<StoreUiModel?>(null) }
  val onClickStore = { store: StoreUiModel -> selectedStore = store }
  val onDrag = { selectedStore = null }

  val selectedStoreIcon = rememberStoreIconBitmapDescriptor(iconSize = 36.dp * 1.5f)
  val normalStoreIcon = rememberStoreIconBitmapDescriptor(iconSize = 36.dp)
  val currentLocationIcon = rememberCurrentLocationBitmapDescriptor()

  MarkersScreen(
    modifier = modifier,
    title = "Basic markers",
    onDrag = onDrag,
  ) { uiState ->
    uiState.stores.forEach { store ->
      Marker(
        state = MarkerState(
          position = remember(store.latLng) { store.latLng.toGmsLatLng() }
        ),
        icon = if (selectedStore == store) {
          selectedStoreIcon
        } else {
          normalStoreIcon
        },
        title = store.name,
        onClick = {
          onClickStore(store)
          true
        },
        anchor = Offset(x = 0.5f, y = 0.5f),
        snippet = "Favorite: ${store.isFavorite}",
        zIndex = if (store.isFavorite) 5f else 2f,
        tag = store.id,
      )
    }

    Marker(
      state = MarkerState(
        position = remember(uiState.currentLatLng) { uiState.currentLatLng.toGmsLatLng() }
      ),
      icon = currentLocationIcon,
      anchor = Offset(x = 0.5f, y = 0.5f),
    )
  }
}

@Composable
private fun rememberStoreIconBitmapDescriptor(iconSize: Dp): BitmapDescriptor? {
  val context = LocalContext.current
  val density = LocalDensity.current

  return remember(context, density, iconSize) {
    val currentIconSizeInDp = density.run { iconSize.toPx() }.toInt()
    context.bitmapDescriptorFactory(
      resId = R.drawable.ic_store_96,
      width = currentIconSizeInDp,
      height = currentIconSizeInDp,
    )
  }
}
