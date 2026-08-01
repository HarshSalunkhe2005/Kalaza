package com.kalazacare.app.util

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Minimal OOXML (.xlsx) writer with no third-party dependency — an xlsx is
 * just a zip of small XML parts. Purpose-built for the "Simple Patient
 * Report" (one styled sheet, replicating a reference template: a dark title
 * banner, a blue column-header row, alternating row banding, wrapped text,
 * and frozen header rows) rather than a general-purpose spreadsheet API,
 * since that's the only shape ever needed here.
 */
class XlsxWriter {

    /** One data row of the report — one per calendar date in the selected range. */
    data class ReportRow(
        val date: String,
        val diagnosis: String,
        val medication: String,
        val vitals: String,
        val utilities: String,
        val notes: String,
        val signedBy: String,
    )

    private val columnWidths = listOf(13.0, 42.0, 40.0, 46.0, 36.0, 52.0, 18.0)
    private val columnHeaders = listOf(
        "Date", "Diagnosis", "Medication\n(Medicine — Dose — Status)",
        "Vitals\n(Pulse / BP / SpO₂ / Temp / Fasting / PP)",
        "Utilities\n(Issued To — By — Checked By)", "Notes / Care Observations", "Signed By",
    )

    /** Builds the single-sheet "Patient Report" workbook for one patient and date range. */
    fun buildPatientReport(patientName: String, rangeLabel: String, rows: List<ReportRow>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            fun entry(path: String, content: String) {
                zip.putNextEntry(ZipEntry(path))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            entry("[Content_Types].xml", contentTypesXml())
            entry("_rels/.rels", relsXml())
            entry("xl/workbook.xml", workbookXml())
            entry("xl/_rels/workbook.xml.rels", workbookRelsXml())
            entry("xl/styles.xml", stylesXml())
            entry("xl/worksheets/sheet1.xml", sheetXml(patientName, rangeLabel, rows))
        }
        return out.toByteArray()
    }

    private fun contentTypesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

    private fun relsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun workbookXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Patient Report" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""

    private fun workbookRelsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    // Style indices used by sheetXml — kept in sync with cellXfs below.
    private val styleTitle = 1
    private val styleHeader = 2
    private val styleDataWhite = 3
    private val styleDataBlue = 4
    private val styleDataBoldWhite = 5
    private val styleDataBoldBlue = 6

    private fun stylesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<fonts count="4">
<font><sz val="10"/><name val="Arial"/></font>
<font><b/><sz val="10"/><name val="Arial"/></font>
<font><b/><sz val="13"/><color rgb="FFFFFFFF"/><name val="Arial"/></font>
<font><b/><sz val="10"/><color rgb="FFFFFFFF"/><name val="Arial"/></font>
</fonts>
<fills count="4">
<fill><patternFill patternType="none"/></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FF1F3864"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FF2F5496"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFEBF3FB"/><bgColor indexed="64"/></patternFill></fill>
</fills>
<borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="7">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="0" fontId="2" fillId="1" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
<xf numFmtId="0" fontId="3" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="left" vertical="center" wrapText="1"/></xf>
<xf numFmtId="0" fontId="0" fillId="3" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="left" vertical="center" wrapText="1"/></xf>
<xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="left" vertical="center" wrapText="1"/></xf>
<xf numFmtId="0" fontId="1" fillId="3" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="left" vertical="center" wrapText="1"/></xf>
</cellXfs>
</styleSheet>"""

    private fun sheetXml(patientName: String, rangeLabel: String, rows: List<ReportRow>): String {
        val colCount = columnHeaders.size
        val lastColLetter = columnLetter(colCount - 1)

        val cols = columnWidths.mapIndexed { i, w ->
            "<col min=\"${i + 1}\" max=\"${i + 1}\" width=\"$w\" customWidth=\"1\"/>"
        }.joinToString("")

        val titleRow = cell(0, 0, styleTitle, "KalazaCare — Patient Report  |  $patientName  |  $rangeLabel")
        val titleRowXml = "<row r=\"1\" ht=\"32\" customHeight=\"1\">$titleRow</row>"

        val headerCells = columnHeaders.mapIndexed { c, text -> cell(c, 1, styleHeader, text) }.joinToString("")
        val headerRowXml = "<row r=\"2\" ht=\"38\" customHeight=\"1\">$headerCells</row>"

        val dataRowsXml = rows.mapIndexed { i, row ->
            val rIdx = i + 2 // 0-based row index (row 0 = title, row 1 = header, data starts at 2)
            val isEven = i % 2 == 1
            val plainStyle = if (isEven) styleDataBlue else styleDataWhite
            val boldStyle = if (isEven) styleDataBoldBlue else styleDataBoldWhite
            val values = listOf(row.date, row.diagnosis, row.medication, row.vitals, row.utilities, row.notes, row.signedBy)
            val cells = values.mapIndexed { c, v -> cell(c, rIdx, if (c == 0) boldStyle else plainStyle, v) }.joinToString("")
            "<row r=\"${rIdx + 1}\" ht=\"55\" customHeight=\"1\">$cells</row>"
        }.joinToString("")

        val lastRow = rows.size + 2
        val sheetViews = """<sheetViews><sheetView workbookViewId="0"><pane ySplit="2" topLeftCell="A3" activePane="bottomLeft" state="frozen"/><selection pane="bottomLeft" activeCell="A3" sqref="A3"/></sheetView></sheetViews>"""
        val mergeCells = """<mergeCells count="1"><mergeCell ref="A1:${lastColLetter}1"/></mergeCells>"""

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<dimension ref="A1:${lastColLetter}$lastRow"/>
$sheetViews
<cols>$cols</cols>
<sheetData>$titleRowXml$headerRowXml$dataRowsXml</sheetData>
$mergeCells
</worksheet>"""
    }

    private fun cell(col: Int, row: Int, style: Int, value: String): String {
        val ref = "${columnLetter(col)}${row + 1}"
        return "<c r=\"$ref\" s=\"$style\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${escape(value)}</t></is></c>"
    }

    private fun columnLetter(index: Int): String {
        var i = index
        val sb = StringBuilder()
        do {
            sb.insert(0, 'A' + (i % 26))
            i = i / 26 - 1
        } while (i >= 0)
        return sb.toString()
    }

    private fun escape(s: String) = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
