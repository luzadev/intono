package com.notemusicali.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

@Composable
fun LineChart(
    data: List<Float>,
    labels: List<String> = emptyList(),
    lineColor: Color = Color(0xFF90CAF9),
    fillColor: Color = lineColor.copy(alpha = 0.15f),
    modifier: Modifier = Modifier,
    yAxisLabel: String = "",
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val paddingLeft = 40f
        val paddingBottom = 30f
        val paddingTop = 16f
        val paddingRight = 16f
        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        val maxVal = data.max().coerceAtLeast(1f)
        val minVal = 0f

        // Draw grid lines
        val gridColor = Color.White.copy(alpha = 0.1f)
        for (i in 0..4) {
            val y = paddingTop + chartHeight * (1f - i / 4f)
            drawLine(gridColor, Offset(paddingLeft, y), Offset(size.width - paddingRight, y))

            val labelVal = (minVal + (maxVal - minVal) * i / 4f).toInt().toString()
            val result = textMeasurer.measure(
                text = labelVal,
                style = TextStyle(fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f)),
            )
            drawText(result, topLeft = Offset(paddingLeft - result.size.width - 4f, y - result.size.height / 2f))
        }

        // Draw data points and line
        if (data.size == 1) {
            val x = paddingLeft + chartWidth / 2
            val y = paddingTop + chartHeight * (1f - (data[0] - minVal) / (maxVal - minVal))
            drawCircle(lineColor, radius = 4f, center = Offset(x, y))
            return@Canvas
        }

        val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)
        val points = data.mapIndexed { index, value ->
            val x = paddingLeft + stepX * index
            val normalized = ((value - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
            val y = paddingTop + chartHeight * (1f - normalized)
            Offset(x, y)
        }

        // Fill area
        val fillPath = Path().apply {
            moveTo(points.first().x, paddingTop + chartHeight)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, paddingTop + chartHeight)
            close()
        }
        drawPath(fillPath, fillColor, style = Fill)

        // Line
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(linePath, lineColor, style = Stroke(width = 2f))

        // Points
        points.forEach { point ->
            drawCircle(lineColor, radius = 3f, center = point)
        }

        // X-axis labels
        if (labels.isNotEmpty()) {
            val labelStep = maxOf(1, labels.size / 6)
            labels.forEachIndexed { index, label ->
                if (index % labelStep == 0 || index == labels.lastIndex) {
                    val x = paddingLeft + stepX * index
                    val result = textMeasurer.measure(
                        text = label,
                        style = TextStyle(fontSize = 8.sp, color = Color.White.copy(alpha = 0.4f)),
                    )
                    drawText(result, topLeft = Offset(x - result.size.width / 2f, paddingTop + chartHeight + 4f))
                }
            }
        }

        // Y-axis label
        if (yAxisLabel.isNotEmpty()) {
            val result = textMeasurer.measure(
                text = yAxisLabel,
                style = TextStyle(fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f)),
            )
            drawText(result, topLeft = Offset(0f, paddingTop - result.size.height - 2f))
        }
    }
}
