package com.opencanvas.app

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.hypot
import kotlin.math.max

const val DEFAULT_INK: Long = 0xFF17191EL

data class BoardPoint(val x: Float, val y: Float)

data class BoardDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val items: List<BoardItem> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class BoardItem(
    val id: String = UUID.randomUUID().toString(),
    val points: List<BoardPoint>,
    val color: Long,
    val width: Float,
    val opacity: Float = 1f,
)

enum class CanvasTool { PEN, MARKER, ERASER }

data class ItemBounds(val left: Float, val top: Float, val right: Float, val bottom: Float)

fun BoardItem.bounds(): ItemBounds {
    if (points.isEmpty()) return ItemBounds(0f, 0f, 0f, 0f)
    return ItemBounds(
        points.minOf { it.x },
        points.minOf { it.y },
        points.maxOf { it.x },
        points.maxOf { it.y },
    )
}


fun hitTest(items: List<BoardItem>, point: BoardPoint, tolerance: Float = 16f): BoardItem? =
    items.asReversed().firstOrNull { item ->
        item.points.zipWithNext().any { (a, b) ->
            distanceToSegment(point, a, b) <= max(tolerance, item.width)
        }
    }

private fun distanceToSegment(point: BoardPoint, a: BoardPoint, b: BoardPoint): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    if (dx == 0f && dy == 0f) return hypot(point.x - a.x, point.y - a.y)
    val t = (((point.x - a.x) * dx + (point.y - a.y) * dy) / (dx * dx + dy * dy)).coerceIn(0f, 1f)
    return hypot(point.x - (a.x + t * dx), point.y - (a.y + t * dy))
}

object BoardCodec {
    fun encode(boards: List<BoardDocument>): String = JSONArray().apply {
        boards.forEach { put(encodeBoard(it)) }
    }.toString()

    fun decode(raw: String): List<BoardDocument> {
        if (raw.isBlank()) return emptyList()
        val json = JSONArray(raw)
        return List(json.length()) { decodeBoard(json.getJSONObject(it)) }
    }

    private fun encodeBoard(board: BoardDocument) = JSONObject().apply {
        put("id", board.id)
        put("title", board.title)
        put("updatedAt", board.updatedAt)
        put("items", JSONArray().apply { board.items.forEach { put(encodeStroke(it)) } })
    }

    private fun decodeBoard(json: JSONObject): BoardDocument {
        val items = json.getJSONArray("items")
        return BoardDocument(
            id = json.getString("id"),
            title = json.getString("title"),
            updatedAt = json.optLong("updatedAt", 0L),
            items = buildList {
                repeat(items.length()) {
                    decodeStroke(items.getJSONObject(it))?.let(::add)
                }
            },
        )
    }

    private fun encodeStroke(item: BoardItem) = JSONObject().apply {
        put("type", "stroke")
        put("id", item.id)
        put("color", item.color)
        put("width", item.width.toDouble())
        put("opacity", item.opacity.toDouble())
        put("points", JSONArray().apply {
            item.points.forEach { put(JSONArray().put(it.x.toDouble()).put(it.y.toDouble())) }
        })
    }

    private fun decodeStroke(json: JSONObject): BoardItem? {
        if (json.optString("type") != "stroke") return null
        return BoardItem(
            id = json.getString("id"),
            points = json.getJSONArray("points").let { points ->
                List(points.length()) {
                    points.getJSONArray(it).let { pair ->
                        BoardPoint(pair.getDouble(0).toFloat(), pair.getDouble(1).toFloat())
                    }
                }
            },
            color = json.getLong("color"),
            width = json.getDouble("width").toFloat(),
            opacity = json.optDouble("opacity", 1.0).toFloat(),
        )
    }
}

fun welcomeBoard() = BoardDocument(
    id = "welcome-board",
    title = "My sketchbook",
    items = listOf(
        BoardItem(
            points = listOf(
                BoardPoint(190f, 470f), BoardPoint(330f, 300f), BoardPoint(455f, 435f),
                BoardPoint(590f, 230f), BoardPoint(820f, 470f),
            ),
            color = DEFAULT_INK,
            width = 8f,
        ),
        BoardItem(
            points = listOf(BoardPoint(240f, 470f), BoardPoint(770f, 470f)),
            color = 0xFF2266E3L,
            width = 18f,
            opacity = 0.55f,
        ),
        BoardItem(
            points = circlePoints(700f, 190f, 58f),
            color = 0xFFFFC62EL,
            width = 13f,
        ),
        BoardItem(
            points = listOf(
                BoardPoint(265f, 560f), BoardPoint(325f, 520f), BoardPoint(385f, 570f),
                BoardPoint(450f, 515f), BoardPoint(520f, 560f), BoardPoint(600f, 510f),
                BoardPoint(690f, 565f), BoardPoint(765f, 525f),
            ),
            color = 0xFF20B77AL,
            width = 10f,
        ),
        BoardItem(
            points = listOf(
                BoardPoint(300f, 660f), BoardPoint(390f, 625f), BoardPoint(480f, 670f),
                BoardPoint(575f, 620f), BoardPoint(690f, 660f),
            ),
            color = 0xFFFF3E55L,
            width = 7f,
        ),
    ),
)

private fun circlePoints(cx: Float, cy: Float, radius: Float): List<BoardPoint> =
    List(33) { index ->
        val angle = Math.PI * 2.0 * index / 32.0
        BoardPoint(
            cx + radius * kotlin.math.cos(angle).toFloat(),
            cy + radius * kotlin.math.sin(angle).toFloat(),
        )
    }
