package com.cipherxzc.whatsnext.ui.main.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.data.database.TodoItemInfo
import com.cipherxzc.whatsnext.ui.main.utils.TodoItemPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    todoItems: List<TodoItem>,
) {
    val previewItem = remember { mutableStateOf<TodoItemInfo?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "重要✖️紧急四象限")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
                )
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            TodoQuadrantChart(
                items = todoItems,
                onItemClick = { previewItem.value = it.toInfo() }
            )
        }

        previewItem.value?.let { item ->
            TodoItemPreview(
                item = item,
                onDismiss = { previewItem.value = null }
            )
        }
    }
}

@Composable
fun TodoQuadrantChart(
    items: List<TodoItem>,
    maxDays: Int = 10,
    urgentDays: Int = 3,
    importanceCut: Int = 5,
    onItemClick: (TodoItem) -> Unit
) {
    val density = LocalDensity.current
    val touchPoints = remember { mutableStateListOf<Pair<Offset, TodoItem>>() }
    val touchPointRadius = with(density) { 12.dp.toPx() }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .padding(16.dp)
            .pointerInput(items) {
                detectTapGestures { tapOffset ->
                    // 遍历每个点，判断点击是否落在圆形内
                    touchPoints.find { (center, _) ->
                        (tapOffset - center).getDistance() <= touchPointRadius
                    }?.let { (_, item) ->
                        onItemClick(item)
                    }
                }
            }
    ) {
        // 画笔
        val axisStroke = Stroke(width = 3.dp.toPx())
        val dashStroke = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
        )

        // 预留四周文字空间
        val leftPad   = 32.dp.toPx()
        val bottomPad = 32.dp.toPx()
        val topPad    = 16.dp.toPx()
        val rightPad  = 16.dp.toPx()

        val plotW = size.width  - leftPad - rightPad
        val plotH = size.height - topPad  - bottomPad
        val originX = leftPad
        val originY = size.height - bottomPad

        // 坐标转换 λ
        fun xPos(days: Float) =
            originX + ((days.coerceIn(0f, maxDays.toFloat()) + 1f) / (maxDays + 2)) * plotW

        fun yPos(importance: Float) =
            originY - ((importance.coerceIn(0f, 10f) + 1f) / 12f) * plotH

        // 主轴
        drawLine(
            color = Color.Black,
            start = Offset(originX, originY),
            end   = Offset(originX, topPad),      // Y 轴
            strokeWidth = axisStroke.width
        )
        drawLine(
            color = Color.Black,
            start = Offset(originX, originY),
            end   = Offset(originX + plotW, originY), // X 轴
            strokeWidth = axisStroke.width
        )
        // 轴箭头
        drawLine(
            color = Color.Black,
            start = Offset(originX, topPad),
            end   = Offset(originX - 8.dp.toPx(), topPad + 12.dp.toPx()),
            strokeWidth = axisStroke.width
        )
        drawLine(
            color = Color.Black,
            start = Offset(originX, topPad),
            end   = Offset(originX + 8.dp.toPx(), topPad + 12.dp.toPx()),
            strokeWidth = axisStroke.width
        )
        drawLine(
            color = Color.Black,
            start = Offset(originX + plotW, originY),
            end   = Offset(originX + plotW - 12.dp.toPx(), originY - 8.dp.toPx()),
            strokeWidth = axisStroke.width
        )
        drawLine(
            color = Color.Black,
            start = Offset(originX + plotW, originY),
            end   = Offset(originX + plotW - 12.dp.toPx(), originY + 8.dp.toPx()),
            strokeWidth = axisStroke.width
        )

        // 四象限分割线
        val midV = xPos(urgentDays.toFloat())
        val midH = yPos(importanceCut.toFloat())
        drawLine(
            color = Color.Gray,
            strokeWidth = dashStroke.width,
            pathEffect = dashStroke.pathEffect,
            start = Offset(midV, originY),
            end   = Offset(midV, topPad)
        )
        drawLine(
            color = Color.Gray,
            strokeWidth = dashStroke.width,
            pathEffect = dashStroke.pathEffect,
            start = Offset(originX, midH),
            end   = Offset(originX + plotW, midH)
        )

        // 标注文字
        drawContext.canvas.nativeCanvas.apply {
            val textPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 14.sp.toPx()
                color = android.graphics.Color.BLACK
            }
            // 轴 Label
            drawText("Important", originX - 24.dp.toPx(), topPad - 4.dp.toPx(), textPaint)
            drawText("Not urgent", originX + plotW - 50.dp.toPx(), originY + 20.dp.toPx(), textPaint)

            // 象限提示（可根据需求自定义）
            textPaint.textSize = 12.sp.toPx()
            textPaint.color = android.graphics.Color.BLUE
            drawText("Do Later", originX + 4.dp.toPx(), midH + 12.dp.toPx(), textPaint)

            textPaint.color = android.graphics.Color.rgb(255, 165, 0) // orange
            drawText("Important not urgent", midV + 4.dp.toPx(), topPad + 24.dp.toPx(), textPaint)

            textPaint.color = android.graphics.Color.rgb(0, 128, 0) // green
            drawText("Low Priority", midV + 4.dp.toPx(), midH + 12.dp.toPx(), textPaint)

            textPaint.color = android.graphics.Color.RED
            drawText("Do Now", originX + 4.dp.toPx(), topPad + 24.dp.toPx(), textPaint)
        }

        /* ---------- 3. 绘制任务节点 ---------- */
        val pointRadius = 6.dp.toPx()

        items.forEach { item ->
            // 重要度
            val imp = (item.importance ?: importanceCut).toFloat()

            // 距截止日的天数；null 视为 maxDays
            val now = System.currentTimeMillis()
            val dueMillis = item.dueDate?.toDate()?.time ?: (now + maxDays * 86_400_000L)
            val diffDays = ((dueMillis - now).coerceAtLeast(0) / 86_400_000L).toFloat()

            // 坐标
            val px = xPos(diffDays)
            val py = yPos(imp)
            val center = Offset(px, py)

            // 存储点击点
            touchPoints.add(center to item)

            // 按象限分色
            val color = when {
                imp >= importanceCut && diffDays <= urgentDays -> Color.Red
                imp >= importanceCut && diffDays >  urgentDays -> Color(0xFFFFA500) // Orange
                imp <  importanceCut && diffDays <= urgentDays -> Color.Blue
                else                                           -> Color(0xFF4CAF50) // Green
            }

            // 绘制
            drawCircle(color = color, radius = pointRadius, center = Offset(px, py))

            // title
            drawContext.canvas.nativeCanvas.apply {
                val titlePaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textSize = 12.sp.toPx()
                    //color = android.graphics.Color.DKGRAY
                }
                drawText(
                    item.title.take(15),   // 截断长标题
                    px + pointRadius + 4.dp.toPx(),
                    py - pointRadius,
                    titlePaint
                )
            }
        }
    }
}