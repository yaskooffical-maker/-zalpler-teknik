package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ServiceRequest
import com.example.ui.theme.AccentSkyBlue
import com.example.ui.theme.BorderBlue
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.CallButtonBg
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.SmallButtonBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextLightBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.ServiceRequestViewModel
import com.example.util.ContactUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerRequestsScreen(
    viewModel: ServiceRequestViewModel,
    onCreateNewRequest: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val requests by viewModel.allRequests.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ana Sayfa",
                    color = AccentSkyBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Taleplerim (${requests.size})",
                color = TextWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (requests.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz Servis Talebiniz Yok",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Beyaz eşyalarınızdaki arızalar için hemen yeni bir servis talebi oluşturabilirsiniz.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                    )
                    Button(
                        onClick = onCreateNewRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("YENİ TALEP OLUŞTUR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Kayıtlı Servis Talepleriniz",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(requests, key = { it.id }) { request ->
                    CustomerRequestCard(
                        request = request,
                        onCallService = { ContactUtils.call(context) },
                        onWhatsAppService = {
                            val msg = "Merhaba, #${request.id} numaralı ${request.deviceType} servis talebim hakkında bilgi almak istiyorum."
                            ContactUtils.openWhatsApp(context, msg)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onCreateNewRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_customer_new_request"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("YENİ SERVİS TALEBİ EKLE", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun CustomerRequestCard(
    request: ServiceRequest,
    onCallService: () -> Unit,
    onWhatsAppService: () -> Unit
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
            .testTag("customer_request_card_${request.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Device & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = applianceEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = request.deviceType,
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Talep # ${request.id}",
                            color = TextDim,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = request.status,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Issue description
            Text(
                text = "Arıza:",
                color = AccentSkyBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = request.issueDescription,
                color = TextWhite,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Address
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextDim,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = request.address,
                    color = TextLightBlue,
                    fontSize = 12.sp
                )
            }

            // Date
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = TextDim,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = dateString,
                    color = TextDim,
                    fontSize = 11.sp
                )
            }

            // Technician note (if provided by owner)
            if (request.technicianNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderHighlight)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Teknisyen Notu:",
                            color = AccentSkyBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = request.technicianNote,
                            color = TextWhite,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Call / WhatsApp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCallService,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CallButtonBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = AccentSkyBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Servisi Ara", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onWhatsAppService,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SmallButtonBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = AccentSkyBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Durum Sor", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
