package hu.szujo.hucallblocker

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.min

// Decorative examples only: never connected to call screening, contacts or the log.
private val samplePrefixes = listOf("+44 7•••", "+49 15•••", "+33 6•••", "+43 66•••")
private const val CYCLE_MS = 3200
private const val HIT = 0.58f
private const val END = 0.86f

/**
 * One masked international example approaches the shield at a time.
 * At impact it is deflected, fades away and emits a brief ripple and block mark.
 * Animation state is read only in the draw phase, not during screen composition.
 * The caller composes this only while protection is active.
 */
@Composable
internal fun CallDefenseAnimation(logoSize: Dp, modifier: Modifier = Modifier) {
    val clock = rememberInfiniteTransition(label = "call-defense")
    val phase = clock.animateFloat(
        initialValue = 0f,
        targetValue = samplePrefixes.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(CYCLE_MS * samplePrefixes.size, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "one-call-at-a-time"
    )
    val paint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            color = android.graphics.Color.rgb(0, 92, 69)
        }
    }
    Canvas(modifier) {
        val position = phase.value
        val index = position.toInt() % samplePrefixes.size
        val t = position - position.toInt()
        if (t <= 0f || t >= END || size.width <= 0f || size.height <= 0f) return@Canvas

        val text = samplePrefixes[index]
        val side = if (index % 2 == 0) -1f else 1f
        val vertical = if (index < 2) -1f else 1f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val shieldRadius = logoSize.toPx() * 0.37f
        paint.textSize = min(10.sp.toPx(), 13.dp.toPx())
        val halfText = paint.measureText(text) / 2f
        val available = (cx - halfText - 3.dp.toPx()).coerceAtLeast(0f)
        val hitDistance = (shieldRadius + halfText + 4.dp.toPx()).coerceAtMost(available)
        val startDistance = min(available, hitDistance + 120.dp.toPx())
        val impact = Offset(cx + side * shieldRadius, cy + vertical * shieldRadius * 0.28f)
        val hitY = cy + vertical * shieldRadius * 0.28f

        val retreat = ((t - HIT) / (END - HIT)).coerceIn(0f, 1f)
        val approach = (t / HIT).coerceIn(0f, 1f)
        val distance = if (t < HIT) {
            startDistance + (hitDistance - startDistance) * approach
        } else {
            hitDistance + (startDistance - hitDistance) * retreat
        }
        val y = if (t < HIT) {
            hitY + vertical * size.height * 0.17f * (1f - approach)
        } else {
            hitY - vertical * size.height * 0.21f * retreat
        }
        val alpha = if (t < HIT) {
            (t / 0.12f).coerceIn(0f, 1f) * 0.54f
        } else {
            (1f - retreat) * 0.54f
        }

        clipRect {
            // Keep all text within the decorative lane and away from the real status labels.
            val textX = cx + side * distance
            if (available >= 1f && abs(textX - cx) >= halfText) {
                paint.alpha = (alpha * 255f).toInt().coerceIn(0, 255)
                val baseline = y - (paint.ascent() + paint.descent()) / 2f
                drawContext.canvas.nativeCanvas.drawText(text, textX, baseline, paint)
            }
            if (t >= HIT) {
                val fade = 1f - retreat
                drawCircle(
                    color = ShieldColors.heroInk.copy(alpha = 0.30f * fade),
                    radius = shieldRadius + 6.dp.toPx() + retreat * 18.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.6.dp.toPx())
                )
                val r = 6.dp.toPx()
                val point = Offset(impact.x + side * 8.dp.toPx(), impact.y)
                val color = ShieldColors.heroInk.copy(alpha = 0.58f * fade)
                drawCircle(color, radius = r, center = point, style = Stroke(1.5.dp.toPx()))
                drawLine(
                    color, point + Offset(-r * 0.65f, r * 0.65f),
                    point + Offset(r * 0.65f, -r * 0.65f),
                    strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round
                )
            }
        }
    }
}
