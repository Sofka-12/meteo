package com.example.meteopipli.presentation.ui

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meteopipli.domain.model.DiaryEntryEntity
import com.example.meteopipli.presentation.viewmodel.DiaryViewModel
import com.example.meteopipli.presentation.viewmodel.DiaryViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onBack: () -> Unit,
    currentTemperature: Double,
    currentPressure: Double,
    currentHumidity: Int,
    currentKpIndex: Int
) {
    val context = LocalContext.current
    val diaryViewModel: DiaryViewModel = viewModel(
        factory = DiaryViewModelFactory(context.applicationContext as Application)
    )
    val entries by diaryViewModel.entries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Метео-дневник") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+", fontSize = 24.sp)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { entry ->
                DiaryEntryCard(entry = entry, onDelete = { diaryViewModel.deleteEntry(entry) })
            }
            if (entries.isEmpty()) {
                item {
                    Text("Пока нет записей. Нажмите +, чтобы добавить.", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddDiaryEntryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { feeling, symptoms, note ->
                diaryViewModel.addEntry(
                    feeling = feeling,
                    symptoms = symptoms,
                    note = note,
                    temperature = currentTemperature,
                    pressure = currentPressure,
                    humidity = currentHumidity,
                    kpIndex = currentKpIndex
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DiaryEntryCard(entry: DiaryEntryEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Круг со смайликом
            Box(
                modifier = Modifier
                    .size(56.dp)  // увеличил размер круга
                    .background(Color(0xFFE0E0E0), shape = CircleShape), // светло-серый
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getSmileyForFeeling(entry.feelingScore),
                    fontSize = 32.sp, // увеличил смайлик
                    modifier = Modifier.padding(0.dp) // убираем лишние отступы
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Остальная информация
            Column(modifier = Modifier.weight(1f)) {
                Text(formatDate(entry.timestamp), fontSize = 14.sp, color = Color.Gray)
                if (entry.symptoms.isNotBlank()) {
                    Text("Симптомы: ${translateSymptoms(entry.symptoms)}", fontSize = 14.sp)
                }
                if (entry.note.isNotBlank()) {
                    Text("Заметка: ${entry.note}", fontSize = 14.sp)
                }
                Text(
                    "🌡️ ${entry.temperature}°C, 📊 ${entry.pressure.toInt()} мм, 💧 ${entry.humidity}%, 🧲 Kp=${entry.kpIndex}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Button(onClick = onDelete, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Удалить")
                }
            }
        }
    }
}

// Функция перевода оценки в смайлик
private fun getSmileyForFeeling(score: Int): String = when (score) {
    1 -> "😞"  // очень плохо
    2 -> "🙁"  // плохо
    3 -> "😐"  // нормально
    4 -> "🙂"  // хорошо
    5 -> "😁"  // отлично
    else -> "😐"
}

// Функция перевода симптомов
private fun translateSymptoms(symptoms: String): String {
    val symptomMap = mapOf(
        "headache" to "Головная боль",
        "fatigue" to "Слабость",
        "joint_pain" to "Боли в суставах",
        "pressure" to "Скачки давления",
        "dyspnea" to "Одышка"
    )
    return symptoms.split(",")
        .map { it.trim() }
        .mapNotNull { symptomMap[it] }
        .joinToString(", ")
}

@Composable
fun AddDiaryEntryDialog(
    onDismiss: () -> Unit,
    onSave: (feeling: Int, symptoms: List<String>, note: String) -> Unit
) {
    var feeling by remember { mutableIntStateOf(3) }
    var selectedSymptoms by remember { mutableStateOf<Set<String>>(emptySet()) }
    var note by remember { mutableStateOf("") }
    val symptomOptions = listOf("Головная боль", "Слабость", "Боли в суставах", "Скачки давления", "Одышка")

    val smileys = listOf("😞", "🙁", "😐", "🙂", "😁")
    val scores = listOf(1, 2, 3, 4, 5)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Как вы себя чувствуете?") },
        text = {
            Column {
                Text("Выберите смайлик:", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                // Ряд смайликов без кнопок
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    smileys.forEachIndexed { index, smiley ->
                        val score = scores[index]
                        Text(
                            text = smiley,
                            fontSize = if (feeling == score) 48.sp else 32.sp,
                            modifier = Modifier
                                .clickable { feeling = score }
                                .padding(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Симптомы (можно выбрать несколько):")
                symptomOptions.forEach { symptom ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = symptom in selectedSymptoms,
                            onCheckedChange = { checked ->
                                selectedSymptoms = if (checked) selectedSymptoms + symptom else selectedSymptoms - symptom
                            }
                        )
                        Text(symptom)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val symptomKeys = selectedSymptoms.map { symptom ->
                    when (symptom) {
                        "Головная боль" -> "headache"
                        "Слабость" -> "fatigue"
                        "Боли в суставах" -> "joint_pain"
                        "Скачки давления" -> "pressure"
                        "Одышка" -> "dyspnea"
                        else -> symptom
                    }
                }
                onSave(feeling, symptomKeys, note)
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}