package com.kalazacare.app.ui.utility

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalazacare.app.data.model.UtilityRecord
import com.kalazacare.app.ui.theme.KalazaRed
import com.kalazacare.app.ui.theme.OnSurface
import com.kalazacare.app.ui.theme.SurfaceVariant
import com.kalazacare.app.ui.theme.White
import com.kalazacare.app.util.DateUtils

@Composable
fun UtilityTable(
    records: List<UtilityRecord>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KalazaRed)
                .horizontalScroll(scrollState)
                .padding(vertical = 12.dp)
        ) {
            HeaderCell("Date", width = 100.dp)
            HeaderCell("Time", width = 80.dp)
            HeaderCell("Face Mask", width = 100.dp)
            HeaderCell("Diaper (Pant)", width = 120.dp)
            HeaderCell("Diaper (Stitch)", width = 130.dp)
            HeaderCell("Hand Gloves", width = 120.dp)
            HeaderCell("Tina Bed", width = 100.dp)
            HeaderCell("Wet Wipes", width = 100.dp)
            HeaderCell("Issued To", width = 120.dp)
            HeaderCell("Issued By", width = 120.dp)
            HeaderCell("Checked By", width = 120.dp)
        }

        // Table Body
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(records) { index, record ->
                val backgroundColor = if (index % 2 == 0) White else SurfaceVariant
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .horizontalScroll(scrollState)
                        .padding(vertical = 12.dp)
                ) {
                    DataCell(DateUtils.formatDate(record.date), width = 100.dp)
                    DataCell(DateUtils.formatTime(record.time), width = 80.dp)
                    DataCell(record.faceMask.toString(), width = 100.dp)
                    DataCell(record.diaperPant.toString(), width = 120.dp)
                    DataCell(record.diaperStitch.toString(), width = 130.dp)
                    DataCell(record.handGloves.toString(), width = 120.dp)
                    DataCell(record.tinaBed.toString(), width = 100.dp)
                    DataCell(record.wetWipes.toString(), width = 100.dp)
                    DataCell(record.issuedToCaregiver, width = 120.dp)
                    DataCell(record.issuedBySupervisor, width = 120.dp)
                    DataCell(record.checkedBy, width = 120.dp)
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        color = White,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier
            .width(width)
            .padding(horizontal = 16.dp)
    )
}

@Composable
private fun DataCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text.ifBlank { "-" },
        color = OnSurface,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 16.dp)
    )
}
