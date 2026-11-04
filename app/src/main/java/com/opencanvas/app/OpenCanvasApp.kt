package com.opencanvas.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.DateFormat
import java.util.ArrayDeque
import java.util.Date
import kotlin.math.max
import kotlin.math.min

private val AppBackground = Color(0xFFF2F3ED)
private val CanvasBackground = Color(0xFFFAFBF7)
private val Surface = Color(0xFFFFFFFF)
private val Ink = Color(0xFF17191E)
private val Muted = Color(0xFF62666F)
private val Accent = Color(0xFF2266E3)
private val Brand = Color(0xFFF5C842)
private val Border = Color(0xFFD8DAD3)
private val Palette = listOf(0xFF17191EL, 0xFF2266E3L, 0xFF20B77AL, 0xFFFFC62EL, 0xFFFF3E55L, 0xFF8C5DE8L)

@Composable
fun OpenCanvasApp(state: OpenCanvasState) {
    val board = state.openBoardId?.let { id -> state.boards.firstOrNull { it.id == id } }
    BackHandler(enabled = board != null) { state.showLibrary() }
    Box(Modifier.fillMaxSize().background(AppBackground)) {
        if (board == null) {
            BoardLibrary(
                boards = state.boards,
                onOpen = state::openBoard,
                onCreate = state::createBoard,
                onDuplicate = state::duplicateBoard,
                onDelete = state::deleteBoard,
            )
        } else {
            BoardEditor(board, state::showLibrary, state::updateBoard)
        }
    }
}

@Composable
private fun BoardLibrary(
    boards: List<BoardDocument>,
    onOpen: (String) -> Unit,
    onCreate: () -> Unit,
    onDuplicate: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<BoardDocument?>(null) }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 22.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                AppText("OpenCanvas", 32.sp, FontWeight.Bold)
                AppText("A focused drawing space", 14.sp, FontWeight.Normal, color = Muted)
            }
            PillButton("New drawing", onCreate, dark = true)
        }
        AppText("My drawings", 20.sp, FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))
        if (boards.isEmpty()) {
            EmptyLibrary(onCreate)
        } else {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                boards.forEach { board ->
                    BoardCard(
                        board = board,
                        onOpen = onOpen,
                        onDuplicate = onDuplicate,
                        onDelete = { pendingDelete = board },
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
    pendingDelete?.let { board ->
        DeleteDrawingDialog(
            title = board.title,
            onDismiss = { pendingDelete = null },
            onConfirm = {
                onDelete(board.id)
                pendingDelete = null
            },
        )
    }
}
@Composable
private fun DeleteDrawingDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth()
                .background(Surface, RoundedCornerShape(28.dp))
                .padding(24.dp),
        ) {
            AppText("Delete drawing?", 22.sp, FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            AppText(
                "\"$title\" will be removed from this device.",
                15.sp,
                FontWeight.Normal,
                color = Muted,
            )
            Spacer(Modifier.height(22.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            ) {
                PillButton("Keep", onDismiss)
                PillButton("Delete", onConfirm, dark = true)
            }
        }
    }
}


@Composable
private fun EmptyLibrary(onCreate: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(72.dp).background(Brand, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center,
        ) { SketchMark(Modifier.size(38.dp)) }
        Spacer(Modifier.height(18.dp))
        AppText("Start a new drawing", 20.sp, FontWeight.SemiBold)
        AppText("Pick a pen and put an idea on the canvas.", 14.sp, FontWeight.Normal, color = Muted)
        Spacer(Modifier.height(20.dp))
        PillButton("Create drawing", onCreate, dark = true)
    }
}

@Composable
private fun BoardCard(
    board: BoardDocument,
    onOpen: (String) -> Unit,
    onDuplicate: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .background(Surface, RoundedCornerShape(22.dp))
            .border(1.dp, Border, RoundedCornerShape(22.dp))
            .clickable { onOpen(board.id) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoardPreview(board, Modifier.size(width = 112.dp, height = 96.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            AppText(board.title, 17.sp, FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            val date = remember(board.updatedAt) {
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(board.updatedAt))
            }
            AppText("${board.items.size} strokes  ·  $date", 13.sp, FontWeight.Normal, color = Muted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SmallAction("Duplicate") { onDuplicate(board.id) }
                if (board.id != "welcome-board") {
                    SmallAction("Delete", color = Color(0xFFB4233D)) { onDelete(board.id) }
                }
            }
        }
    }
}

@Composable
private fun BoardPreview(board: BoardDocument, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Canvas(modifier.clip(shape).background(Color(0xFFF6F7F2))) {
        val padding = 12.dp.toPx()
        if (board.items.isEmpty()) {
            drawLine(
                color = Color(0xFFC6C9C1),
                start = Offset(size.width * 0.28f, size.height * 0.62f),
                end = Offset(size.width * 0.72f, size.height * 0.38f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
            return@Canvas
        }
        val bounds = board.items.map { it.bounds() }
        val left = bounds.minOf { it.left }
        val top = bounds.minOf { it.top }
        val contentWidth = max(1f, bounds.maxOf { it.right } - left)
        val contentHeight = max(1f, bounds.maxOf { it.bottom } - top)
        val scale = min(
            (size.width - padding * 2f) / contentWidth,
            (size.height - padding * 2f) / contentHeight,
        )
        val x = (size.width - contentWidth * scale) / 2f - left * scale
        val y = (size.height - contentHeight * scale) / 2f - top * scale
        withTransform({
            translate(x, y)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            board.items.forEach { drawBoardItem(it) }
        }
    }
}

@Composable
private fun SketchMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.08f, size.height * 0.68f)
            cubicTo(
                size.width * 0.28f,
                size.height * 0.15f,
                size.width * 0.42f,
                size.height * 0.9f,
                size.width * 0.58f,
                size.height * 0.44f,
            )
            cubicTo(
                size.width * 0.72f,
                size.height * 0.08f,
                size.width * 0.86f,
                size.height * 0.32f,
                size.width * 0.92f,
                size.height * 0.6f,
            )
        }
        drawPath(path, Ink, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Stable
private class EditorSession(
    board: BoardDocument,
    private val onChange: (BoardDocument) -> Unit,
) {
    private val boardId = board.id
    private var title by mutableStateOf(board.title)
    val items = mutableStateListOf<BoardItem>().apply { addAll(board.items) }
    private val undoStack = ArrayDeque<List<BoardItem>>()
    private val redoStack = ArrayDeque<List<BoardItem>>()
    var selectedId by mutableStateOf<String?>(null)
    val previewPoints = mutableStateListOf<BoardPoint>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
    fun currentTitle() = title
    fun selectedItem(): BoardItem? = items.firstOrNull { it.id == selectedId }
    fun snapshot(): List<BoardItem> = items.toList()

    fun rename(value: String) {
        title = value
        publish()
    }

    fun select(id: String?) { selectedId = id }

    fun add(item: BoardItem) {
        checkpoint()
        items += item
        selectedId = null
        publish()
    }

    fun eraseAt(point: BoardPoint) {
        val item = hitTest(items, point, 20f) ?: return
        items.remove(item)
        if (selectedId == item.id) selectedId = null
    }

    fun moveSelected(dx: Float, dy: Float) {
        val id = selectedId ?: return
        val index = items.indexOfFirst { it.id == id }
        if (index >= 0) items[index] = items[index].moved(dx, dy)
    }

    fun finishTransaction(before: List<BoardItem>) {
        if (before == items) return
        undoStack.addLast(before)
        redoStack.clear()
        publish()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.addLast(items.toList())
        replaceAll(undoStack.removeLast())
        selectedId = null
        publish()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.addLast(items.toList())
        replaceAll(redoStack.removeLast())
        selectedId = null
        publish()
    }

    private fun checkpoint() {
        undoStack.addLast(items.toList())
        if (undoStack.size > 80) undoStack.removeFirst()
        redoStack.clear()
    }

    private fun replaceAll(newItems: List<BoardItem>) {
        items.clear()
        items.addAll(newItems)
    }

    private fun publish() = onChange(BoardDocument(boardId, title, items.toList()))
}

@Composable
private fun BoardEditor(
    board: BoardDocument,
    onBack: () -> Unit,
    onBoardChange: (BoardDocument) -> Unit,
) {
    val session = remember(board.id) { EditorSession(board, onBoardChange) }
    var tool by remember { mutableStateOf(CanvasTool.PEN) }
    var color by remember { mutableLongStateOf(DEFAULT_INK) }
    var scale by remember { mutableFloatStateOf(0.78f) }
    var viewport by remember { mutableStateOf(Offset(140f, 246f)) }

    Box(Modifier.fillMaxSize().background(CanvasBackground)) {
        DrawingCanvas(
            session = session,
            tool = tool,
            color = color,
            scale = scale,
            viewport = viewport,
            onViewportChange = { viewport = it },
        )
        EditorTopBar(
            title = session.currentTitle(),
            canUndo = session.canUndo,
            canRedo = session.canRedo,
            onTitleChange = session::rename,
            onBack = onBack,
            onUndo = session::undo,
            onRedo = session::redo,
        )
        ZoomRail(
            scale = scale,
            onZoomOut = { scale = (scale / 1.2f).coerceAtLeast(0.35f) },
            onZoomIn = { scale = (scale * 1.2f).coerceAtMost(2.5f) },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
        )
        DrawingDock(
            selectedTool = tool,
            selectedColor = color,
            onTool = { tool = it },
            onColor = { color = it },
            modifier = Modifier.align(Alignment.BottomCenter)
                .navigationBarsPadding().padding(horizontal = 12.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun EditorTopBar(
    title: String,
    canUndo: Boolean,
    canRedo: Boolean,
    onTitleChange: (String) -> Unit,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RoundAction("Drawings", ActionIcon.BACK, onBack)
        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            singleLine = true,
            textStyle = TextStyle(color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f)
                .background(Surface.copy(alpha = 0.96f), RoundedCornerShape(20.dp))
                .border(1.dp, Border, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 13.dp),
        )
        Row(
            Modifier.background(Surface.copy(alpha = 0.96f), RoundedCornerShape(24.dp))
                .border(1.dp, Border, RoundedCornerShape(24.dp)).padding(2.dp),
        ) {
            RoundAction("Undo", ActionIcon.UNDO, onUndo, canUndo, flat = true)
            RoundAction("Redo", ActionIcon.REDO, onRedo, canRedo, flat = true)
        }
    }
}

@Composable
private fun ZoomRail(
    scale: Float,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.background(Surface.copy(alpha = 0.96f), RoundedCornerShape(24.dp))
            .border(1.dp, Border, RoundedCornerShape(24.dp)).padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RoundAction("Zoom in", ActionIcon.PLUS, onZoomIn, flat = true)
        AppText("${(scale * 100).toInt()}%", 11.sp, FontWeight.SemiBold, color = Muted)
        RoundAction("Zoom out", ActionIcon.MINUS, onZoomOut, flat = true)
    }
}

@Composable
private fun DrawingCanvas(
    session: EditorSession,
    tool: CanvasTool,
    color: Long,
    scale: Float,
    viewport: Offset,
    onViewportChange: (Offset) -> Unit,
) {
    Canvas(
        Modifier.fillMaxSize().pointerInput(tool, color, scale, viewport) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val startBoard = screenToBoard(down.position, viewport, scale)
                when (tool) {
                    CanvasTool.PEN, CanvasTool.MARKER -> {
                        session.previewPoints.clear()
                        session.previewPoints += startBoard
                        down.consume()
                        var pressed = true
                        while (pressed) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val change = event.changes.first()
                            if (change.pressed) {
                                session.previewPoints += screenToBoard(change.position, viewport, scale)
                                change.consume()
                            }
                            pressed = event.changes.any { it.pressed }
                        }
                        if (session.previewPoints.size > 1) {
                            session.add(
                                BoardItem(
                                    points = session.previewPoints.toList(),
                                    color = color,
                                    width = if (tool == CanvasTool.MARKER) 26f else 5.5f,
                                    opacity = if (tool == CanvasTool.MARKER) 0.42f else 1f,
                                ),
                            )
                        }
                        session.previewPoints.clear()
                    }
                    CanvasTool.ERASER -> {
                        val before = session.snapshot()
                        session.eraseAt(startBoard)
                        var pressed = true
                        while (pressed) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    session.eraseAt(screenToBoard(change.position, viewport, scale))
                                    change.consume()
                                }
                            }
                            pressed = event.changes.any { it.pressed }
                        }
                        session.finishTransaction(before)
                    }
                    CanvasTool.SELECT -> {
                        val hit = hitTest(session.items, startBoard, 18f / scale)
                        if (hit != null) {
                            session.select(hit.id)
                            val before = session.snapshot()
                            var previous = down.position
                            var pressed = true
                            while (pressed) {
                                val event = awaitPointerEvent()
                                val change = event.changes.first()
                                if (change.pressed) {
                                    val delta = change.position - previous
                                    session.moveSelected(delta.x / scale, delta.y / scale)
                                    previous = change.position
                                    change.consume()
                                }
                                pressed = event.changes.any { it.pressed }
                            }
                            session.finishTransaction(before)
                        } else {
                            session.select(null)
                            var previous = down.position
                            var currentViewport = viewport
                            var pressed = true
                            while (pressed) {
                                val event = awaitPointerEvent()
                                val change = event.changes.first()
                                if (change.pressed) {
                                    currentViewport += change.position - previous
                                    onViewportChange(currentViewport)
                                    previous = change.position
                                    change.consume()
                                }
                                pressed = event.changes.any { it.pressed }
                            }
                        }
                    }
                }
            }
        },
    ) {
        drawGrid(viewport, scale)
        withTransform({
            translate(viewport.x, viewport.y)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            session.items.forEach { drawBoardItem(it) }
            if (session.previewPoints.size > 1) {
                drawStroke(
                    session.previewPoints,
                    Color(color).copy(alpha = if (tool == CanvasTool.MARKER) 0.42f else 1f),
                    if (tool == CanvasTool.MARKER) 26f else 5.5f,
                )
            }
            session.selectedItem()?.let { drawSelection(it.bounds(), 1f / scale) }
        }
    }
}

private fun screenToBoard(point: Offset, viewport: Offset, scale: Float) =
    BoardPoint((point.x - viewport.x) / scale, (point.y - viewport.y) / scale)

private fun DrawScope.drawGrid(viewport: Offset, scale: Float) {
    val step = 64f * scale
    if (step < 12f) return
    val startX = ((viewport.x % step) + step) % step
    val startY = ((viewport.y % step) + step) % step
    var x = startX
    while (x < size.width) {
        var y = startY
        while (y < size.height) {
            drawCircle(Color(0xFFD3D6CE), radius = max(1.3f, scale * 1.35f), center = Offset(x, y))
            y += step
        }
        x += step
    }
}

private fun DrawScope.drawBoardItem(item: BoardItem) {
    drawStroke(item.points, Color(item.color).copy(alpha = item.opacity), item.width)
}

private fun DrawScope.drawStroke(points: List<BoardPoint>, color: Color, width: Float) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        if (points.size == 2) lineTo(points[1].x, points[1].y)
        else {
            for (index in 1 until points.lastIndex) {
                val current = points[index]
                val next = points[index + 1]
                quadraticTo(current.x, current.y, (current.x + next.x) / 2f, (current.y + next.y) / 2f)
            }
            lineTo(points.last().x, points.last().y)
        }
    }
    drawPath(path, color, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawSelection(bounds: ItemBounds, screenStroke: Float) {
    val padding = 10f
    val rect = Rect(bounds.left - padding, bounds.top - padding, bounds.right + padding, bounds.bottom + padding)
    drawRoundRect(
        color = Accent,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f),
        style = Stroke(max(2f, screenStroke * 1.5f)),
    )
    listOf(rect.topLeft, rect.topRight, rect.bottomLeft, rect.bottomRight).forEach {
        drawCircle(Color.White, radius = 6f + screenStroke, center = it)
        drawCircle(Accent, radius = 6f + screenStroke, center = it, style = Stroke(max(2f, screenStroke)))
    }
}

@Composable
private fun DrawingDock(
    selectedTool: CanvasTool,
    selectedColor: Long,
    onTool: (CanvasTool) -> Unit,
    onColor: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            Modifier.background(Surface.copy(alpha = 0.98f), RoundedCornerShape(28.dp))
                .border(1.dp, Border, RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Palette.forEach { swatch ->
                Box(
                    Modifier.size(44.dp)
                        .semantics { contentDescription = "Choose color"; role = Role.Button }
                        .clickable { onColor(swatch) },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier.size(28.dp)
                            .background(Color(swatch), CircleShape)
                            .then(
                                if (selectedColor == swatch) {
                                    Modifier.border(3.dp, Surface, CircleShape).border(1.dp, Ink, CircleShape)
                                } else Modifier,
                            ),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.background(Surface.copy(alpha = 0.98f), RoundedCornerShape(30.dp))
                .border(1.dp, Border, RoundedCornerShape(30.dp))
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CanvasTool.entries.forEach { tool ->
                ToolButton(tool, selectedTool == tool) { onTool(tool) }
            }
        }
    }
}

@Composable
private fun ToolButton(tool: CanvasTool, selected: Boolean, onClick: () -> Unit) {
    val label = when (tool) {
        CanvasTool.SELECT -> "Move"
        CanvasTool.PEN -> "Pen"
        CanvasTool.MARKER -> "Marker"
        CanvasTool.ERASER -> "Eraser"
    }
    Column(
        Modifier.width(66.dp).height(56.dp)
            .background(if (selected) Brand else Color.Transparent, RoundedCornerShape(20.dp))
            .semantics { contentDescription = label; role = Role.Button }
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ToolGlyph(tool, Ink)
        AppText(label, 11.sp, FontWeight.SemiBold, color = if (selected) Ink else Muted)
    }
}

@Composable
private fun ToolGlyph(tool: CanvasTool, color: Color) {
    Canvas(Modifier.size(24.dp)) {
        val iconScale = size.minDimension / 24f
        withTransform({ scale(iconScale, iconScale, pivot = Offset.Zero) }) {
            val stroke = 2.2f
            when (tool) {
                CanvasTool.SELECT -> {
                    val path = Path().apply {
                        moveTo(6f, 3f); lineTo(19f, 14f); lineTo(13f, 15f); lineTo(10f, 21f); close()
                    }
                    drawPath(path, color, style = Stroke(stroke, join = StrokeJoin.Round))
                }
                CanvasTool.PEN -> {
                    drawLine(color, Offset(7f, 19f), Offset(17f, 5f), stroke * 2f, StrokeCap.Round)
                    drawLine(color, Offset(5f, 21f), Offset(9f, 19f), stroke, StrokeCap.Round)
                }
                CanvasTool.MARKER -> {
                    drawLine(color, Offset(7f, 18f), Offset(17f, 7f), stroke * 4f, StrokeCap.Square)
                    drawLine(color, Offset(5f, 21f), Offset(12f, 21f), stroke * 2f, StrokeCap.Round)
                }
                CanvasTool.ERASER -> {
                    val path = Path().apply {
                        moveTo(5f, 16f); lineTo(13f, 6f); lineTo(20f, 12f); lineTo(13f, 20f); lineTo(8f, 20f); close()
                    }
                    drawPath(path, color, style = Stroke(stroke, join = StrokeJoin.Round))
                }
            }
        }
    }
}

private enum class ActionIcon { BACK, UNDO, REDO, PLUS, MINUS }

@Composable
private fun RoundAction(
    description: String,
    icon: ActionIcon,
    onClick: () -> Unit,
    enabled: Boolean = true,
    flat: Boolean = false,
) {
    val iconColor = if (enabled) Ink else Color(0xFFB9BBC0)
    Box(
        Modifier.size(48.dp)
            .then(
                if (flat) Modifier
                else Modifier.background(Surface.copy(alpha = 0.96f), CircleShape).border(1.dp, Border, CircleShape),
            )
            .semantics { contentDescription = description; role = Role.Button }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(24.dp)) {
            val stroke = 2.2.dp.toPx()
            when (icon) {
                ActionIcon.BACK -> {
                    drawLine(iconColor, Offset(size.width * 0.65f, size.height * 0.2f), Offset(size.width * 0.35f, size.height * 0.5f), stroke, StrokeCap.Round)
                    drawLine(iconColor, Offset(size.width * 0.35f, size.height * 0.5f), Offset(size.width * 0.65f, size.height * 0.8f), stroke, StrokeCap.Round)
                }
                ActionIcon.PLUS -> {
                    drawLine(iconColor, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.8f, size.height * 0.5f), stroke, StrokeCap.Round)
                    drawLine(iconColor, Offset(size.width * 0.5f, size.height * 0.2f), Offset(size.width * 0.5f, size.height * 0.8f), stroke, StrokeCap.Round)
                }
                ActionIcon.MINUS -> drawLine(iconColor, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.8f, size.height * 0.5f), stroke, StrokeCap.Round)
                ActionIcon.UNDO, ActionIcon.REDO -> {
                    val direction = if (icon == ActionIcon.UNDO) -1f else 1f
                    val path = Path().apply {
                        moveTo(size.width * (0.5f - direction * 0.28f), size.height * 0.7f)
                        cubicTo(
                            size.width * (0.5f - direction * 0.2f), size.height * 0.25f,
                            size.width * (0.5f + direction * 0.25f), size.height * 0.3f,
                            size.width * (0.5f + direction * 0.28f), size.height * 0.58f,
                        )
                    }
                    drawPath(path, iconColor, style = Stroke(stroke, cap = StrokeCap.Round))
                    val tip = Offset(size.width * (0.5f - direction * 0.28f), size.height * 0.7f)
                    drawLine(iconColor, tip, Offset(tip.x + direction * size.width * 0.02f, tip.y - size.height * 0.23f), stroke, StrokeCap.Round)
                    drawLine(iconColor, tip, Offset(tip.x + direction * size.width * 0.2f, tip.y - size.height * 0.02f), stroke, StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
private fun PillButton(label: String, onClick: () -> Unit, dark: Boolean = false) {
    Box(
        Modifier.height(48.dp)
            .background(if (dark) Brand else Surface, RoundedCornerShape(20.dp))
            .border(1.dp, if (dark) Brand else Border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick).padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(label, 14.sp, FontWeight.SemiBold, color = Ink)
    }
}

@Composable
private fun SmallAction(label: String, color: Color = Muted, onClick: () -> Unit) {
    Box(
        Modifier.height(48.dp)
            .background(Color(0xFFF1F2ED), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(label, 13.sp, FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun AppText(
    value: String,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight,
    modifier: Modifier = Modifier,
    color: Color = Ink,
) {
    androidx.compose.foundation.text.BasicText(
        value,
        modifier,
        style = TextStyle(color = color, fontSize = size, fontWeight = weight, fontFamily = FontFamily.SansSerif),
    )
}
