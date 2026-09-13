package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.data.model.AppUpdateInfo
import com.example.data.model.ServiceRequest
import com.example.ui.theme.AccentSkyBlue
import com.example.ui.theme.BorderBlue
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.CallButtonBg
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SelectedCardBg
import com.example.ui.theme.SmallButtonBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextLightBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.ServiceRequestViewModel
import com.example.util.ContactUtils
import com.example.util.UpdateManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    viewModel: ServiceRequestViewModel,
    onSwitchToCustomerPanel: () -> Unit,
    onTriggerUpdate: (AppUpdateInfo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allRequests by viewModel.allRequests.collectAsStateWithLifecycle()
    val filteredRequests by viewModel.filteredAdminRequests.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var managingRequest by remember { mutableStateOf<ServiceRequest?>(null) }
    var requestToDelete by remember { mutableStateOf<ServiceRequest?>(null) }
    var showUpdateSettings by remember { mutableStateOf(false) }

    // Metrics
    val totalCount = allRequests.size
    val newCount = allRequests.count { it.status == ServiceRequest.STATUS_NEW }
    val inProgressCount = allRequests.count { it.status == ServiceRequest.STATUS_IN_PROGRESS }
    val completedCount = allRequests.count { it.status == ServiceRequest.STATUS_COMPLETED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Admin Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "YÖNETİCİ PANELİ",
                    color = AccentSkyBlue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Özalpler Sistem Sahibi & Servis Yönetimi",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onSwitchToCustomerPanel,
                colors = ButtonDefaults.buttonColors(containerColor = SmallButtonBg),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue),
                modifier = Modifier.testTag("btn_switch_to_customer_panel")
            ) {
                Icon(
                    imageVector = Icons.Default.SwitchAccount,
                    contentDescription = null,
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Çıkış Yap", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // OTA Update & App Version Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurface)
                .clickable { showUpdateSettings = true }
                .border(1.dp, BorderBlue, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("btn_admin_update_settings"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sürüm: v${BuildConfig.VERSION_NAME} (Yapı: ${BuildConfig.VERSION_CODE})",
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "OTA / Güncelleme Ayarları",
                    color = AccentSkyBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Metrics Summary Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Toplam",
                count = totalCount,
                color = AccentSkyBlue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Yeni",
                count = newCount,
                color = Color(0xFF64B5F6),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "İşlemde",
                count = inProgressCount,
                color = Color(0xFFFFB74D),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Biten",
                count = completedCount,
                color = Color(0xFF81C784),
                modifier = Modifier.weight(1f)
            )
        }

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Müşteri Ara...", color = TextDim, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = AccentSkyBlue, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Temizle", tint = TextDim, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = AccentSkyBlue,
                unfocusedBorderColor = BorderBlue,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = AccentSkyBlue
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips (Horizontal Scroll)
        val filterOptions = listOf(
            null to "Tümü ($totalCount)",
            ServiceRequest.STATUS_NEW to "Yeni ($newCount)",
            ServiceRequest.STATUS_IN_PROGRESS to "İşlemde ($inProgressCount)",
            ServiceRequest.STATUS_COMPLETED to "Tamamlanan ($completedCount)",
            ServiceRequest.STATUS_CANCELLED to "İptal (${allRequests.count { it.status == ServiceRequest.STATUS_CANCELLED }})"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filterOptions.forEach { (status, label) ->
                val isSelected = selectedFilter == status
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedStatusFilter.value = status },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SelectedCardBg,
                        selectedLabelColor = AccentSkyBlue,
                        containerColor = DarkSurface,
                        labelColor = TextMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) AccentSkyBlue else BorderBlue,
                        selectedBorderColor = AccentSkyBlue,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        // Requests List
        if (filteredRequests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank() || selectedFilter != null) {
                        "Kriterlere uygun servis talebi bulunamadı."
                    } else {
                        "Henüz kayıtlı servis talebi yok."
                    },
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRequests, key = { it.id }) { request ->
                    AdminRequestCard(
                        request = request,
                        onCallCustomer = { ContactUtils.callCustomer(context, request.customerPhone) },
                        onMessageCustomer = {
                            val msg = "Merhaba Sayın ${request.customerName}, Özalpler Teknik Servis'ten #${request.id} nolu ${request.deviceType} talebiniz için ulaşıyoruz."
                            ContactUtils.messageCustomer(context, request.customerPhone, msg)
                        },
                        onOpenMap = { ContactUtils.openMapLocation(context, request.address) },
                        onManage = { managingRequest = request },
                        onDeleteRequest = { requestToDelete = request }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Manage / Edit Modal Dialog
    if (managingRequest != null) {
        val req = managingRequest!!
        AdminManageRequestDialog(
            request = req,
            onDismiss = { managingRequest = null },
            onUpdateStatus = { newStatus ->
                viewModel.updateStatus(req.id, newStatus)
                managingRequest = req.copy(status = newStatus)
            },
            onUpdateNote = { note ->
                viewModel.updateTechnicianNote(req.id, note)
                managingRequest = req.copy(technicianNote = note)
            },
            onDelete = {
                requestToDelete = req
                managingRequest = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (requestToDelete != null) {
        val target = requestToDelete!!
        AlertDialog(
            onDismissRequest = { requestToDelete = null },
            containerColor = DarkCard,
            title = {
                Text("Talebi Sil", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Sayın ${target.customerName} adına kayıtlı #${target.id} nolu ${target.deviceType} servis talebini silmek istediğinize emin misiniz?",
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRequest(target.id)
                        requestToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Evet, Sil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { requestToDelete = null }) {
                    Text("Vazgeç", color = TextWhite)
                }
            }
        )
    }

    // Admin Update Settings Dialog
    if (showUpdateSettings) {
        AdminUpdateSettingsDialog(
            onDismiss = { showUpdateSettings = false },
            onTriggerUpdate = { updateInfo ->
                showUpdateSettings = false
                onTriggerUpdate(updateInfo)
            }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = title,
                color = TextDim,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AdminRequestCard(
    request: ServiceRequest,
    onCallCustomer: () -> Unit,
    onMessageCustomer: () -> Unit,
    onOpenMap: () -> Unit,
    onManage: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(request.createdAt))

    val (statusColor, statusBg) = when (request.status) {
        ServiceRequest.STATUS_NEW -> Pair(AccentSkyBlue, Color(0xFF103657))
        ServiceRequest.STATUS_IN_PROGRESS -> Pair(Color(0xFFFFB74D), Color(0xFF3E2713))
        ServiceRequest.STATUS_COMPLETED -> Pair(Color(0xFF81C784), Color(0xFF13381B))
        ServiceRequest.STATUS_CANCELLED -> Pair(Color(0xFFE57373), Color(0xFF381313))
        else -> Pair(TextMuted, DarkSurface)
    }

    val applianceEmoji = when {
        request.deviceType.contains("Çamaşır", ignoreCase = true) -> "🧺"
        request.deviceType.contains("Bulaşık", ignoreCase = true) -> "🍽️"
        request.deviceType.contains("Buzdolabı", ignoreCase = true) -> "❄️"
        request.deviceType.contains("Fırın", ignoreCase = true) -> "🔥"
        request.deviceType.contains("Kombi", ignoreCase = true) -> "♨️"
        else -> "🔧"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_request_card_${request.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Customer Name & Status Badge + Delete Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = applianceEmoji, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = request.customerName,
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${request.deviceType} • Talep #${request.id}",
                            color = AccentSkyBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = request.status,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onDeleteRequest,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF381313))
                            .testTag("admin_header_delete_${request.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Talebi Sil",
                            tint = ErrorRed,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Phone and Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = AccentSkyBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = request.customerPhone,
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = dateString,
                    color = TextDim,
                    fontSize = 11.sp
                )
            }

            // Issue Description
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Arıza Açıklaması:",
                color = TextDim,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = request.issueDescription,
                color = TextWhite,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Address
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextDim,
                    modifier = Modifier
                        .size(15.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = request.address,
                    color = TextLightBlue,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // Photo indicator
            if (request.photoUri != null) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = AccentSkyBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Fotoğraf eklendi", color = AccentSkyBlue, fontSize = 11.sp)
                }
            }

            // Technician note preview if any
            if (request.technicianNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderHighlight)
                ) {
                    Text(
                        text = "Not: ${request.technicianNote}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row 1: Müşteri Ara, WhatsApp, Harita
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Call -> Müşteri Ara
                Button(
                    onClick = onCallCustomer,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(40.dp)
                        .testTag("admin_call_customer_${request.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CallButtonBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = AccentSkyBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Müşteri Ara", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // WhatsApp
                Button(
                    onClick = onMessageCustomer,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SmallButtonBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = AccentSkyBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Map
                Button(
                    onClick = onOpenMap,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SmallButtonBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentSkyBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Harita", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row 2: Talebi Yönet / Not Ekle + Talebi Sil
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Manage / Edit
                Button(
                    onClick = onManage,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(40.dp)
                        .testTag("admin_manage_btn_${request.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = TextWhite, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Talebi Yönet", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Direct Delete Button
                Button(
                    onClick = onDeleteRequest,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(40.dp)
                        .testTag("admin_delete_btn_${request.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381313)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Talebi Sil", color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminManageRequestDialog(
    request: ServiceRequest,
    onDismiss: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onUpdateNote: (String) -> Unit,
    onDelete: () -> Unit
) {
    var noteInput by remember { mutableStateOf(request.technicianNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Talep #${request.id} Yönetimi",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${request.customerName} - ${request.deviceType}",
                        color = AccentSkyBlue,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextWhite)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Photo preview if available
                if (request.photoUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(bottom = 10.dp)
                    ) {
                        AsyncImage(
                            model = request.photoUri,
                            contentDescription = "Cihaz fotoğrafı",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Status selection
                Text(
                    text = "Durumu Güncelle:",
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ServiceRequest.ALL_STATUSES.forEach { status ->
                        val isSelected = request.status == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateStatus(status) },
                            label = { Text(status, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SelectedCardBg,
                                selectedLabelColor = AccentSkyBlue,
                                containerColor = DarkSurface,
                                labelColor = TextDim
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) AccentSkyBlue else BorderBlue,
                                selectedBorderColor = AccentSkyBlue,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Technician note input
                Text(
                    text = "Teknisyen Notu:",
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("Parça değişimi, randevu saati, ücret vb. notlar...", color = TextDim, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = AccentSkyBlue,
                        unfocusedBorderColor = BorderBlue,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = AccentSkyBlue
                    )
                )

                Button(
                    onClick = { onUpdateNote(noteInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Notu Kaydet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Delete Request Button
                Button(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF421616)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bu Talebi Sil", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = AccentSkyBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AdminUpdateSettingsDialog(
    onDismiss: () -> Unit,
    onTriggerUpdate: (AppUpdateInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var urlInput by remember { mutableStateOf(UpdateManager.getUpdateJsonUrl(context)) }
    var saveStatus by remember { mutableStateOf<String?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var checkMessage by remember { mutableStateOf<String?>(null) }
    var detectedUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.testTag("dialog_admin_update_settings"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "OTA Güncelleme Ayarları",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mevcut Yapı: v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        color = TextLightBlue,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Güncelleme JSON Sunucu Linki:",
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = {
                        urlInput = it
                        saveStatus = null
                    },
                    placeholder = { Text("https://alanadiniz.com/version.json", color = TextDim, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_update_json_url"),
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = AccentSkyBlue,
                        unfocusedBorderColor = BorderBlue,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = AccentSkyBlue
                    )
                )

                if (saveStatus != null) {
                    Text(
                        text = saveStatus!!,
                        color = WhatsAppGreen,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            UpdateManager.setUpdateJsonUrl(context, urlInput)
                            saveStatus = "Link başarıyla kaydedildi!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_save_update_url")
                    ) {
                        Text("Kaydet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            UpdateManager.resetUpdateJsonUrl(context)
                            urlInput = UpdateManager.getUpdateJsonUrl(context)
                            saveStatus = "Varsayılan linke sıfırlandı."
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Text("Varsayılana Dön", color = TextDim, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Test Connection Button
                Button(
                    onClick = {
                        isChecking = true
                        checkMessage = null
                        detectedUpdate = null
                        isError = false
                        coroutineScope.launch {
                            val result = UpdateManager.checkForUpdate(context, urlInput)
                            isChecking = false
                            if (result.isSuccess) {
                                val update = result.getOrNull()
                                if (update != null) {
                                    detectedUpdate = update
                                    checkMessage = "Yeni güncelleme bulundu: v${update.versionName} (Kod: ${update.versionCode})"
                                    isError = false
                                } else {
                                    checkMessage = "Uygulamanız en güncel sürümde! (Sunucudaki sürüm mevcut sürümden daha yüksek değil)"
                                    isError = false
                                }
                            } else {
                                checkMessage = "Kontrol başarısız: ${result.exceptionOrNull()?.localizedMessage}"
                                isError = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_test_update_connection"),
                    enabled = !isChecking
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AccentSkyBlue,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kontrol Ediliyor...", color = TextLightBlue, fontSize = 12.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Güncellemeyi Şimdi Denetle", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Check message feedback
                if (checkMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isError) Color(0xFF331515) else DarkSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isError) ErrorRed.copy(alpha = 0.5f) else BorderBlue
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = checkMessage!!,
                                color = if (isError) ErrorRed else if (detectedUpdate != null) WhatsAppGreen else TextLightBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            if (detectedUpdate != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onTriggerUpdate(detectedUpdate!!) },
                                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                        .testTag("btn_open_detected_update")
                                ) {
                                    Text("Güncelleme Penceresini Aç", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Expected JSON Schema info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSubtle),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AccentSkyBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sunucuda Bulunması Gereken JSON:",
                                color = TextLightBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "{\n  \"versionCode\": 2,\n  \"versionName\": \"1.1\",\n  \"apkUrl\": \"https://siteniz.com/ozalpler.apk\",\n  \"releaseNotes\": \"Yeni özellikler ve hata düzeltmeleri.\",\n  \"fileSizeMb\": 18.5,\n  \"forceUpdate\": false\n}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = AccentSkyBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}

