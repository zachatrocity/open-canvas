package com.opencanvas.app

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

class ViewportTransformTest {
    @Test
    fun twoFingerPanMovesViewportByCentroidDelta() {
        val result = transformViewport(
            viewport = Offset(10f, 20f),
            previousCentroid = Offset(100f, 100f),
            currentCentroid = Offset(120f, 130f),
            scaleChange = 1f,
        )

        assertEquals(Offset(30f, 50f), result)
    }

    @Test
    fun pinchKeepsBoardPointUnderGestureCentroid() {
        val oldViewport = Offset(10f, 20f)
        val centroid = Offset(100f, 120f)
        val boardPointUnderCentroid = (centroid - oldViewport) / 1f

        val newViewport = transformViewport(
            viewport = oldViewport,
            previousCentroid = centroid,
            currentCentroid = centroid,
            scaleChange = 2f,
        )

        assertEquals(centroid, boardPointUnderCentroid * 2f + newViewport)
    }
}
