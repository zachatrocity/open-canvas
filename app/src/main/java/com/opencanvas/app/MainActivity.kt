package com.opencanvas.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        val appState = OpenCanvasState(BoardRepository(this))
        setContent { OpenCanvasApp(appState) }
    }
}

class BoardRepository(context: Context) {
    private val preferences = context.getSharedPreferences("open_canvas_boards", Context.MODE_PRIVATE)

    fun load(): List<BoardDocument> = runCatching {
        BoardCodec.decode(preferences.getString("boards", "").orEmpty())
    }.getOrDefault(emptyList())

    fun save(boards: List<BoardDocument>) {
        preferences.edit().putString("boards", BoardCodec.encode(boards)).apply()
    }
}

class OpenCanvasState(private val repository: BoardRepository) {
    val boards = mutableStateListOf<BoardDocument>()
    var openBoardId by mutableStateOf<String?>(null)
        private set

    init {
        val loaded = repository.load()
        val stored = loaded.map { board ->
            if (board.id == "welcome-board" && board.title == "Solar energy notes") welcomeBoard() else board
        }
        boards += if (stored.isEmpty()) listOf(welcomeBoard()) else stored
        openBoardId = boards.firstOrNull()?.id
        if (stored.isEmpty() || stored != loaded) repository.save(boards)
    }

    fun openBoard(id: String) { openBoardId = id }
    fun showLibrary() { openBoardId = null }

    fun createBoard() {
        val board = BoardDocument(title = "Untitled drawing")
        boards.add(0, board)
        persist()
        openBoardId = board.id
    }

    fun updateBoard(board: BoardDocument) {
        val index = boards.indexOfFirst { it.id == board.id }
        if (index >= 0) boards[index] = board.copy(updatedAt = System.currentTimeMillis())
        persist()
    }

    fun duplicateBoard(id: String) {
        val source = boards.firstOrNull { it.id == id } ?: return
        val copy = source.copy(
            id = java.util.UUID.randomUUID().toString(),
            title = "${source.title} copy",
            updatedAt = System.currentTimeMillis(),
        )
        boards.add(0, copy)
        persist()
    }

    fun deleteBoard(id: String) {
        boards.removeAll { it.id == id }
        if (openBoardId == id) openBoardId = null
        persist()
    }

    private fun persist() = repository.save(boards)
}
