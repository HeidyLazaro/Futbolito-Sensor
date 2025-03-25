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
    val dampingFactor = 0.8f // Para reducir la velocidad tras cada rebote

    Box(
        modifier = Modifier.fillMaxSize()
            .paint(
                painterResource(id = R.drawable.campo),
                contentScale = ContentScale.FillBounds
            )
    ) {
        val width = with(LocalDensity.current) { 360.dp.toPx() }
        val height = with(LocalDensity.current) { 735.dp.toPx() }
        val userRadius = with(LocalDensity.current) { 10.dp.toPx() }
        val radius = width / 17

        var center by remember { mutableStateOf(Offset(width / 2, height / 2)) }
        val orientation = LocalConfiguration.current.orientation

        val topGoal = Offset((width / 2) - 80, 180f) to Offset((width / 2) + 75, 230f)
        val bottomGoal =
            Offset((width / 2) - 80, height - 100f) to Offset((width / 2) + 75, height - 50f)

        if (timeLeft > 0) {
            // Temporizer
            LaunchedEffect(Unit) {
                while (timeLeft > 0) {
                    delay(1000L)
                    timeLeft--
                }
            }
            velocityX += x * 0.1f
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
                center = Offset(width / 2, height / 2)
                velocityX = 0f
                velocityY = 0f
            } else if (center.y >= bottomGoal.first.y && center.x in bottomGoal.first.x..bottomGoal.second.x) {
                bottomCount++
                center = Offset(width / 2, height / 2)
                velocityX = 0f
                velocityY = 0f
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color.Green, topGoal.first, androidx.compose.ui.geometry.Size(
                topGoal.second.x - topGoal.first.x, topGoal.second.y - topGoal.first.y
            ))
            drawRect(Color.Red, bottomGoal.first, androidx.compose.ui.geometry.Size(
                bottomGoal.second.x - bottomGoal.first.x, bottomGoal.second.y - bottomGoal.first.y
            ))
            drawCircle(Color.White, userRadius, center)
        }
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
        Text(
            text = "Tiempo: $timeLeft s",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
        )
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
                            }
                        )
                    }
            ) {
                Text(
                    text = "¡Fin del juego!",
                    color = Color.Gray,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
