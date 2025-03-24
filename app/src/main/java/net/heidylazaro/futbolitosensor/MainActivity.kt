package net.heidylazaro.futbolitosensor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import net.heidylazaro.futbolitosensor.viewmodel.SensorViewModel
import net.heidylazaro.futbolitosensor.ui.theme.FutbolitoSensorTheme
import net.heidylazaro.futbolitosensor.FutbolitoGameScreen

class MainActivity : ComponentActivity() {
    private val sensorViewModel: SensorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FutbolitoSensorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FutbolitoGameScreen(
                        viewModel = sensorViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

            }
        }
    }
}

