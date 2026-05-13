package com.example.meteopipli.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meteopipli.domain.model.UserProfile
import com.example.meteopipli.presentation.viewmodel.ProfileViewModel
import com.example.meteopipli.presentation.viewmodel.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onProfileSaved: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var ageGroup by remember { mutableStateOf(profile.ageGroup) }
    var selectedDiseases by remember { mutableStateOf(profile.chronicDiseases.toSet()) }
    var otherDiseaseText by remember { mutableStateOf(profile.otherDisease) }
    var sensitivity by remember { mutableStateOf(profile.weatherSensitivity) }
    var selectedSymptoms by remember { mutableStateOf(profile.symptoms.toSet()) }

    val ageOptions = listOf("до 30", "30-45", "46-60", "старше 60")
    val diseaseOptions = listOf("гипертония", "гипотония", "болезнь суставов", "мигрень", "астма", "другое")
    val sensitivityOptions = listOf("редко", "1-2 раза в месяц", "иногда", "раз в неделю", "2-3 раза в неделю", "всегда")
    val symptomOptions = listOf("Головная боль", "Слабость", "Боли в суставах", "Скачки давления", "Одышка")

    // Цвета из текущей темы
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardContainerColor = MaterialTheme.colorScheme.surface
    val accentColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Text(
            text = "Давайте познакомимся",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "Ответьте на несколько вопросов, чтобы мы могли давать персонализированные советы.",
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Блок: Возраст
        ProfileCard(
            title = "Ваш возраст",
            icon = Icons.Default.Cake,
            containerColor = cardContainerColor,
            contentColor = textColor,
            accentColor = accentColor,
            content = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ageOptions.forEach { option ->
                        FilterChip(
                            selected = ageGroup == option,
                            onClick = { ageGroup = option },
                            label = { Text(option) },
                            shape = RoundedCornerShape(24.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentColor,
                                selectedLabelColor = Color.White,
                                disabledContainerColor = cardContainerColor,
                                disabledLabelColor = textColor
                            )
                        )
                    }
                }
            }
        )

        // Блок: Хронические заболевания
        ProfileCard(
            title = "Хронические заболевания",
            icon = Icons.Default.Favorite,
            containerColor = cardContainerColor,
            contentColor = textColor,
            accentColor = accentColor,
            content = {
                Column {
                    diseaseOptions.forEach { disease ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = disease in selectedDiseases,
                                onCheckedChange = { checked ->
                                    selectedDiseases = if (checked) selectedDiseases + disease else selectedDiseases - disease
                                },
                                colors = CheckboxDefaults.colors(checkedColor = accentColor)
                            )
                            Text(disease, fontSize = 15.sp, color = textColor)
                        }
                    }
                    if ("другое" in selectedDiseases) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = otherDiseaseText,
                            onValueChange = { otherDiseaseText = it },
                            label = { Text("Укажите заболевание") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        )

        // Блок: Частота изменений погоды
        ProfileCard(
            title = "Как часто вы замечаете изменения погоды?",
            icon = Icons.Default.WbSunny,
            containerColor = cardContainerColor,
            contentColor = textColor,
            accentColor = accentColor,
            content = {
                Column {
                    sensitivityOptions.forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = sensitivity == option,
                                onClick = { sensitivity = option },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                            )
                            Text(option, fontSize = 15.sp, color = textColor)
                        }
                    }
                }
            }
        )

        // Блок: Обычные симптомы
        ProfileCard(
            title = "Какие симптомы вас обычно беспокоят?",
            icon = Icons.Default.Sick,
            containerColor = cardContainerColor,
            contentColor = textColor,
            accentColor = accentColor,
            content = {
                symptomOptions.forEach { symptom ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = symptom in selectedSymptoms,
                            onCheckedChange = { checked ->
                                selectedSymptoms = if (checked) selectedSymptoms + symptom else selectedSymptoms - symptom
                            },
                            colors = CheckboxDefaults.colors(checkedColor = accentColor)
                        )
                        Text(symptom, fontSize = 15.sp, color = textColor)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка сохранения (однотонная с цветом акцента)
        Button(
            onClick = {
                val symptomsKeys = selectedSymptoms.map { symptom ->
                    when (symptom) {
                        "Головная боль" -> "headache"
                        "Слабость" -> "fatigue"
                        "Боли в суставах" -> "joint_pain"
                        "Скачки давления" -> "pressure"
                        "Одышка" -> "dyspnea"
                        else -> symptom
                    }
                }
                val profileToSave = UserProfile(
                    isFilled = true,
                    ageGroup = ageGroup,
                    chronicDiseases = selectedDiseases.toList(),
                    otherDisease = if ("другое" in selectedDiseases) otherDiseaseText else "",
                    weatherSensitivity = sensitivity,
                    symptoms = symptomsKeys
                )
                viewModel.saveProfile(profileToSave)
                onProfileSaved()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = ageGroup.isNotBlank() && sensitivity.isNotBlank(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Сохранить", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileCard(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}