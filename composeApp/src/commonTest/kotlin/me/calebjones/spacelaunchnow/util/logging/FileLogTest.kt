package me.calebjones.spacelaunchnow.util.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeStore : DiagnosticsFileStore {
    var content: String? = null
    var writes = 0
    override fun read(): String? = content
    override fun write(content: String) { this.content = content; writes++ }
}

class FileLogTest {

    @Test
    fun ringBuffer_dropsOldestWhenOverCap() {
        val buf = LogRingBuffer(maxChars = 30)
        buf.append("aaaaaaaaaa") // 10 + 1
        buf.append("bbbbbbbbbb")
        buf.append("cccccccccc")
        // 3 lines x 11 chars = 33 > 30 -> oldest dropped
        assertEquals("bbbbbbbbbb\ncccccccccc", buf.snapshot())
    }

    @Test
    fun ringBuffer_tailCapsFromEnd() {
        val buf = LogRingBuffer(maxChars = 1000)
        buf.append("hello")
        buf.append("world")
        assertEquals("world", buf.tail(5))
        assertEquals("hello\nworld", buf.tail(999))
    }

    @Test
    fun core_loadsExistingFileOnFirstUse() {
        val store = FakeStore().apply { content = "old line 1\nold line 2" }
        val core = FileLogCore(store, maxChars = 1000, flushEveryLines = 100)
        assertTrue(core.export(1000).startsWith("old line 1"))
    }

    @Test
    fun core_flushesEveryNLines() {
        val store = FakeStore()
        val core = FileLogCore(store, maxChars = 1000, flushEveryLines = 3)
        core.append("a", urgent = false)
        core.append("b", urgent = false)
        assertEquals(0, store.writes)
        core.append("c", urgent = false)
        assertEquals(1, store.writes)
        assertEquals("a\nb\nc", store.content)
    }

    @Test
    fun core_flushesImmediatelyWhenUrgent() {
        val store = FakeStore()
        val core = FileLogCore(store, maxChars = 1000, flushEveryLines = 100)
        core.append("warn line", urgent = true)
        assertEquals(1, store.writes)
    }

    @Test
    fun core_exportFlushesAndCaps() {
        val store = FakeStore()
        val core = FileLogCore(store, maxChars = 1000, flushEveryLines = 100)
        core.append("0123456789", urgent = false)
        val exported = core.export(maxChars = 4)
        assertEquals("6789", exported)
        assertEquals(1, store.writes) // export forced a flush
    }
}
