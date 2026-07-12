package com.kalazacare.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val KalazaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // Chips, small tags
    small       = RoundedCornerShape(8.dp),   // Text fields, small cards
    medium      = RoundedCornerShape(12.dp),  // Buttons, dialogs
    large       = RoundedCornerShape(16.dp),  // Cards, patient cards
    extraLarge  = RoundedCornerShape(24.dp),  // Bottom sheets, modals
)
