package me.calebjones.spacelaunchnow.util.logging

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Keeps `DiagnosticEventKind` and docs/logging/LOG_EVENTS.md in agreement (spec phase 4). */
class DiagnosticEventCatalogueTest {

    private fun catalogueFile(): File {
        var dir: File? = File(System.getProperty("user.dir")).absoluteFile
        while (dir != null) {
            val f = File(dir, "docs/logging/LOG_EVENTS.md")
            if (f.isFile) return f
            dir = dir.parentFile
        }
        error("docs/logging/LOG_EVENTS.md not found above ${System.getProperty("user.dir")}")
    }

    /** kind -> severity cell, from table rows whose first cell is a backticked kind. */
    private fun catalogueRows(): Map<String, String> =
        catalogueFile().readLines().mapNotNull { line ->
            val cells = line.trim().trim('|').split('|').map { it.trim() }
            val kind = cells.firstOrNull()?.let { Regex("^`([a-z_]+)`$").find(it)?.groupValues?.get(1) }
            if (kind != null && kind != "log_kind" && cells.size >= 5) kind to cells[2] else null
        }.toMap()

    @Test
    fun everyEnumKindIsCatalogued() {
        val rows = catalogueRows()
        val missing = DiagnosticEventKind.entries.map { it.kind }.filter { it !in rows }
        assertTrue(missing.isEmpty(), "Kinds missing from LOG_EVENTS.md: $missing")
    }

    @Test
    fun everyCataloguedKindExistsInEnum() {
        val known = DiagnosticEventKind.entries.map { it.kind }.toSet()
        val extra = catalogueRows().keys.filter { it !in known }
        assertTrue(extra.isEmpty(), "LOG_EVENTS.md rows with no enum entry: $extra")
    }

    @Test
    fun everyCataloguedKindHasASeverity() {
        val blank = catalogueRows().filterValues { it.isBlank() }.keys
        assertTrue(blank.isEmpty(), "Kinds without a severity: $blank")
        assertEquals(DiagnosticEventKind.entries.size, catalogueRows().size)
    }
}
