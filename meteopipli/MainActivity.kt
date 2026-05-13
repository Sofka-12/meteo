package com.example.meteopipli

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meteopipli.R
import com.example.meteopipli.domain.model.RiskLevel
import com.example.meteopipli.domain.model.UserProfile
import com.example.meteopipli.presentation.components.GlowRing
import com.example.meteopipli.presentation.components.GradientButton
import com.example.meteopipli.presentation.theme.MeteopipliTheme
import com.example.meteopipli.presentation.ui.DiaryScreen
import com.example.meteopipli.presentation.ui.ProfileScreen
import com.example.meteopipli.presentation.ui.SettingsScreen
import com.example.meteopipli.presentation.viewmodel.HomeViewModel
import com.example.meteopipli.presentation.viewmodel.HomeViewModelFactory
import com.example.meteopipli.presentation.viewmodel.ProfileViewModel
import com.example.meteopipli.presentation.viewmodel.ProfileViewModelFactory
import com.example.meteopipli.utils.NotificationHelper
import com.example.meteopipli.utils.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) NotificationHelper.createNotificationChannel(this)
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                NotificationHelper.createNotificationChannel(this)
            }
        } else {
            NotificationHelper.createNotificationChannel(this)
        }

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            val settingsDataStore = SettingsDataStore(this)

            LaunchedEffect(Unit) {
                isDarkTheme = settingsDataStore.isDarkThemeFlow.first()
            }

            DisposableEffect(Unit) {
                val job = scope.launch {
                    settingsDataStore.isDarkThemeFlow.collect { newValue ->
                        isDarkTheme = newValue
                    }
                }
                onDispose { job.cancel() }
            }

            MeteopipliTheme(darkTheme = isDarkTheme) {
                AppNavigation(isDarkTheme = isDarkTheme)
            }
        }
    }
}

@Composable
fun AppNavigation(isDarkTheme: Boolean) {
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current.applicationContext as Application))
    val profile by profileViewModel.profile.collectAsState()
    var showProfile by remember { mutableStateOf(!profile.isFilled) }

    if (showProfile) {
        ProfileScreen(onProfileSaved = { showProfile = false })
    } else {
        HomeScreen(
            onEditProfile = { showProfile = true },
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
fun getCardColor(): Color {
    val isDarkTheme = MaterialTheme.colorScheme.primary == Color(0xFF8E7DFF)
    // Усиленная прозрачность: 60% непрозрачности (40% просвечивает)
    return if (isDarkTheme) Color(0xFF2D2F45).copy(alpha = 0.6f)
    else Color.White.copy(alpha = 0.6f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEditProfile: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current.applicationContext as Application))
) {
    var showDiary by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }

    val riskLevel by viewModel.riskLevel.collectAsState()
    val riskReason by viewModel.riskReason.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val pressure by viewModel.pressure.collectAsState()
    val humidity by viewModel.humidity.collectAsState()
    val kpIndex by viewModel.kpIndex.collectAsState()
    val personalRisk by viewModel.personalRisk.collectAsState()
    val pressureChartData by viewModel.pressureChartData.collectAsState()
    val temperatureChartData by viewModel.temperatureChartData.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val textColor = if (isDarkTheme) Color.White else Color.Black

    var showTipsDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadWeather()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showDiary) {
        DiaryScreen(
            onBack = { showDiary = false },
            currentTemperature = temperature,
            currentPressure = pressure,
            currentHumidity = humidity,
            currentKpIndex = kpIndex
        )
    } else if (showNotificationSettings) {
        SettingsScreen(onBack = { showNotificationSettings = false })
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Метео-помощник") },
                    actions = {
                        IconButton(onClick = { showNotificationSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Настройки")
                        }
                        IconButton(onClick = onEditProfile) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать профиль")
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Фоновое изображение
                Image(
                    painter = painterResource(
                        if (isDarkTheme) R.drawable.sky_dark else R.drawable.sky_light
                    ),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Полупрозрачный слой для лучшей читаемости
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isDarkTheme) Color.Black.copy(alpha = 0.4f)
                            else Color.White.copy(alpha = 0.2f)
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))

                    GlowRing(level = riskLevel.name)
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = when (riskLevel) {
                            RiskLevel.GREEN -> "Всё спокойно"
                            RiskLevel.YELLOW -> "Умеренный риск"
                            RiskLevel.RED -> "Высокий риск"
                        },
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = riskReason, fontSize = 16.sp, color = textColor)
                    Spacer(Modifier.height(16.dp))

                    // Карточка текущих показателей (полупрозрачная)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = getCardColor()),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
                            Text("Текущие показатели", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Thermostat, null, tint = when {
                                        temperature > 30 -> Color.Red
                                        temperature < -10 -> Color(0xFF00BFFF)
                                        else -> Color(0xFF5B8C5A)
                                    })
                                    Spacer(Modifier.width(8.dp))
                                    Text("Температура:", fontSize = 16.sp)
                                }
                                Text("${temperature}°C", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, null, tint = when {
                                        pressure < 740 -> Color(0xFFFFA500)
                                        pressure > 780 -> Color.Red
                                        else -> Color(0xFF5B8C5A)
                                    })
                                    Spacer(Modifier.width(8.dp))
                                    Text("Давление:", fontSize = 16.sp)
                                }
                                Text("${pressure.toInt()} мм рт.ст.", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WaterDrop, null, tint = if (humidity > 75) Color(0xFFFFA500) else Color(0xFF5B8C5A))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Влажность:", fontSize = 16.sp)
                                }
                                Text("$humidity%", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bolt, null, tint = when {
                                        kpIndex >= 5 -> Color.Red
                                        kpIndex >= 3 -> Color(0xFFFFA500)
                                        else -> Color(0xFF5B8C5A)
                                    })
                                    Spacer(Modifier.width(8.dp))
                                    Text("Kp-индекс:", fontSize = 16.sp)
                                }
                                Text(
                                    if (kpIndex > 0) "$kpIndex (${kpDescription(kpIndex)})" else "Нет данных",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (kpIndex >= 5) Color.Red else Color.Unspecified
                                )
                            }
                        }
                    }

                    // График давления
                    if (pressureChartData.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = getCardColor())
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("📉 Давление (мм рт. ст.) на ближайшие 12 часов", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Spacer(Modifier.height(8.dp))
                                Canvas(Modifier.fillMaxWidth().height(120.dp)) {
                                    val data = pressureChartData
                                    if (data.size < 2) return@Canvas
                                    val minPressure = data.minOrNull() ?: return@Canvas
                                    val maxPressure = data.maxOrNull() ?: return@Canvas
                                    val range = (maxPressure - minPressure).coerceAtLeast(1.0)
                                    val stepX = size.width / (data.size - 1)
                                    val points = data.mapIndexed { i, value ->
                                        Offset(i * stepX, size.height - ((value - minPressure) / range * size.height).toFloat())
                                    }
                                    val path = Path().apply {
                                        moveTo(points.first().x, points.first().y)
                                        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                                    }
                                    drawPath(path, color = Color(0xFF5B8C5A), style = Stroke(width = 3f))
                                    points.forEach { drawCircle(color = Color(0xFF5B8C5A), radius = 4f, center = it) }
                                }
                            }
                        }
                    }

                    // График температуры
                    if (temperatureChartData.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = getCardColor())
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("📈 Температура (°C) на ближайшие 12 часов", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Spacer(Modifier.height(8.dp))
                                Canvas(Modifier.fillMaxWidth().height(120.dp)) {
                                    val data = temperatureChartData
                                    if (data.size < 2) return@Canvas
                                    val minTemp = data.minOrNull() ?: return@Canvas
                                    val maxTemp = data.maxOrNull() ?: return@Canvas
                                    val range = (maxTemp - minTemp).coerceAtLeast(1.0)
                                    val stepX = size.width / (data.size - 1)
                                    val points = data.mapIndexed { i, value ->
                                        Offset(i * stepX, size.height - ((value - minTemp) / range * size.height).toFloat())
                                    }
                                    val path = Path().apply {
                                        moveTo(points.first().x, points.first().y)
                                        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                                    }
                                    drawPath(path, color = Color(0xFFE88873), style = Stroke(width = 3f))
                                    points.forEach { drawCircle(color = Color(0xFFE88873), radius = 4f, center = it) }
                                }
                            }
                        }
                    }

                    // ML-блок
                    if (personalRisk > 0.6 && kpIndex > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = getCardColor())
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⚠️ Ваш персональный риск выше среднего", fontWeight = FontWeight.Bold)
                                Text("На основе вашего дневника, сегодня возможен дискомфорт. Будьте внимательны к себе.", fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    GradientButton(
                        onClick = { showTipsDialog = true },
                        text = "Что делать",
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF6C5CE7), Color(0xFF8E7DFF)))
                    )
                    Spacer(Modifier.height(12.dp))
                    GradientButton(
                        onClick = { showDiary = true },
                        text = "Мой дневник",
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF00B894), Color(0xFF55EFC4)))
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    if (showTipsDialog) {
        AlertDialog(
            onDismissRequest = { showTipsDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Рекомендации на сегодня", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(getTipMessage(pressure, temperature, humidity, riskLevel, kpIndex, userProfile), fontSize = 15.sp, lineHeight = 22.sp)
                    Spacer(Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("☝️ Помните: эти советы носят рекомендательный характер. При необходимости проконсультируйтесь с врачом.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showTipsDialog = false }) {
                    Text("Понятно", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                }
            }
        )
    }
}

private fun kpDescription(kp: Int): String = when {
    kp <= 2 -> "спокойно"
    kp <= 4 -> "возмущённо"
    else -> "буря"
}

private fun getTipMessage(
    pressure: Double,
    temperature: Double,
    humidity: Int,
    riskLevel: RiskLevel,
    kpIndex: Int,
    profile: UserProfile?
): String {
    val hasHypertonia = profile?.chronicDiseases?.contains("гипертония") == true
    val hasHypotonia = profile?.chronicDiseases?.contains("гипотония") == true
    val hasJoint = profile?.chronicDiseases?.contains("болезнь суставов") == true
    val hasMigraine = profile?.chronicDiseases?.contains("мигрень") == true
    val hasAsthma = profile?.chronicDiseases?.contains("астма") == true

    return when {
        kpIndex >= 5 -> {
            val advice = "🌍 Магнитная буря (Kp = $kpIndex).\n\n• Снизьте физические и умственные нагрузки\n• Пейте больше чистой воды\n• Ложитесь спать на час раньше\n• При головной боли создайте тишину и темноту"
            if (hasMigraine) advice + "\n• Примите препарат от мигрени (по назначению врача)"
            else advice
        }
        riskLevel == RiskLevel.RED -> {
            "⚠️ Сегодня высокий риск ухудшения самочувствия.\n\n• Снизьте физические нагрузки\n• Пейте больше чистой воды\n• При необходимости примите лекарства (по назначению врача)\n• Ложитесь спать на час раньше"
        }
        pressure < 740 -> {
            if (hasHypotonia) {
                "🩸 Давление ниже нормы (${pressure.toInt()} мм). Вы гипотоник.\n\n• Утром выпейте тёплую воду с щепоткой соли\n• Не вставайте резко с постели\n• Контрастный душ или зелёный чай помогут взбодриться"
            } else if (hasHypertonia) {
                "🩸 Давление ниже нормы (${pressure.toInt()} мм). У вас гипертония, такое давление может быть необычным.\n\n• Измерьте давление, будьте осторожны\n• Не делайте резких движений"
            } else {
                "🩸 Давление ниже нормы (${pressure.toInt()} мм).\n\n• Утром выпейте тёплую воду с щепоткой соли\n• Не вставайте резко с постели\n• Контрастный душ или зелёный чай помогут взбодриться"
            }
        }
        pressure > 780 -> {
            if (hasHypertonia) {
                "💢 Давление выше нормы (${pressure.toInt()} мм). У вас гипертония!\n\n• Немедленно примите гипотензивное (по назначению)\n• Ложитесь, избегайте стресса\n• Не пейте кофе и алкоголь"
            } else {
                "💢 Давление выше нормы (${pressure.toInt()} мм).\n\n• Снизьте потребление соли\n• Измерьте давление, примите гипотензивное (по назначению)\n• Избегайте стресса и интенсивных нагрузок"
            }
        }
        humidity > 75 -> {
            val jointAdvice = if (hasJoint) "\n• При болях в суставах держите их в тепле" else ""
            val asthmaAdvice = if (hasAsthma) "\n• Астматикам: держите ингалятор при себе" else ""
            "💧 Высокая влажность ($humidity%).\n\n• Проветривайте помещение короткими интервалами$jointAdvice$asthmaAdvice"
        }
        temperature > 30 -> {
            "🌡️ Аномальная жара ($temperature°C).\n\n• Пейте воду каждые 20 минут\n• Не выходите на солнце с 12 до 16 часов\n• Носите светлую одежду и головной убор"
        }
        temperature < -10 -> {
            "❄️ Сильный холод ($temperature°C).\n\n• Одевайтесь многослойно\n• Сердечникам: избегайте резкого выхода на холод\n• При выходе закройте лицо шарфом"
        }
        else -> {
            "☀️ Показатели в норме.\n\n• Вы можете вести обычный образ жизни\n• Следите за самочувствием в дневнике\n• При ухудшении добавьте запись — алгоритм станет точнее предсказывать ваши критические дни"
        }
    }
}