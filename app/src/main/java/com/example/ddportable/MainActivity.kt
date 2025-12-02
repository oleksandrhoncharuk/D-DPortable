package com.example.ddportable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ddportable.sensor.ShakeDetector
import com.example.ddportable.ui.theme.DDPortableTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DDPortableTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DiceRoll()
                }
            }
        }
    }
}

@Composable
fun DiceRoll() {
    val context = LocalContext.current
    val isShaking = remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }
    val rotationSpeed = remember { mutableStateOf(0f) }
    var shakeResetJob by remember { mutableStateOf<Job?>(null) }
    val diceNumber = remember { mutableIntStateOf(1) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val detector = ShakeDetector(context) {
            isShaking.value = true

            shakeResetJob?.cancel()
            shakeResetJob = coroutineScope.launch {
                delay(500)
                isShaking.value = false
            }
        }
        detector.start()
        onDispose { detector.stop() }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (isShaking.value) {
                rotationSpeed.value = min(rotationSpeed.value + 4f, 50f)
            } else {
                rotationSpeed.value = max(rotationSpeed.value - 1f, 0f)
            }

            rotation.snapTo(rotation.value + rotationSpeed.value)

            if (rotationSpeed.value == 0f) {
                diceNumber.intValue = (1..6).random()
            }

            delay(16)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier
                .size(150.dp)
                .graphicsLayer {
                    rotationY = rotation.value % 360
                },
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Dice ${diceNumber.value}",
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DDPortableTheme {
        Greeting("Android")
    }
}