package net.heidylazaro.futbolitosensor

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import net.heidylazaro.futbolitosensor.viewmodel.SensorViewModel

@SuppressLint("ResourceAsColor")
@Composable
fun FutbolitoGameScreen(viewModel: SensorViewModel, modifier: Modifier = Modifier) {
    val sensorData = viewModel.accelerometerData
    var (x, y, _) = Triple(sensorData.x, sensorData.y, sensorData.z)

        var topCount by remember { mutableStateOf(0) }
        var bottomCount by remember { mutableStateOf(0) }
        var timeLeft by remember { mutableStateOf(60) }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = with(LocalDensity.current) { 360.dp.toPx() } // valores por defecto
            val height = with(LocalDensity.current) { 640.dp.toPx() }
            val userRadius = with(LocalDensity.current) { 10.dp.toPx() }

            var center by remember { mutableStateOf(Offset(width / 2, height / 2)) }
            val orientation = LocalConfiguration.current.orientation

            // Temporizador
            LaunchedEffect(Unit) {
                while (timeLeft > 0) {
                    delay(1000L)
                    timeLeft--
                }
            }

            val goalHeight = with(LocalDensity.current) { 80.dp.toPx() }
            val goalWidth = width / 5
            val topGoalRect = Offset((width - goalWidth) / 2, 100f) to Offset((width + goalWidth) / 2, goalHeight)
            val bottomGoalRect = Offset((width - goalWidth) / 2, height - goalHeight) to Offset((width + goalWidth) / 2, height - 100)

            if (timeLeft > 0) {
                val newX: Float
                val newY: Float

                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    newX = center.x - x
                    newY = center.y + y
                } else {
                    newX = center.x + y
                    newY = center.y + x
                }

                var tempX = newX
                var tempY = newY

                // Rebote en bordes
                if (tempX <= userRadius || tempX >= width - userRadius) {
                    x = -x
                    tempX = center.x - x
                }
                if (tempY <= userRadius || tempY >= height - userRadius) {
                    y = -y
                    tempY = center.y + y
                }

                center = Offset(
                    x = tempX.coerceIn(userRadius, width - userRadius),
                    y = tempY.coerceIn(userRadius, height - userRadius)
                )

                // Detectar gol
                if (center.y <= topGoalRect.second.y && center.x in topGoalRect.first.x..topGoalRect.second.x) {
                    topCount++
                    center = Offset(width / 2, height / 2)
                } else if (center.y >= bottomGoalRect.first.y && center.x in bottomGoalRect.first.x..bottomGoalRect.second.x) {
                    bottomCount++
                    center = Offset(width / 2, height / 2)
                }
            }

            // Dibujo
            Canvas(modifier = Modifier.fillMaxSize().paint(painterResource(id = R.drawable.campo))) {
                // Portería superior
                drawRect(
                    color = Color.LightGray,
                    topLeft = topGoalRect.first,
                    size = androidx.compose.ui.geometry.Size(
                        topGoalRect.second.x - topGoalRect.first.x,
                        topGoalRect.second.y - topGoalRect.first.y
                    )
                )

                // Jugador usuario (pelota)
                drawCircle(
                    color = Color.White,
                    radius = userRadius,
                    center = center
                )

                // Portería inferior
                drawRect(
                    color = Color.LightGray,
                    topLeft = bottomGoalRect.first,
                    size = androidx.compose.ui.geometry.Size(
                        bottomGoalRect.second.x - bottomGoalRect.first.x,
                        bottomGoalRect.second.y - bottomGoalRect.first.y
                    )
                )
            }

            // Marcadores y tiempo
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
                    .align(Alignment.CenterStart),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$bottomCount",
                    color = Color.Red,
                    fontSize = 20.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$topCount",
                    color = Color.Green,
                    fontSize = 20.sp,
                )
            }

            Text(
                text = "Tiempo: $timeLeft s",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
            )

            if (timeLeft == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.Green, shape = RoundedCornerShape(12.dp))
                        .border(2.dp, Color.White, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                        .align(Alignment.Center)
                ) {
                    Text(
                        text = "¡Fin del juego!",
                        color = Color.White,
                        fontSize = 24.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }


