package com.example

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppUpdateInfo
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.UpdateDialog
import com.example.ui.components.UpdateDownloadState
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.CustomerRequestsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RequestScreen
import com.example.ui.theme.AccentSkyBlue
import com.example.ui.theme.BorderNav
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkNavBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ServiceRequestViewModel
import com.example.util.ContactUtils
import com.example.util.UpdateManager
import kotlinx.coroutines.launch
import java.io.File

enum class PanelMode {
    CUSTOMER,
    ADMIN
}

enum class CustomerScreen {
    HOME,
    CREATE_REQUEST,
    MY_REQUESTS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OzalplerApp()
            }
        }
    }
}

@Composable
fun OzalplerApp() {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val viewModel: ServiceRequestViewModel = viewModel(factory = ServiceRequestViewModel.Factory(app))

    var currentPanel by remember { mutableStateOf(PanelMode.CUSTOMER) }
    var customerScreen by remember { mutableStateOf(CustomerScreen.HOME) }
    var selectedServiceType by remember { mutableStateOf("") }
    var isAdminAuthenticated by remember { mutableStateOf(false) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }

    val requestAdminAccess = {
        if (isAdminAuthenticated) {
            currentPanel = PanelMode.ADMIN
        } else {
            showAdminLoginDialog = true
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var availableUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var updateDownloadState by remember { mutableStateOf<UpdateDownloadState>(UpdateDownloadState.Idle) }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }

    // Check for updates automatically on app startup
    LaunchedEffect(Unit) {
        val result = UpdateManager.checkForUpdate(context)
        if (result.isSuccess) {
            val update = result.getOrNull()
            if (update != null) {
                availableUpdate = update
            }
        }
    }

    val checkUpdatesManually: () -> Unit = {
        coroutineScope.launch {
            Toast.makeText(context, "Güncellemeler denetleniyor...", Toast.LENGTH_SHORT).show()
            val result = UpdateManager.checkForUpdate(context)
            if (result.isSuccess) {
                val update = result.getOrNull()
                if (update != null) {
                    availableUpdate = update
                } else {
                    Toast.makeText(
                        context,
                        "Uygulamanız en güncel sürümde (v${BuildConfig.VERSION_NAME})",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    "Güncelleme kontrolü başarısız: ${result.exceptionOrNull()?.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val allRequests by viewModel.allRequests.collectAsStateWithLifecycle()
    val requestCount = allRequests.size

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding(),
        containerColor = DarkBg,
        bottomBar = {
            if (currentPanel == PanelMode.CUSTOMER) {
                CustomerBottomNav(
                    currentScreen = customerScreen,
                    requestCount = requestCount,
                    onNavigateHome = { customerScreen = CustomerScreen.HOME },
                    onNavigateCreate = {
                        selectedServiceType = ""
                        customerScreen = CustomerScreen.CREATE_REQUEST
                    },
                    onNavigateMyRequests = { customerScreen = CustomerScreen.MY_REQUESTS },
                    onNavigateAdmin = requestAdminAccess,
                    onCall = { ContactUtils.call(context) }
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentPanel,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "PanelTransition"
        ) { panel ->
            when (panel) {
                PanelMode.CUSTOMER -> {
                    AnimatedContent(
                        targetState = customerScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "CustomerScreenTransition"
                    ) { screen ->
                        when (screen) {
                            CustomerScreen.HOME -> {
                                HomeScreen(
                                    onRequestService = { service ->
                                        selectedServiceType = service
                                        customerScreen = CustomerScreen.CREATE_REQUEST
                                    },
                                    onNavigateToMyRequests = {
                                        customerScreen = CustomerScreen.MY_REQUESTS
                                    },
                                    onSwitchToAdmin = requestAdminAccess,
                                    onCheckForUpdates = checkUpdatesManually
                                )
                            }
                            CustomerScreen.CREATE_REQUEST -> {
                                RequestScreen(
                                    viewModel = viewModel,
                                    initialServiceType = selectedServiceType,
                                    onBack = { customerScreen = CustomerScreen.HOME },
                                    onNavigateToMyRequests = {
                                        customerScreen = CustomerScreen.MY_REQUESTS
                                    }
                                )
                            }
                            CustomerScreen.MY_REQUESTS -> {
                                CustomerRequestsScreen(
                                    viewModel = viewModel,
                                    onCreateNewRequest = {
                                        selectedServiceType = ""
                                        customerScreen = CustomerScreen.CREATE_REQUEST
                                    },
                                    onBack = { customerScreen = CustomerScreen.HOME }
                                )
                            }
                        }
                    }
                }
                PanelMode.ADMIN -> {
                    AdminPanelScreen(
                        viewModel = viewModel,
                        onSwitchToCustomerPanel = {
                            isAdminAuthenticated = false
                            currentPanel = PanelMode.CUSTOMER
                            customerScreen = CustomerScreen.HOME
                        },
                        onTriggerUpdate = { updateInfo ->
                            availableUpdate = updateInfo
                            updateDownloadState = UpdateDownloadState.Idle
                        }
                    )
                }
            }
        }
    }

    // Admin Login Dialog (Password protected: admin / 1818)
    if (showAdminLoginDialog) {
        AdminLoginDialog(
            onDismiss = { showAdminLoginDialog = false },
            onLoginSuccess = {
                isAdminAuthenticated = true
                showAdminLoginDialog = false
                currentPanel = PanelMode.ADMIN
            }
        )
    }

    // App Update Dialog
    availableUpdate?.let { updateInfo ->
        UpdateDialog(
            updateInfo = updateInfo,
            downloadState = updateDownloadState,
            onStartDownload = {
                updateDownloadState = UpdateDownloadState.Downloading(0f)
                coroutineScope.launch {
                    val downloadResult = UpdateManager.downloadApk(
                        context = context,
                        apkUrl = updateInfo.apkUrl,
                        onProgress = { progress ->
                            updateDownloadState = UpdateDownloadState.Downloading(progress)
                        }
                    )
                    if (downloadResult.isSuccess) {
                        val file = downloadResult.getOrThrow()
                        downloadedApkFile = file
                        if (UpdateManager.canInstallApk(context)) {
                            updateDownloadState = UpdateDownloadState.ReadyToInstall
                            UpdateManager.installApk(context, file)
                        } else {
                            updateDownloadState = UpdateDownloadState.NeedPermission()
                        }
                    } else {
                        updateDownloadState = UpdateDownloadState.Error(
                            downloadResult.exceptionOrNull()?.localizedMessage
                                ?: "APK indirme sırasında bir hata oluştu."
                        )
                    }
                }
            },
            onInstall = {
                downloadedApkFile?.let { file ->
                    if (UpdateManager.canInstallApk(context)) {
                        UpdateManager.installApk(context, file)
                    } else {
                        updateDownloadState = UpdateDownloadState.NeedPermission()
                    }
                }
            },
            onRequestPermission = {
                UpdateManager.openInstallPermissionSettings(context)
            },
            onDismiss = {
                availableUpdate = null
                updateDownloadState = UpdateDownloadState.Idle
            }
        )
    }
}

@Composable
fun CustomerBottomNav(
    currentScreen: CustomerScreen,
    requestCount: Int,
    onNavigateHome: () -> Unit,
    onNavigateCreate: () -> Unit,
    onNavigateMyRequests: () -> Unit,
    onNavigateAdmin: () -> Unit,
    onCall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkNavBg)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = BorderNav
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomerBottomNavItem(
                icon = Icons.Default.Home,
                label = "Ana Sayfa",
                isSelected = currentScreen == CustomerScreen.HOME,
                onClick = onNavigateHome,
                testTag = "nav_home"
            )
            CustomerBottomNavItem(
                icon = Icons.Default.Build,
                label = "Talep Aç",
                isSelected = currentScreen == CustomerScreen.CREATE_REQUEST,
                onClick = onNavigateCreate,
                testTag = "nav_create_request"
            )
            CustomerBottomNavItem(
                icon = Icons.Default.Assignment,
                label = "Taleplerim",
                isSelected = currentScreen == CustomerScreen.MY_REQUESTS,
                onClick = onNavigateMyRequests,
                badgeCount = requestCount,
                testTag = "nav_my_requests"
            )
            CustomerBottomNavItem(
                icon = Icons.Default.AdminPanelSettings,
                label = "Yönetici",
                isSelected = false,
                onClick = onNavigateAdmin,
                testTag = "nav_admin_panel"
            )
            CustomerBottomNavItem(
                icon = Icons.Default.Call,
                label = "Ara",
                isSelected = false,
                onClick = onCall,
                testTag = "nav_call"
            )
        }
    }
}

@Composable
fun CustomerBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0,
    testTag: String
) {
    val tint = if (isSelected) AccentSkyBlue else TextDim
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (badgeCount > 0 && label == "Taleplerim") {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = BrandBlue,
                        contentColor = TextWhite
                    ) {
                        Text(text = badgeCount.toString(), fontSize = 10.sp)
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = label,
            color = if (isSelected) TextWhite else TextDim,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OzalplerAppPreview() {
    MyApplicationTheme {
        OzalplerApp()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
