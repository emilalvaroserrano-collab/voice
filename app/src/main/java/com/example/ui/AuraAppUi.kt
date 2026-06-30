package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.api.AuraAudioHelper
import com.example.data.ChatMessage
import com.example.ui.theme.*
import com.example.viewmodel.AuraViewModel
import com.example.viewmodel.VoiceState
import kotlinx.coroutines.launch

@Composable
fun AuraAppNavigator(viewModel: AuraViewModel) {
    var currentScreen by remember { mutableStateOf("onboarding") }
    val currentUser by viewModel.currentUser.collectAsState()

    // Redirect to main screen if already signed in
    LaunchedEffect(currentUser) {
        if (currentUser != null && currentScreen != "main") {
            currentScreen = "main"
        } else if (currentUser == null && currentScreen == "main") {
            currentScreen = "onboarding"
        }
    }

    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            "onboarding" -> OnboardingScreen(
                onNavigateToAuth = { currentScreen = "auth" }
            )
            "auth" -> AuthScreen(
                viewModel = viewModel,
                onBack = { currentScreen = "onboarding" },
                onAuthSuccess = { currentScreen = "main" }
            )
            "main" -> MainScreen(
                viewModel = viewModel,
                onSignOut = {
                    viewModel.authManager.logout()
                    currentScreen = "onboarding"
                }
            )
        }
    }
}

// ==========================================
// 1. ONBOARDING SCREEN
// ==========================================
@Composable
fun OnboardingScreen(onNavigateToAuth: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AuraBackground, Color(0xFF1B072D))
                )
            )
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "AURA AI",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    color = AuraTextPrimary,
                    modifier = Modifier.testTag("onboarding_title")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Conversational Voice Entity",
                    fontSize = 14.sp,
                    color = AuraAccentCyan,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
            }

            // Beautiful Onboarding Illustration
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .aspectRatio(3f / 4f)
                        .border(1.dp, Brush.linearGradient(listOf(AuraAccentViolet, AuraAccentPink)), RoundedCornerShape(24.dp))
                        .shadow(20.dp, clip = true)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_aura_onboarding_1782829514721),
                        contentDescription = "Aura Onboarding",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Subtitle & Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter a realm of ambient intelligence where voice flows instantly. Ask anything, speak seamlessly, listen to wisdom.",
                    textAlign = TextAlign.Center,
                    color = AuraTextSecondary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onNavigateToAuth,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_start_button")
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(AuraAccentViolet, AuraAccentPink)
                            )
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Begin Journey",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ==========================================
// 2. AUTHENTICATION SCREEN
// ==========================================
@Composable
fun AuthScreen(
    viewModel: AuraViewModel,
    onBack: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val firebaseEnabled = viewModel.authManager.isFirebaseEnabled

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraBackground)
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .border(1.dp, Color(0xFF261D3A), CircleShape)
                        .background(Color(0xFF0F071D), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AuraTextPrimary
                    )
                }
                
                Text(
                    text = "SECURE ENTRY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = AuraAccentCyan
                )
                
                Spacer(modifier = Modifier.width(48.dp)) // Equalizer space
            }

            // Input Fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSignUp) "Create Aura Account" else "Welcome Back",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isSignUp) "Sign up to persist chats and audio memories" else "Authorize access to resume conversational threads",
                    fontSize = 13.sp,
                    color = AuraTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))

                if (isSignUp) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name", color = AuraTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AuraTextPrimary,
                            unfocusedTextColor = AuraTextPrimary,
                            focusedBorderColor = AuraAccentViolet,
                            unfocusedBorderColor = Color(0xFF2E2442),
                            focusedContainerColor = Color(0xFF130921),
                            unfocusedContainerColor = Color(0xFF0F071C)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("auth_name_field")
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = AuraTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AuraTextPrimary,
                        unfocusedTextColor = AuraTextPrimary,
                        focusedBorderColor = AuraAccentViolet,
                        unfocusedBorderColor = Color(0xFF2E2442),
                        focusedContainerColor = Color(0xFF130921),
                        unfocusedContainerColor = Color(0xFF0F071C)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("auth_email_field")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = AuraTextSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AuraTextPrimary,
                        unfocusedTextColor = AuraTextPrimary,
                        focusedBorderColor = AuraAccentViolet,
                        unfocusedBorderColor = Color(0xFF2E2442),
                        focusedContainerColor = Color(0xFF130921),
                        unfocusedContainerColor = Color(0xFF0F071C)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_field")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank() || (isSignUp && name.isBlank())) {
                            Toast.makeText(context, "Please populate all fields.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        loading = true
                        coroutineScope.launch {
                            val res = if (isSignUp) {
                                viewModel.authManager.signUpWithEmail(email, password, name)
                            } else {
                                viewModel.authManager.loginWithEmail(email, password)
                            }
                            loading = false
                            if (res.isSuccess) {
                                onAuthSuccess()
                            } else {
                                Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraAccentViolet),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_submit_button")
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isSignUp) "Establish Account" else "Authorize & Resume",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Account Mode
                Text(
                    text = if (isSignUp) "Already have an account? Sign In" else "Need an account? Sign Up",
                    color = AuraAccentPink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { isSignUp = !isSignUp }
                        .padding(8.dp)
                )
            }

            // Google Sign In & Sandbox notice
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Google Button
                OutlinedButton(
                    onClick = {
                        loading = true
                        // Simulate or trigger beautiful Google authentication
                        val res = viewModel.authManager.loginWithGoogleSimulated(
                            name = "Emil Alvaro Serrano",
                            email = "emilalvaroserrano@gmail.com"
                        )
                        loading = false
                        if (res.isSuccess) {
                            onAuthSuccess()
                        }
                    },
                    border = BorderStroke(1.dp, Color(0xFF2C2240)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF0F061C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Google Sign In",
                            tint = AuraAccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sign in with Google",
                            color = AuraTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Compliance Mode Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF100720))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (firebaseEnabled) Icons.Default.Cloud else Icons.Default.Lock,
                        contentDescription = "Cloud Sync",
                        tint = if (firebaseEnabled) AuraAccentCyan else AuraAccentPink,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (firebaseEnabled) "Firebase Active: Syncing with Firestore DB" else "Offline Sandbox Active: Data persisted in Room Database",
                        fontSize = 10.sp,
                        color = AuraTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==========================================
// TYPING INDICATOR & DISCOVER TILES FROM DESIGN SPEC
// ==========================================
@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "Typing")
    
    @Composable
    fun BouncingDot(delay: Int) {
        val bounce by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -12f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = delay, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Bounce"
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .graphicsLayer(translationY = bounce)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), CircleShape)
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        BouncingDot(delay = 0)
        BouncingDot(delay = 150)
        BouncingDot(delay = 300)
    }
}

@Composable
fun DiscoverView(viewModel: AuraViewModel) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Discover",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-1).sp
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DiscoverCard(
                    title = "Trending Prompts",
                    brush = Brush.linearGradient(colors = listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF))),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.stopVoiceRecording("What are some trending AI prompt patterns?")
                        Toast.makeText(context, "Initiated: Trending Prompts", Toast.LENGTH_SHORT).show()
                    }
                )
                DiscoverCard(
                    title = "Learn a Language",
                    brush = Brush.linearGradient(colors = listOf(Color(0xFFA1C4FD), Color(0xFFC2E9FB))),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.stopVoiceRecording("Can we practice speaking conversational Spanish?")
                        Toast.makeText(context, "Initiated: Language Practice", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DiscoverCard(
                    title = "Interview Prep",
                    brush = Brush.linearGradient(colors = listOf(Color(0xFFD4FC79), Color(0xFF96E6A1))),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.stopVoiceRecording("Can you run a mock Android developer interview with me?")
                        Toast.makeText(context, "Initiated: Interview Preparation", Toast.LENGTH_SHORT).show()
                    }
                )
                DiscoverCard(
                    title = "Write a Song",
                    brush = Brush.linearGradient(colors = listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC))),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.stopVoiceRecording("Write a catchy verse and chorus for a synthwave pop song about Aura.")
                        Toast.makeText(context, "Initiated: Songwriting", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun DiscoverCard(
    title: String,
    brush: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
                .padding(20.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

// ==========================================
// ==========================================
// 3. MAIN APP SCREEN (WITH BOTH VOICE & CHAT)
// ==========================================
@Composable
fun MainScreen(
    viewModel: AuraViewModel,
    onSignOut: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current

    // Active View Mode inside Main: "voice", "discover", or "chat"
    var activeTab by remember { mutableStateOf("voice") }
    var textInput by remember { mutableStateOf("") }
    
    val voiceState by viewModel.voiceState.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    // Permissions Helper
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecording()
        } else {
            Toast.makeText(context, "Microphone permission is required for Voice Assistant capabilities.", Toast.LENGTH_LONG).show()
        }
    }

    // Sidebar Layout wrapping contents
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerTonalElevation = 8.dp,
                modifier = Modifier
                    .width(310.dp)
                    .fillMaxHeight()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(0.dp))
            ) {
                AuraSidebarContent(
                    viewModel = viewModel,
                    onSignOut = onSignOut,
                    onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                // Unified Glassmorphic Bottom Sheet wrapping input elements and navigation
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        // 1. Dynamic Input Area (hides on Discover tab)
                        if (activeTab != "discover") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (activeTab == "voice") {
                                    // Voice State Display and Mic Trigger
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                if (voiceState == VoiceState.IDLE) {
                                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                        viewModel.startVoiceRecording()
                                                    } else {
                                                        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                    }
                                                }
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Voice Mode",
                                            tint = if (voiceState != VoiceState.IDLE) AuraAccentPink else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = when (voiceState) {
                                                VoiceState.LISTENING -> "Listening..."
                                                VoiceState.THINKING -> "Thinking..."
                                                VoiceState.SPEAKING -> "Speaking..."
                                                else -> "Talk or type..."
                                            },
                                            color = if (voiceState != VoiceState.IDLE) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    // RED STOP BUTTON with equalizer bars (Waveform) matching the design spec
                                    if (voiceState != VoiceState.IDLE) {
                                        Button(
                                            onClick = {
                                                viewModel.stopPlayback()
                                                viewModel.stopVoiceRecording()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFFE8EB),
                                                contentColor = Color(0xFFDA283F)
                                            ),
                                            shape = RoundedCornerShape(20.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // 3 small vertical Equalizer waves
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier = Modifier.height(12.dp)
                                                ) {
                                                    val infiniteTransition = rememberInfiniteTransition(label = "EqWave")
                                                    val waveHeight1 by infiniteTransition.animateFloat(
                                                        initialValue = 4f, targetValue = 12f,
                                                        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
                                                        label = "W1"
                                                    )
                                                    val waveHeight2 by infiniteTransition.animateFloat(
                                                        initialValue = 10f, targetValue = 4f,
                                                        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
                                                        label = "W2"
                                                    )
                                                    val waveHeight3 by infiniteTransition.animateFloat(
                                                        initialValue = 5f, targetValue = 11f,
                                                        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
                                                        label = "W3"
                                                    )
                                                    Box(modifier = Modifier.width(2.dp).height(waveHeight1.dp).background(Color(0xFFDA283F), RoundedCornerShape(1.dp)))
                                                    Box(modifier = Modifier.width(2.dp).height(waveHeight2.dp).background(Color(0xFFDA283F), RoundedCornerShape(1.dp)))
                                                    Box(modifier = Modifier.width(2.dp).height(waveHeight3.dp).background(Color(0xFFDA283F), RoundedCornerShape(1.dp)))
                                                }
                                                Text(
                                                    text = "Stop",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                } else if (activeTab == "chat") {
                                    // Text Mode input bar with Send Actions
                                    TextField(
                                        value = textInput,
                                        onValueChange = { textInput = it },
                                        placeholder = { Text("Talk or type...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                                        colors = TextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                        keyboardActions = KeyboardActions(onSend = {
                                            if (textInput.isNotBlank()) {
                                                viewModel.sendTextMessage(textInput)
                                                textInput = ""
                                            }
                                        }),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("chat_text_input")
                                    )

                                    IconButton(
                                        onClick = {
                                            if (textInput.isNotBlank()) {
                                                viewModel.sendTextMessage(textInput)
                                                textInput = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                if (textInput.isNotBlank()) AuraAccentPink else MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Send Message",
                                            tint = if (textInput.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            
                            Divider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }

                        // 2. Beautiful Bottom Navigation Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Home tab (Mic icon)
                            BottomNavItem(
                                icon = Icons.Default.Home,
                                label = "Home",
                                isActive = activeTab == "voice",
                                onClick = { activeTab = "voice" }
                            )

                            // 2. Discover tab (Explore icon)
                            BottomNavItem(
                                icon = Icons.Default.Explore,
                                label = "Discover",
                                isActive = activeTab == "discover",
                                onClick = { activeTab = "discover" }
                            )

                            // 3. Chat tab (Chat icon)
                            BottomNavItem(
                                icon = Icons.Default.Chat,
                                label = "Chat",
                                isActive = activeTab == "chat",
                                onClick = { activeTab = "chat" }
                            )

                            // 4. Spaces tab (Drawer trigger)
                            BottomNavItem(
                                icon = Icons.Default.FolderOpen,
                                label = "Spaces",
                                isActive = drawerState.isOpen,
                                onClick = {
                                    coroutineScope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                }
                            )

                            // 5. Theme Toggle tab (Sun/Moon Settings icon)
                            BottomNavItem(
                                icon = if (isDarkTheme) Icons.Default.NightsStay else Icons.Default.WbSunny,
                                label = "Theme",
                                isActive = false,
                                onClick = {
                                    viewModel.toggleTheme()
                                    Toast.makeText(context, "Display Mode toggled.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Dynamic Theme-Responsive Top Bar
                    AuraTopBar(
                        viewModel = viewModel,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } }
                    )

                    // Main View Switcher
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Crossfade(targetState = activeTab, label = "TabCrossfade") { tab ->
                            when (tab) {
                                "voice" -> VoiceChatView(
                                    viewModel = viewModel,
                                    onRequestPermission = {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            viewModel.startVoiceRecording()
                                        } else {
                                            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                )
                                "discover" -> DiscoverView(viewModel = viewModel)
                                "chat" -> TraditionalChatView(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(26.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Dot indicator or subtle label
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

// ==========================================
// 4. AURA TOP BAR COMPONENT
// ==========================================
@Composable
fun AuraTopBar(
    viewModel: AuraViewModel,
    onMenuClick: () -> Unit
) {
    val selectedModel by viewModel.selectedModel.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Actions: Drawer menu & Theme Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu Sidebar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = { viewModel.toggleTheme() },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.NightsStay,
                    contentDescription = "Theme Toggle",
                    tint = if (isDarkTheme) AuraAccentCyan else AuraAccentPink
                )
            }
        }

        // Center Model Version Dropdown
        Box(contentAlignment = Alignment.TopCenter) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .clickable { dropdownExpanded = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = selectedModel,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "BETA",
                    color = AuraAccentCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .border(1.dp, AuraAccentCyan, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Model",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Aura 1.2 BETA", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        viewModel.setModel("Aura 1.2 BETA")
                        dropdownExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Aura 2.0 PRO", color = AuraAccentCyan) },
                    onClick = {
                        viewModel.setModel("Aura 2.0 PRO")
                        dropdownExpanded = false
                    }
                )
            }
        }

        // Right Actions: Volume/Mute & Profile Avatar with border gradient
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { viewModel.toggleMute() },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Mute Toggle",
                    tint = if (isMuted) AuraAccentPink else AuraAccentCyan
                )
            }

            // Initials Avatar with dynamic borders
            val currentUser by viewModel.currentUser.collectAsState()
            val initials = currentUser?.displayName?.take(1)?.uppercase() ?: "A"

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AuraAccentPink, AuraAccentViolet, AuraAccentCyan)
                        )
                    )
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onMenuClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ==========================================
// 5. VIEW A: VOICE CHAT PANEL (SCREEN 3)
// ==========================================
@Composable
fun VoiceChatView(
    viewModel: AuraViewModel,
    onRequestPermission: () -> Unit
) {
    val voiceState by viewModel.voiceState.collectAsState()
    val transcript by viewModel.voiceTranscript.collectAsState()
    val subtext by viewModel.voiceSubtext.collectAsState()

    // Check if we are showing default/empty state to display suggestions
    val showSuggestions = (voiceState == VoiceState.IDLE && 
            (transcript == "Tap the mic below to begin speaking with Aura." || transcript.isBlank()))

    // Breathing / Pulse animation for glowing orb
    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScaleAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Centered Glowing Orb Asset with a beautiful halo
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Halo glow behind the orb that animates scale and changes color based on active state
            val haloGlowColor = if (voiceState == VoiceState.LISTENING) {
                AuraAccentPink
            } else if (voiceState == VoiceState.THINKING) {
                AuraAccentCyan
            } else if (voiceState == VoiceState.SPEAKING) {
                AuraAccentViolet
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            }
            
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .scale(pulseScale)
                    .shadow(
                        elevation = if (voiceState != VoiceState.IDLE) 32.dp else 16.dp,
                        shape = CircleShape,
                        ambientColor = haloGlowColor,
                        spotColor = haloGlowColor
                    )
                    .clip(CircleShape)
                    .border(
                        BorderStroke(
                            2.dp,
                            Brush.linearGradient(
                                colors = listOf(AuraAccentPink, AuraAccentViolet, AuraAccentCyan)
                            )
                        ),
                        CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_aura_orb_1782829482429),
                    contentDescription = "Pulsing Aura Sphere",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Midsection Content: Suggestions list OR Active Conversation bubbles
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (showSuggestions) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Where should we start?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    // Suggestion Card 1
                    SuggestionButton(
                        text = "Let's get to know each other",
                        onClick = { viewModel.stopVoiceRecording("Let's get to know each other") }
                    )
                    
                    // Suggestion Card 2
                    SuggestionButton(
                        text = "Learn how to use AI",
                        onClick = { viewModel.stopVoiceRecording("Learn how to use AI") }
                    )

                    // Suggestion Card 3
                    SuggestionButton(
                        text = "Talk about my goals",
                        onClick = { viewModel.stopVoiceRecording("Talk about my goals") }
                    )
                }
            } else {
                // Display the active conversational response
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (voiceState == VoiceState.THINKING) {
                        TypingIndicator()
                    } else {
                        // AI Transcript bubble with a premium gradient matching the design spec
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(AuraAccentViolet.copy(alpha = 0.85f), AuraAccentPink.copy(alpha = 0.85f))
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = transcript,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    lineHeight = 26.sp,
                                    fontStyle = FontStyle.Normal,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("voice_transcript_text")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = subtext,
                        fontSize = 11.sp,
                        color = AuraAccentCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.testTag("voice_status_indicator")
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionButton(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Waveform equalizer logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.height(16.dp)
            ) {
                Box(modifier = Modifier.width(3.dp).height(10.dp).background(AuraAccentCyan, CircleShape))
                Box(modifier = Modifier.width(3.dp).height(16.dp).background(AuraAccentViolet, CircleShape))
                Box(modifier = Modifier.width(3.dp).height(8.dp).background(AuraAccentPink, CircleShape))
            }
            
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ==========================================
// 6. VIEW B: TRADITIONAL CHAT PANEL (SCREEN 4)
// ==========================================
@Composable
fun TraditionalChatView(viewModel: AuraViewModel) {
    val messages by viewModel.currentMessages.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (messages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Empty Chat",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Begin conversing with Aura.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { message ->
                    AuraMessageBubble(message = message, onPlayVoice = { base64 ->
                        viewModel.stopPlayback()
                        AuraAudioHelper.playAudioFromBase64(viewModel.getApplication(), base64)
                    })
                }
            }
        }
    }
}

// ==========================================
// 7. MESSAGE BUBBLE WITH DESIGN IMITATION (SCREEN 4)
// ==========================================
@Composable
fun AuraMessageBubble(
    message: ChatMessage,
    onPlayVoice: (String) -> Unit
) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Glowing circular avatar as seen in image 4 response!
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, AuraAccentPink, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Aura",
                    tint = AuraAccentViolet,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Main text card bubble
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) Color(0xFF1B0C2F) else Color(0xFF140B23))
                    .border(1.dp, if (isUser) AuraAccentViolet.copy(alpha = 0.3f) else Color(0xFF281C3F), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = AuraTextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                    
                    if (message.audioBase64 != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF25133E))
                                .clickable { onPlayVoice(message.audioBase64) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play spoken text",
                                tint = AuraAccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play Voice", color = AuraAccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Screen 4 MIMIC Link: If message matches training discussion, insert the embedded link card!
            if (!isUser && (message.text.contains("training") || message.text.contains("schedule"))) {
                Spacer(modifier = Modifier.height(10.dp))
                
                // Embedded clickable link box
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF2E2243), RoundedCornerShape(12.dp))
                        .clickable { /* Simulate switching thread */ },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10071E))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Training",
                                tint = AuraAccentPink,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Training",
                                color = AuraTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "6d. ago",
                                color = AuraTextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View",
                                tint = AuraTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. TRANS-LUCENT SIDEBAR MENUS / SESSION SELECTORS
// ==========================================
@Composable
fun AuraSidebarContent(
    viewModel: AuraViewModel,
    onSignOut: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val activeSessionId by viewModel.currentSessionId.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    val selectedVoice by viewModel.selectedVoice.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090314))
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Drawer Header
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AURA SPACES",
                    color = AuraAccentPink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp
                )
                
                IconButton(onClick = onCloseDrawer) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close spaces",
                        tint = AuraTextPrimary
                    )
                }
            }
            
            Divider(color = Color(0xFF1E1430), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            // Action block to spawn a new Chat session
            Button(
                onClick = {
                    viewModel.createNewSession("Aura Session ${sessions.size + 1}")
                    Toast.makeText(context, "New Space initialized.", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF190D2E)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF321E53), RoundedCornerShape(10.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New chat", tint = AuraAccentCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Initialize New Space", color = AuraTextPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Session List
            Text(
                text = "Recent Conversational Threads",
                color = AuraTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions) { session ->
                    val isActive = session.id == activeSessionId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) Color(0xFF1E0D36) else Color.Transparent)
                            .border(1.dp, if (isActive) AuraAccentViolet else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.selectSession(session.id)
                                onCloseDrawer()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "Session",
                                tint = if (isActive) AuraAccentCyan else AuraTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = session.title,
                                color = AuraTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                        
                        if (sessions.size > 1) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = AuraAccentPink.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.deleteSession(session.id) }
                            )
                        }
                    }
                }
            }
        }

        // Drawer Bottom settings & profile controls
        Column {
            Divider(color = Color(0xFF1E1430), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

            // Choose Aura Prebuilt Voice
            Text(
                text = "Aura Voice Synthesis",
                color = AuraAccentCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Kore", "Puck", "Fenrir", "Aoede").forEach { voice ->
                    val isVoiceActive = voice == selectedVoice
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isVoiceActive) AuraAccentPink else Color(0xFF110722))
                            .clickable { viewModel.setVoice(voice) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = voice,
                            color = if (isVoiceActive) Color.White else AuraTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Info card & Sign out
            currentUser?.let { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF140825))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AuraAccentViolet),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.displayName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column {
                            Text(
                                text = user.displayName,
                                color = AuraTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = user.email,
                                color = AuraTextSecondary,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = AuraAccentPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
