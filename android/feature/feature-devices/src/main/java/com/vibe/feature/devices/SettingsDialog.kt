package com.vibe.feature.devices

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vibe.core.model.AudioQuality
import com.vibe.core.model.PlaybackMode
import com.vibe.core.model.ThemeMode
import com.vibe.core.model.VibeSettings
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    settings: VibeSettings,
    currentVersion: String = "0.5.4",
    onUpdateSettings: (VibeSettings) -> Unit,
    onOpenDevices: () -> Unit = {},
    onClearCache: () -> Unit = {},
    onLogout: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var showEngineMenu by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            if (showEngineMenu) showEngineMenu = false else onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (showEngineMenu) {
                                IconButton(
                                    onClick = { showEngineMenu = false },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(com.vibe.core.ui.R.string.nav_back),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = stringResource(
                                    if (showEngineMenu) com.vibe.core.ui.R.string.settings_engine_menu_title
                                    else com.vibe.core.ui.R.string.settings_title
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(com.vibe.core.ui.R.string.banner_close),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedContent(
                        targetState = showEngineMenu,
                        transitionSpec = {
                            if (targetState) {
                                (slideInHorizontally(
                                    initialOffsetX = { it / 3 },
                                    animationSpec = tween(240, easing = FastOutSlowInEasing)
                                ) + fadeIn(tween(200))).togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { -it / 4 },
                                        animationSpec = tween(220)
                                    ) + fadeOut(tween(160))
                                )
                            } else {
                                (slideInHorizontally(
                                    initialOffsetX = { -it / 4 },
                                    animationSpec = tween(240, easing = FastOutSlowInEasing)
                                ) + fadeIn(tween(200))).togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { it / 3 },
                                        animationSpec = tween(220)
                                    ) + fadeOut(tween(160))
                                )
                            }
                        },
                        label = "settingsSubMenu"
                    ) { isEnginePage ->
                        if (isEnginePage) {
                            PlaybackEngineMenu(
                                settings = settings,
                                onUpdateSettings = onUpdateSettings,
                                onOpenDevices = {
                                    onDismiss()
                                    onOpenDevices()
                                }
                            )
                        } else {
                            MainSettingsContent(
                                settings = settings,
                                currentVersion = currentVersion,
                                onUpdateSettings = onUpdateSettings,
                                onOpenEngineMenu = { showEngineMenu = true },
                                onClearCache = onClearCache,
                                onLogout = onLogout
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainSettingsContent(
    settings: VibeSettings,
    currentVersion: String,
    onUpdateSettings: (VibeSettings) -> Unit,
    onOpenEngineMenu: () -> Unit,
    onClearCache: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isPlayStoreInstalled = remember { AppUpdater.isInstalledFromPlayStore(context) }
    var updateState by remember { mutableStateOf<UpdateCheckResult>(UpdateCheckResult.Idle) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Playback Engine & Connect (Dedicated Submenu Entry)
        SettingsSectionTitle(stringResource(com.vibe.core.ui.R.string.settings_engine_section))

        val activeModeLabel = when (settings.playbackMode) {
            PlaybackMode.SPOTIFY_REMOTE -> stringResource(com.vibe.core.ui.R.string.settings_mode_ipc_title)
            PlaybackMode.STANDALONE -> stringResource(com.vibe.core.ui.R.string.settings_mode_standalone_title)
            PlaybackMode.CONNECT -> stringResource(com.vibe.core.ui.R.string.settings_mode_connect_title)
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onOpenEngineMenu)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (settings.playbackMode) {
                                    PlaybackMode.SPOTIFY_REMOTE -> Icons.Default.PhoneAndroid
                                    PlaybackMode.STANDALONE -> Icons.Default.MusicNote
                                    PlaybackMode.CONNECT -> Icons.Default.Speaker
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(com.vibe.core.ui.R.string.settings_engine_menu_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activeModeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Audio Quality selector
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(com.vibe.core.ui.R.string.settings_audio_quality_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        AudioQuality.NORMAL to stringResource(com.vibe.core.ui.R.string.settings_audio_quality_normal),
                        AudioQuality.HIGH to stringResource(com.vibe.core.ui.R.string.settings_audio_quality_high)
                    ).forEach { (quality, label) ->
                        val selected = settings.audioQuality == quality
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdateSettings(settings.copy(audioQuality = quality)) },
                            label = {
                                Text(
                                    text = label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Section 2: Appearance (Theme & Monet Dynamic Colors)
        SettingsSectionTitle(stringResource(com.vibe.core.ui.R.string.settings_appearance_section))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(com.vibe.core.ui.R.string.settings_theme_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ThemeMode.SYSTEM to stringResource(com.vibe.core.ui.R.string.settings_theme_system),
                        ThemeMode.LIGHT to stringResource(com.vibe.core.ui.R.string.settings_theme_light),
                        ThemeMode.DARK to stringResource(com.vibe.core.ui.R.string.settings_theme_dark)
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { onUpdateSettings(settings.copy(themeMode = mode)) },
                            label = {
                                Text(
                                    text = label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(com.vibe.core.ui.R.string.settings_dynamic_colors_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(com.vibe.core.ui.R.string.settings_dynamic_colors_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = settings.useDynamicColors,
                        onCheckedChange = { enabled ->
                            onUpdateSettings(settings.copy(useDynamicColors = enabled))
                        }
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Section 3: Resilience & Fallback
        SettingsSectionTitle(stringResource(com.vibe.core.ui.R.string.settings_resilience_section))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(com.vibe.core.ui.R.string.settings_auto_fallback_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(com.vibe.core.ui.R.string.settings_auto_fallback_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = settings.autoFallbackEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateSettings(settings.copy(autoFallbackEnabled = enabled))
                    }
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Section 4: Storage & Cache
        SettingsSectionTitle(stringResource(com.vibe.core.ui.R.string.settings_storage_section))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(com.vibe.core.ui.R.string.settings_audio_cache_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(com.vibe.core.ui.R.string.settings_audio_cache_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = onClearCache,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(com.vibe.core.ui.R.string.settings_clear_cache))
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Section 5: Updates
        SettingsSectionTitle(stringResource(com.vibe.core.ui.R.string.settings_updates_section))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(com.vibe.core.ui.R.string.settings_version_label, currentVersion),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isPlayStoreInstalled) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(com.vibe.core.ui.R.string.settings_update_play_store_managed),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isPlayStoreInstalled) {
                        Button(
                            onClick = { AppUpdater.openPlayStore(context) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(com.vibe.core.ui.R.string.settings_update_open_play_store))
                        }
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    updateState = UpdateCheckResult.Checking
                                    updateState = AppUpdater.checkGitHubUpdate(currentVersion)
                                }
                            },
                            enabled = updateState !is UpdateCheckResult.Checking && !isDownloadingUpdate,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    if (updateState is UpdateCheckResult.Checking) com.vibe.core.ui.R.string.settings_checking_updates
                                    else com.vibe.core.ui.R.string.settings_check_updates
                                )
                            )
                        }
                    }
                }

                when (val state = updateState) {
                    is UpdateCheckResult.UpToDate -> {
                        Text(
                            text = stringResource(com.vibe.core.ui.R.string.settings_update_up_to_date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    is UpdateCheckResult.UpdateAvailable -> {
                        Text(
                            text = stringResource(
                                com.vibe.core.ui.R.string.settings_update_available_format,
                                state.latestVersion
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.releaseNotes.isNotBlank()) {
                            Text(
                                text = state.releaseNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!state.apkDownloadUrl.isNullOrBlank()) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isDownloadingUpdate = true
                                            val res = AppUpdater.downloadAndInstallApk(context, state.apkDownloadUrl)
                                            isDownloadingUpdate = false
                                            res.onFailure { err ->
                                                updateState = UpdateCheckResult.Error(err.message ?: "Download failed")
                                            }
                                        }
                                    },
                                    enabled = !isDownloadingUpdate,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = stringResource(
                                            if (isDownloadingUpdate) com.vibe.core.ui.R.string.settings_update_downloading
                                            else com.vibe.core.ui.R.string.settings_update_download_install
                                        )
                                    )
                                }
                            }
                            if (state.releaseUrl.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.releaseUrl)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(stringResource(com.vibe.core.ui.R.string.settings_update_open_release))
                                }
                            }
                        }
                    }
                    is UpdateCheckResult.Error -> {
                        Text(
                            text = stringResource(com.vibe.core.ui.R.string.settings_update_error, state.message),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> Unit
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Section 6: Spotify Account
        SettingsSectionTitle(stringResource(com.vibe.core.ui.R.string.settings_account_section))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(com.vibe.core.ui.R.string.settings_connected_oauth),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(com.vibe.core.ui.R.string.settings_logout))
                }
            }
        }
    }
}

@Composable
private fun PlaybackEngineMenu(
    settings: VibeSettings,
    onUpdateSettings: (VibeSettings) -> Unit,
    onOpenDevices: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(com.vibe.core.ui.R.string.settings_engine_menu_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PlaybackModeCard(
            title = stringResource(com.vibe.core.ui.R.string.settings_mode_ipc_title),
            subtitle = stringResource(com.vibe.core.ui.R.string.settings_mode_ipc_desc),
            badgeText = stringResource(com.vibe.core.ui.R.string.settings_badge_recommended),
            icon = Icons.Default.PhoneAndroid,
            isSelected = settings.playbackMode == PlaybackMode.SPOTIFY_REMOTE,
            onClick = {
                onUpdateSettings(settings.copy(playbackMode = PlaybackMode.SPOTIFY_REMOTE))
            }
        )

        PlaybackModeCard(
            title = stringResource(com.vibe.core.ui.R.string.settings_mode_standalone_title),
            subtitle = stringResource(com.vibe.core.ui.R.string.settings_mode_standalone_desc),
            badgeText = stringResource(com.vibe.core.ui.R.string.settings_badge_alternative),
            icon = Icons.Default.MusicNote,
            isSelected = settings.playbackMode == PlaybackMode.STANDALONE,
            onClick = {
                onUpdateSettings(settings.copy(playbackMode = PlaybackMode.STANDALONE))
            }
        )

        PlaybackModeCard(
            title = stringResource(com.vibe.core.ui.R.string.settings_mode_connect_title),
            subtitle = stringResource(com.vibe.core.ui.R.string.settings_mode_connect_desc),
            badgeText = stringResource(com.vibe.core.ui.R.string.settings_badge_remote),
            icon = Icons.Default.Speaker,
            isSelected = settings.playbackMode == PlaybackMode.CONNECT,
            onClick = {
                onUpdateSettings(settings.copy(playbackMode = PlaybackMode.CONNECT))
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedButton(
            onClick = onOpenDevices,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.SpeakerGroup,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(com.vibe.core.ui.R.string.settings_open_devices))
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun PlaybackModeCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        label = "modeBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "modeBg"
    )
    val titleColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val subtitleColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (badgeText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = badgeText.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                maxLines = 1
                            )
                        }
                    }
                }

                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor,
                lineHeight = 18.sp
            )
        }
    }
}
