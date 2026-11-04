package com.opencanvas.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class BoardModelsTest {
    @Test
    fun codecRoundTripPreservesStrokeStyleAndPoints() {
        val board = BoardDocument(
            id = "board-1",
            title = "Sketch",
            updatedAt = 42L,
            items = listOf(
                BoardItem(
                    id = "stroke",
                    points = listOf(BoardPoint(1f, 2f), BoardPoint(8f, 9f)),
                    color = 0xFF123456L,
                    width = 4f,
                    opacity = 0.5f,
                ),
            ),
        )

        assertEquals(listOf(board), BoardCodec.decode(BoardCodec.encode(listOf(board))))
    }

    @Test
    fun decoderDropsLegacyNonDrawingObjectsButKeepsStrokes() {
        val encoded = """[{"id":"board","title":"Old board","updatedAt":1,"items":[{"type":"note","id":"note"},{"type":"stroke","id":"line","color":4278190080,"width":5.0,"opacity":1.0,"points":[[0.0,0.0],[10.0,10.0]]}]}]"""

        val decoded = BoardCodec.decode(encoded)

        assertEquals(1, decoded.single().items.size)
        assertEquals("line", decoded.single().items.single().id)
    }

    @Test
    fun hitTestReturnsTopmostStrokeAndRejectsEmptySpace() {
        val bottom = BoardItem(
            id = "bottom",
            points = listOf(BoardPoint(0f, 0f), BoardPoint(100f, 100f)),
            color = DEFAULT_INK,
            width = 5f,
        )
        val top = BoardItem(
            id = "top",
            points = listOf(BoardPoint(0f, 100f), BoardPoint(100f, 0f)),
            color = DEFAULT_INK,
            width = 5f,
        )
        val items = listOf(bottom, top)

        assertSame(top, hitTest(items, BoardPoint(50f, 50f), tolerance = 0f))
        assertSame(bottom, hitTest(items, BoardPoint(10f, 10f), tolerance = 0f))
        assertNull(hitTest(items, BoardPoint(200f, 200f), tolerance = 0f))
    }

    @Test
    fun movingAStrokeTranslatesEveryPointWithoutChangingStyle() {
        val original = BoardItem(
            id = "stroke",
            points = listOf(BoardPoint(1f, 2f), BoardPoint(3f, 5f)),
            color = 0xFF445566L,
            width = 7f,
        )

        assertEquals(
            original.copy(points = listOf(BoardPoint(11f, -2f), BoardPoint(13f, 1f))),
            original.moved(10f, -4f),
        )
    }
}
