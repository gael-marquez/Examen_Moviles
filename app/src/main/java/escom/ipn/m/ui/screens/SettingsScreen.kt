package escom.ipn.m.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose. foundation.shape.CircleShape
import androidx.compose. foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw. clip
import androidx. compose.ui.graphics.Color
import androidx.compose.ui. platform.LocalContext
import androidx.compose. ui.res.painterResource
import androidx.compose. ui.semantics.Role
import androidx.compose.ui.text. font.FontWeight
import androidx.compose. ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import escom.ipn.m.R
import escom.ipn.m.data.preferences.AppTheme
import escom.ipn.m.data.preferences. PreferencesManager
import escom.ipn.m.data.preferences.TrackingInterval
import escom. ipn.m. data.preferences.UserPreferences
import escom.ipn. m.service.LocationService
import escom.ipn.m.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext. current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()

    val userPreferences by preferencesManager.userPreferencesFlow
        .collectAsStateWithLifecycle(initialValue = UserPreferences())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Configuración",
                        fontWeight = FontWeight. Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R. drawable.ic_back),
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                . fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de Apariencia
            SettingsSection(title = "Apariencia") {
                // Selector de tema
                ThemeSelector(
                    selectedTheme = userPreferences.theme,
                    onThemeSelected = { theme ->
                        scope.launch {
                            preferencesManager.updateTheme(theme)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle modo oscuro
                DarkModeToggle(
                    isDarkMode = userPreferences.isDarkMode,
                    onToggle = { isDark ->
                        scope.launch {
                            preferencesManager.updateDarkMode(isDark)
                        }
                    }
                )
            }

            // Sección de Rastreo
            SettingsSection(title = "Rastreo GPS") {
                // Selector de intervalo
                IntervalSelector(
                    selectedInterval = userPreferences.trackingInterval,
                    onIntervalSelected = { interval ->
                        scope.launch {
                            preferencesManager.updateTrackingInterval(interval)
                            // Actualizar servicio si está activo
                            if (userPreferences.isTrackingEnabled) {
                                updateServiceInterval(context)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle mostrar precisión
                SettingsToggleItem(
                    title = "Mostrar precisión",
                    description = "Muestra el nivel de precisión del GPS",
                    isChecked = userPreferences.showAccuracy,
                    onToggle = { show ->
                        scope.launch {
                            preferencesManager.updateShowAccuracy(show)
                        }
                    }
                )
            }

            // Sección de Notificaciones
            SettingsSection(title = "Notificaciones") {
                SettingsToggleItem(
                    title = "Notificaciones",
                    description = "Mostrar notificación durante el rastreo",
                    isChecked = userPreferences.notificationsEnabled,
                    onToggle = { enabled ->
                        scope.launch {
                            preferencesManager.updateNotificationsEnabled(enabled)
                        }
                    }
                )
            }

            // Información de la app
            SettingsSection(title = "Información") {
                InfoItem(label = "Versión", value = "1.0.0")
                Spacer(modifier = Modifier.height(8.dp))
                InfoItem(label = "Desarrollado para", value = "ESCOM - IPN")
                Spacer(modifier = Modifier.height(8.dp))
                InfoItem(label = "Compatibilidad", value = "Android 7.0+")
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier. fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults. cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme. typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme. colorScheme.primary
            )

            Spacer(modifier = Modifier. height(16.dp))

            content()
        }
    }
}

@Composable
fun ThemeSelector(
    selectedTheme: AppTheme,
    onThemeSelected:  (AppTheme) -> Unit
) {
    Column {
        Text(
            text = "Tema de color",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight. Medium
        )

        Spacer(modifier = Modifier. height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tema Guinda IPN
            ThemeOption(
                name = "Guinda IPN",
                primaryColor = GuindaPrimary,
                secondaryColor = GuindaSecondary,
                isSelected = selectedTheme == AppTheme. GUINDA_IPN,
                onClick = { onThemeSelected(AppTheme.GUINDA_IPN) },
                modifier = Modifier.weight(1f)
            )

            // Tema Azul ESCOM
            ThemeOption(
                name = "Azul ESCOM",
                primaryColor = EscomPrimary,
                secondaryColor = EscomSecondary,
                isSelected = selectedTheme == AppTheme.AZUL_ESCOM,
                onClick = { onThemeSelected(AppTheme. AZUL_ESCOM) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ThemeOption(
    name: String,
    primaryColor: Color,
    secondaryColor: Color,
    isSelected: Boolean,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme. primary
    } else {
        MaterialTheme.colorScheme.outline. copy(alpha = 0.3f)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Muestra de colores
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(secondaryColor)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme. primary
            } else {
                MaterialTheme.colorScheme. onSurface
            }
        )

        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = "Seleccionado",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DarkModeToggle(
    isDarkMode: Boolean,
    onToggle:  (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            . fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant. copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Modo oscuro",
                style = MaterialTheme. typography.bodyLarge,
                fontWeight = FontWeight. Medium
            )
            Text(
                text = if (isDarkMode) "Activado" else "Desactivado",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme. onSurfaceVariant
            )
        }

        Switch(
            checked = isDarkMode,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme. colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun IntervalSelector(
    selectedInterval: TrackingInterval,
    onIntervalSelected: (TrackingInterval) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Intervalo de rastreo",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight. Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier. fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = selectedInterval.displayName,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_dropdown),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                TrackingInterval.entries.forEach { interval ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = interval.displayName,
                                fontWeight = if (interval == selectedInterval) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        },
                        onClick = {
                            onIntervalSelected(interval)
                            expanded = false
                        },
                        leadingIcon = {
                            if (interval == selectedInterval) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Frecuencia con la que se registra la ubicación",
            style = MaterialTheme. typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    description:  String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            . fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme. surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme. typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme. typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults. colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier. fillMaxWidth(),
        horizontalArrangement = Arrangement. SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme. colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme. typography.bodyMedium,
            fontWeight = FontWeight. Medium
        )
    }
}

private fun updateServiceInterval(context: Context) {
    val intent = Intent(context, LocationService::class. java).apply {
        action = LocationService.ACTION_UPDATE_INTERVAL
    }
    context. startService(intent)
}