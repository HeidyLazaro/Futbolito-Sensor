package net.heidylazaro.futbolitosensor

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import net.heidylazaro.futbolitosensor.viewmodel.SensorViewModel

@SuppressLint("ResourceAsColor")
@Composable
fun FutbolitoGameScreen(viewModel: SensorViewModel, modifier: Modifier = Modifier) {
    val sensorData = viewModel.accelerometerData
    var (x, y, _) = Triple(sensorData.x, sensorData.y, sensorData.z)

    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }
    var topCount by remember { mutableStateOf(0) }
    var bottomCount by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(60) }

    var prevTopCount by remember { mutableStateOf(0) }
    var prevBottomCount by remember { mutableStateOf(0) }

    val confetti = remember { mutableStateListOf<Triple<Offset, Offset, Color>>() }
    val colors = listOf(Color.Yellow, Color.Magenta, Color.Cyan, Color.Green, Color.Red)

    val dampingFactor = 0.8f

    Box(
        modifier = Modifier.fillMaxSize()
            .paint(
                painterResource(id = R.drawable.campo),
                contentScale = ContentScale.FillBounds
            )
    ) {
        val configuration = LocalConfiguration.current
        val width = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
        val height = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
        val userRadius = width / 30
        val radius = width / 17

        var center by remember { mutableStateOf(Offset(width / 2, height / 2)) }

        val goalWidth = width * 0.4f
        val goalHeight = height * 0.05f

        val topGoal = Offset((width - goalWidth) / 2, goalHeight * 1.2f) to
                Offset((width + goalWidth) / 2, goalHeight * 2.2f)

        val bottomGoal = Offset((width - goalWidth) / 2, height - goalHeight * 0.6f) to
                Offset((width + goalWidth) / 2, height - goalHeight * -0.4f)

        if (timeLeft > 0) {
            LaunchedEffect(Unit) {
                while (timeLeft > 0) {
                    delay(1000L)
                    timeLeft--
                }
            }
            velocityX += x * -0.1f
            velocityY += y * 0.1f
            center = Offset(
                (center.x + velocityX).coerceIn(radius, width - radius),
                (center.y + velocityY).coerceIn(200f, height - radius)
            )

            if (center.x <= radius || center.x >= width - radius) velocityX = -velocityX * dampingFactor
            if (center.y <= 200f || center.y >= height - radius)
                velocityY = -velocityY * dampingFactor

            if (center.y <= topGoal.second.y && center.x in topGoal.first.x..topGoal.second.x) {
                topCount++
                if (topCount > prevTopCount) {
                    repeat(50) {
                        val angle = (0..360).random().toFloat()
                        val speed = (2..6).random()
                        val velocity = Offset(
                            x = speed * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(),
                            y = speed * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
                        )
                        confetti.add(Triple(center, velocity, colors.random()))
                    }
                    prevTopCount = topCount
                }
                center = Offset(width / 2, height / 2)
                velocityX = 0f
                velocityY = 0f
            } else if (center.y >= bottomGoal.first.y && center.x in bottomGoal.first.x..bottomGoal.second.x) {
                bottomCount++
                if (bottomCount > prevBottomCount) {
                    repeat(50) {
                        val angle = (0..360).random().toFloat()
                        val speed = (2..6).random()
                        val velocity = Offset(
                            x = speed * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(),
                            y = speed * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
                        )
                        confetti.add(Triple(center, velocity, colors.random()))
                    }
                    prevBottomCount = bottomCount
                }
                center = Offset(width / 2, height / 2)
                velocityX = 0f
                velocityY = 0f
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Dibujar porterías
            drawRect(Color.Red, topGoal.first, androidx.compose.ui.geometry.Size(
                topGoal.second.x - topGoal.first.x, topGoal.second.y - topGoal.first.y
            ))
            drawRect(Color.Green, bottomGoal.first, androidx.compose.ui.geometry.Size(
                bottomGoal.second.x - bottomGoal.first.x, bottomGoal.second.y - bottomGoal.first.y
            ))

            // Dibujar confeti
            confetti.forEachIndexed { index, (pos, vel, color) ->
                drawCircle(color, radius = 6f, center = pos)
                confetti[index] = Triple(pos + vel, vel.copy(y = vel.y + 0.2f), color) // gravedad
            }

            // Dibujar pelota
            drawCircle(Color.White, userRadius, center)
        }

        // Contador
        Box(
            modifier = Modifier
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .align(Alignment.CenterStart)
                .widthIn(max = 64.dp)
                .heightIn(max = 88.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight().padding(16.dp).align(Alignment.CenterStart),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "$bottomCount", color = Color.Red, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "$topCount", color = Color.Green, fontSize = 20.sp)
            }
        }

        // Temporizador
        Text(
            text = "Tiempo: $timeLeft s",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
        )

        // Mensaje final
        if (timeLeft == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Yellow, shape = RoundedCornerShape(12.dp))
                    .border(2.dp, Color.LightGray, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
                    .align(Alignment.Center)
                    .pointerInput(Unit) {
                        detectTapGestures (
                            onTap = {
                                timeLeft = 60
                                topCount = 0
                                bottomCount = 0
                                prevTopCount = 0
                                prevBottomCount = 0
                                confetti.clear()
                            }
                        )
                    }
            ) {
                Text(
                    text = if (topCount > bottomCount) {
                        "¡Fin del juego!\n¡Jugador verde gana!"
                    } else if (bottomCount > topCount) {
                        "¡Fin del juego!\n¡Jugador rojo gana!"
                    } else {
                        "¡Fin del juego!\n¡Empate!"
                    },
                    color = Color.Gray,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
