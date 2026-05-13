package com.example.meteopipli.presentation.ui

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meteopipli.presentation.viewmodel.SettingsViewModel
import com.example.meteopipli.presentation.viewmodel.SettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context.applicationContext as Application)
    )

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val areNotificationsEnabled by viewModel.areNotificationsEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Карточка с настройкой темы
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Внешний вид", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Тёмная тема", fontSize = 16.sp)
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.toggleDarkTheme(it) }
                        )
                    }
                }
            }

            // Карточка с настройкой уведомлений
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Уведомления", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Получать предупреждения о погоде", fontSize = 16.sp)
                        Switch(
                            checked = areNotificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (areNotificationsEnabled) "✓ Вы будете получать оповещения о магнитных бурях и резких изменениях погоды"
                        else "✗ Уведомления отключены",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}