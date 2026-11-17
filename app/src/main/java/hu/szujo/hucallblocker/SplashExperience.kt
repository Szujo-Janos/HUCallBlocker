package hu.szujo.hucallblocker

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val splashNavy = Color(0xFF051721)
private val splashNavyLight = Color(0xFF0C3038)
private val mint = Color(0xFF17E5BC)
private val cyan = Color(0xFF8BECDD)

/**
 * A rendszer rövid, gyártófüggő splash képe után megjelenő valódi
 * Compose animáció. Nem indít szolgáltatást, nem érinti a hívásszűrőt.
 */
@Composable
internal fun SplashExperience(onFinished: () -> Unit) {
    val screenOpacity = remember { Animatable(1f) }
    val logoOpacity = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.72f) }
    val ringOpacity = remember { Animatable(0f) }
    val titleOpacity = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(22f) }
    val trackFill = remember { Animatable(0f) }

    val repeat = rememberInfiniteTransition(label = "orbitalMotion")
    val ringRotation by repeat.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitRotation"
    )
    val breathe by repeat.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoBreathing"
    )
    val glow by repeat.animateFloat(
        initialValue = 0.48f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloGlow"
    )

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { ringOpacity.animateTo(1f, animationSpec = tween(620)) }
            launch { logoOpacity.animateTo(1f, animationSpec = tween(420)) }
            launch {
                logoScale.animateTo(
                    1f, animationSpec = tween(730, easing = FastOutSlowInEasing)
                )
            }
            launch {
                delay(340)
                titleOpacity.animateTo(1f, animationSpec = tween(480))
            }
            launch {
                delay(340)
                titleOffset.animateTo(
                    0f, animationSpec = tween(510, easing = FastOutSlowInEasing)
                )
            }
            launch {
                delay(600)
                trackFill.animateTo(1f, animationSpec = tween(1190, easing = FastOutSlowInEasing))
            }
            delay(2050)
            screenOpacity.animateTo(0f, animationSpec = tween(270))
        }
        onFinished()
    }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = screenOpacity.value }
            .background(
                Brush.verticalGradient(
                    listOf(splashNavy, Color(0xFF08222D), splashNavyLight, splashNavy)
                )
            )
    ) {
        // Lágy háttérglória – nem raszterkép, minden kijelzőn éles.
        Box(
            Modifier
                .align(Alignment.Center)
                .size(340.dp)
                .graphicsLayer { alpha = glow }
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0x4226CFAE),
                            Color(0x201A8E88),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier.align(Alignment.Center).padding(bottom = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(290.dp), contentAlignment = Alignment.Center) {
                Canvas(
                    Modifier.fillMaxSize().graphicsLayer {
                        alpha = ringOpacity.value
                        rotationZ = ringRotation
                    }
                ) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val outside = size.minDimension * 0.45f
                    val inner = outside * 0.73f
                    drawCircle(
                        color = cyan.copy(alpha = 0.12f),
                        radius = outside,
                        center = c,
                        style = Stroke(width = 1.1.dp.toPx())
                    )
                    drawCircle(
                        color = cyan.copy(alpha = 0.12f),
                        radius = inner,
                        center = c,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawArc(
                        color = mint.copy(alpha = 0.94f),
                        startAngle = -110f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(c.x - outside, c.y - outside),
                        size = Size(outside * 2f, outside * 2f),
                        style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = cyan.copy(alpha = 0.84f),
                        startAngle = 88f,
                        sweepAngle = 59f,
                        useCenter = false,
                        topLeft = Offset(c.x - inner, c.y - inner),
                        size = Size(inner * 2f, inner * 2f),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawCircle(
                        color = mint,
                        radius = 4.5.dp.toPx(),
                        center = Offset(c.x + outside, c.y)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = 2.6.dp.toPx(),
                        center = Offset(c.x - inner, c.y)
                    )
                }

                // Koncentrikus HUD-karika és a HU-s vektorpajzs.
                Box(
                    Modifier
                        .size(190.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0x4217E5BC),
                                    Color(0x1417E5BC),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_shield),
                        contentDescription = "HU Call Blocker",
                        modifier = Modifier
                            .size(114.dp)
                            .graphicsLayer {
                                alpha = logoOpacity.value
                                scaleX = logoScale.value * breathe
                                scaleY = logoScale.value * breathe
                            }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = titleOpacity.value
                    translationY = titleOffset.value.dp.toPx()
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HU CALL BLOCKER",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.6.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "KÜLFÖLDI HÍVÁSOK SZŰRÉSE",
                    color = Color(0xFF9BC6C9),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Vizuális, rövid fénycsík. Nem állít valós hálózati/betöltési folyamatot.
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 66.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.width(165.dp).height(3.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
            ) {
                Box(
                    Modifier.fillMaxHeight()
                        .fillMaxWidth(trackFill.value)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF157E78), mint, Color(0xFFBBFFF2))
                            ),
                            CircleShape
                        )
                )
            }
        }
    }
}
