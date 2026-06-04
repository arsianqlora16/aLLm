package com.lo.allm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lo.allm.detector.DeviceSpecsDetector

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AllMTheme {
                SpecsScreen()
            }
        }
    }
}

@Composable
fun SpecsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val detector = remember { DeviceSpecsDetector(context) }
    var specs by remember { mutableStateOf("Lagi deteksi spek HP lo...") }

    LaunchedEffect(Unit) {
        specs = detector.getFullSpecs()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AllM - Spek Detector",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = specs,
                    modifier = Modifier.padding(16.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = { specs = detector.getFullSpecs() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Deteksi Ulang")
            }
        }
    }
}

@Composable
fun AllMTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF4CAF50),
            secondary = Color(0xFF2196F3),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onSurface = Color.White
        ),
        content = content
    )
}
