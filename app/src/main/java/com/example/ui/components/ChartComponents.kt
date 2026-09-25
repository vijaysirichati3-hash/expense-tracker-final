package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.CategorySpend
import com.example.ui.viewmodel.DailySpend
import com.example.ui.viewmodel.ExpenseViewModel

@Composable
fun SpendingDonutChart(
    breakdown: List<CategorySpend>,
    totalExpense: Double,
    modifier: Modifier = Modifier,
    chartSize: Dp = 190.dp,
    strokeWidth: Dp = 26.dp
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(breakdown, totalExpense) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        if (breakdown.isEmpty() || totalExpense == 0.0) {
            Canvas(modifier = Modifier.size(chartSize)) {
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    radius = (size.minDimension - strokeWidth.toPx()) / 2,
                    style = Stroke(width = strokeWidth.toPx())
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Canvas(modifier = Modifier.size(chartSize)) {
                val canvasStroke = strokeWidth.toPx()
                val radius = (size.minDimension - canvasStroke) / 2
                val arcSize = Size(radius * 2, radius * 2)
                val topLeft = Offset(
                    (size.width - arcSize.width) / 2,
                    (size.height - arcSize.height) / 2
                )

                var currentStartAngle = -90f
                val spacingAngle = if (breakdown.size > 1) 2.5f else 0f

                breakdown.forEach { item ->
                    val sweepAngle = (item.percentage * 360f - spacingAngle) * animationProgress.value
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = item.category.color,
                            startAngle = currentStartAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = canvasStroke, cap = StrokeCap.Round)
                        )
                        currentStartAngle += (item.percentage * 360f) * animationProgress.value
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ExpenseViewModel.formatCurrency(totalExpense),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun WeeklySpendBarChart(
    dailyData: List<DailySpend>,
    modifier: Modifier = Modifier
) {
    if (dailyData.isEmpty()) return

    val maxAmount = remember(dailyData) {
        val max = dailyData.maxOfOrNull { it.amount } ?: 100.0
        if (max <= 0.0) 100.0 else max
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            dailyData.forEach { day ->
                val ratio = (day.amount / maxAmount).toFloat().coerceIn(0.04f, 1f)
                val isMax = day.amount == maxAmount && day.amount > 0

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (day.amount > 0) {
                        Text(
                            text = "$${day.amount.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = if (isMax) FontWeight.Bold else FontWeight.Normal,
                            color = if (isMax) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(84.dp)
                            .background(trackColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((84 * ratio).dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = if (isMax) {
                                            listOf(primaryColor, primaryColor.copy(alpha = 0.7f))
                                        } else {
                                            listOf(
                                                MaterialTheme.colorScheme.secondary,
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                            )
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = if (day.dayLabel == "Today") FontWeight.Bold else FontWeight.Normal,
                        color = if (day.dayLabel == "Today") primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val barColor = when {
        progress > 1f -> ExpenseRed
        progress > 0.85f -> Color(0xFFF59E0B) // Amber warning
        else -> IncomeGreen
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(height / 2)),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clampedProgress)
                .height(height)
                .background(barColor, RoundedCornerShape(height / 2))
        )
    }
}
