package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.data.model.AppUpdateInfo
import com.example.ui.theme.AccentSkyBlue
import com.example.ui.theme.BorderBlue
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextLightBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WhatsAppGreen

sealed class UpdateDownloadState {
    object Idle : UpdateDownloadState()
    data class Downloading(val progress: Float) : UpdateDownloadState()
    object ReadyToInstall : UpdateDownloadState()
    data class NeedPermission(val reason: String = "Kurulum için bilinmeyen kaynaklara izin vermeniz gerekiyor.") : UpdateDownloadState()
    data class Error(val message: String) : UpdateDownloadState()
}

@Composable
fun UpdateDialog(
    updateInfo: AppUpdateInfo,
    downloadState: UpdateDownloadState,
    onStartDownload: () -> Unit,
    onInstall: () -> Unit,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = {
            if (!updateInfo.forceUpdate && downloadState !is UpdateDownloadState.Downloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !updateInfo.forceUpdate && downloadState !is UpdateDownloadState.Downloading,
            dismissOnClickOutside = !updateInfo.forceUpdate && downloadState !is UpdateDownloadState.Downloading
        ),
        modifier = modifier.testTag("dialog_app_update"),
        containerColor = DarkCard,
        shape = RoundedCornerShape(24.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .border(1.dp, BorderHighlight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when (downloadState) {
                        is UpdateDownloadState.Downloading -> {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = AccentSkyBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        is UpdateDownloadState.ReadyToInstall -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        is UpdateDownloadState.NeedPermission -> {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        is UpdateDownloadState.Error -> {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = AccentSkyBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = when (downloadState) {
                        is UpdateDownloadState.Downloading -> "Güncelleme İndiriliyor"
                        is UpdateDownloadState.ReadyToInstall -> "İndirme Tamamlandı!"
                        is UpdateDownloadState.NeedPermission -> "Kurulum İzni Gerekli"
                        is UpdateDownloadState.Error -> "Güncelleme Hatası"
                        else -> "Yeni Güncelleme Var!"
                    },
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Version comparison pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, BorderBlue, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mevcut: v${BuildConfig.VERSION_NAME}",
                        color = TextDim,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "  ➔  ",
                        color = AccentSkyBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Yeni: v${updateInfo.versionName}",
                        color = WhatsAppGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content based on state
                when (downloadState) {
                    is UpdateDownloadState.Idle -> {
                        if (updateInfo.releaseNotes.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCardSubtle),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Yenilikler & İyileştirmeler:",
                                        color = TextLightBlue,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = updateInfo.releaseNotes,
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        if (updateInfo.fileSizeMb > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "İndirme boyutu: ~${String.format("%.1f", updateInfo.fileSizeMb)} MB",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        if (updateInfo.forceUpdate) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ Uygulamayı kullanmaya devam edebilmek için bu güncelleme zorunludur.",
                                color = Color(0xFFFFB300),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is UpdateDownloadState.Downloading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (downloadState.progress >= 0f) {
                                LinearProgressIndicator(
                                    progress = { downloadState.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = AccentSkyBlue,
                                    trackColor = DarkSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "%${(downloadState.progress * 100).toInt()} İndirildi...",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = AccentSkyBlue,
                                    trackColor = DarkSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "İndiriliyor, lütfen bekleyin...",
                                    color = TextWhite,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "İndirme tamamlandığında kurulum ekranı otomatik olarak açılacaktır.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is UpdateDownloadState.ReadyToInstall -> {
                        Text(
                            text = "Yeni sürüm APK dosyası başarıyla indirildi. Kurulumu tamamlamak için aşağıdaki butona basın.",
                            color = TextWhite,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    is UpdateDownloadState.NeedPermission -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = downloadState.reason,
                                color = Color(0xFFFFB300),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Android güvenlik kuralları gereği, APK dosyasını yükleyebilmek için 'Bu kaynaktan izin ver' seçeneğini etkinleştirmeniz gerekir.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is UpdateDownloadState.Error -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                        ) {
                            Text(
                                text = downloadState.message,
                                color = ErrorRed,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (downloadState) {
                is UpdateDownloadState.Idle -> {
                    Button(
                        onClick = onStartDownload,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_update_download")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Güncelle",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                is UpdateDownloadState.Downloading -> {
                    // In progress
                }

                is UpdateDownloadState.ReadyToInstall -> {
                    Button(
                        onClick = onInstall,
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_update_install")
                    ) {
                        Text(
                            text = "Şimdi Kur",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                is UpdateDownloadState.NeedPermission -> {
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_update_permission")
                    ) {
                        Text(
                            text = "Ayarları Aç ve İzin Ver",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                is UpdateDownloadState.Error -> {
                    Button(
                        onClick = onStartDownload,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_update_retry")
                    ) {
                        Text(
                            text = "Tekrar Dene",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        dismissButton = {
            if (!updateInfo.forceUpdate && downloadState !is UpdateDownloadState.Downloading) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_update_dismiss")
                ) {
                    Text(
                        text = if (downloadState is UpdateDownloadState.ReadyToInstall) "Daha Sonra Kur" else "Daha Sonra",
                        color = TextDim
                    )
                }
            }
        }
    )
}
