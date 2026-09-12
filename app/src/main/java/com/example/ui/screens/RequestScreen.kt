package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AccentSkyBlue
import com.example.ui.theme.BorderBlue
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.CallButtonBg
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.SelectedCardBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextLightBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ServiceRequestViewModel
import com.example.util.ContactUtils

@Composable
fun RequestScreen(
    viewModel: ServiceRequestViewModel,
    initialServiceType: String = "",
    onBack: () -> Unit,
    onNavigateToMyRequests: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialServiceType) }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var showMissingFieldsAlert by remember { mutableStateOf(false) }
    var createdRequestId by remember { mutableStateOf<Long?>(null) }

    BackHandler {
        onBack()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
    }

    // Success dialog after saving to database
    if (createdRequestId != null) {
        val reqId = createdRequestId!!
        AlertDialog(
            onDismissRequest = { createdRequestId = null },
            containerColor = DarkCard,
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Talep Sisteme Kaydedildi!",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Kayıt No: #$reqId\nServis talebiniz sisteme başarıyla kaydedildi. Yetkili servis ekibimiz en kısa sürede sizinle iletişime geçecektir.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "WhatsApp üzerinden de hızlıca bildirim gönderebilirsiniz:",
                        color = TextLightBlue,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val message = "🔧 ÖZALPLER TEKNİK SERVİS TALEBİ (Kayıt #$reqId)\n\n" +
                                "Ad Soyad: ${customerName.trim()}\n" +
                                "Telefon: ${customerPhone.trim()}\n" +
                                "Cihaz: $selectedType\n" +
                                "Arıza: ${description.trim()}\n" +
                                "Adres: ${address.trim()}" +
                                if (selectedPhotoUri != null) "\n📷 Cihaz fotoğrafı eklendi." else ""

                        ContactUtils.openWhatsApp(context, message)
                        createdRequestId = null
                        onNavigateToMyRequests()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("WhatsApp'tan Gönder", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        createdRequestId = null
                        onNavigateToMyRequests()
                    }
                ) {
                    Text("Taleplerimi Gör", color = AccentSkyBlue)
                }
            }
        )
    }

    // Validation alert
    if (showMissingFieldsAlert) {
        AlertDialog(
            onDismissRequest = { showMissingFieldsAlert = false },
            containerColor = DarkCard,
            title = {
                Text(
                    text = "Eksik Bilgi",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Lütfen aşağıdaki zorunlu alanları doldurun:\n" +
                            (if (customerName.isBlank()) "• Ad Soyad\n" else "") +
                            (if (customerPhone.isBlank()) "• Telefon Numarası\n" else "") +
                            (if (selectedType.isBlank()) "• Cihaz Türü\n" else "") +
                            (if (description.isBlank()) "• Arıza Açıklaması\n" else "") +
                            (if (address.isBlank()) "• Adres" else ""),
                    color = TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showMissingFieldsAlert = false }) {
                    Text("Tamam", color = AccentSkyBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Back Button
        Row(
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(vertical = 8.dp)
                .testTag("btn_back_to_home"),
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
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Branding and Title
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ÖZALPLER",
                color = AccentSkyBlue,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "TEKNİK SERVİS",
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Servis Talebi Oluştur",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            Text(
                text = "Talebiniz sisteme kaydedilerek yetkili teknisyene iletilecektir.",
                color = TextDim,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Customer Info Section
        Text(
            text = "İletişim Bilgileriniz",
            color = AccentSkyBlue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Ad Soyad
        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            placeholder = { Text("Adınız Soyadınız", color = TextDim, fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null, tint = AccentSkyBlue, modifier = Modifier.size(20.dp))
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_customer_name"),
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

        // Telefon
        OutlinedTextField(
            value = customerPhone,
            onValueChange = { customerPhone = it },
            placeholder = { Text("Telefon Numaranız (05XX XXX XX XX)", color = TextDim, fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null, tint = AccentSkyBlue, modifier = Modifier.size(20.dp))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_customer_phone"),
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

        // Device Selection
        Text(
            text = "Cihazınız",
            color = TextWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeviceOptionButton(
                title = "Çamaşır Makinesi",
                isSelected = selectedType == "Çamaşır Makinesi",
                onClick = { selectedType = "Çamaşır Makinesi" },
                modifier = Modifier.weight(1f)
            )
            DeviceOptionButton(
                title = "Bulaşık Makinesi",
                isSelected = selectedType == "Bulaşık Makinesi",
                onClick = { selectedType = "Bulaşık Makinesi" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeviceOptionButton(
                title = "Buzdolabı",
                isSelected = selectedType == "Buzdolabı",
                onClick = { selectedType = "Buzdolabı" },
                modifier = Modifier.weight(1f)
            )
            DeviceOptionButton(
                title = "Fırın / Ocak",
                isSelected = selectedType == "Fırın / Ocak",
                onClick = { selectedType = "Fırın / Ocak" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        DeviceOptionButton(
            title = "Kombi",
            isSelected = selectedType == "Kombi",
            onClick = { selectedType = "Kombi" },
            modifier = Modifier.fillMaxWidth()
        )

        // Arıza Açıklaması
        Text(
            text = "Arıza Açıklaması",
            color = TextWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = {
                Text(
                    text = "Cihazdaki arızayı ve belirtileri yazın...",
                    color = TextDim,
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_issue_description"),
            minLines = 3,
            maxLines = 6,
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

        // Adres
        Text(
            text = "Servis Adresi",
            color = TextWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            placeholder = {
                Text(
                    text = "İlçe, mahalle, sokak, bina ve kapı no yazın...",
                    color = TextDim,
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_address"),
            minLines = 2,
            maxLines = 4,
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

        // Photo Picker
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                .testTag("card_photo_picker"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedPhotoUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription = "Seçilen fotoğraf",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedPhotoUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(32.dp)
                                .background(DarkBg.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fotoğrafı Kaldır",
                                tint = TextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fotoğraf eklendi (Değiştirmek için dokunun)",
                        color = AccentSkyBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fotoğraf Ekle (İsteğe Bağlı)",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "Arızalı cihazın veya parçanın fotoğrafını ekleyebilirsiniz",
                        color = TextDim,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Submit Button (Sisteme Kaydet & WhatsApp'tan İlet)
        Button(
            onClick = {
                if (customerName.isBlank() || customerPhone.isBlank() || selectedType.isBlank() || description.isBlank() || address.isBlank()) {
                    showMissingFieldsAlert = true
                } else {
                    viewModel.submitRequest(
                        customerName = customerName,
                        customerPhone = customerPhone,
                        deviceType = selectedType,
                        issueDescription = description,
                        address = address,
                        photoUri = selectedPhotoUri?.toString()
                    ) { newId ->
                        createdRequestId = newId
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp)
                .height(56.dp)
                .testTag("btn_save_request"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TALEBİ SİSTEME KAYDET",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Direct Call Button
        Button(
            onClick = { ContactUtils.call(context) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 24.dp)
                .height(52.dp)
                .testTag("btn_request_call"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CallButtonBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HEMEN TELEFONLA ARA",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun DeviceOptionButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(52.dp)
            .clickable(onClick = onClick)
            .testTag("option_device_${title.replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SelectedCardBg else DarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) AccentSkyBlue else BorderBlue
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = AccentSkyBlue,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
