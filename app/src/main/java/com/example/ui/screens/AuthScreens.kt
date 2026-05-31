package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BackgroundParticles
import com.example.ui.components.GlassmorphicCard
import com.example.ui.viewmodel.EcoScreen
import com.example.ui.viewmodel.EcoViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthNavigator(viewModel: EcoViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    BackgroundParticles {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f) with
                            fadeOut(animationSpec = tween(300))
                },
                label = "ScreenTransition"
            ) { target ->
                when (target) {
                    EcoScreen.LOGIN -> LoginScreen(viewModel)
                    EcoScreen.REGISTER_S1 -> RegisterS1Screen(viewModel)
                    EcoScreen.REGISTER_S2 -> RegisterS2Screen(viewModel)
                    EcoScreen.REGISTER_S3 -> RegisterS3Screen(viewModel)
                    EcoScreen.VERIFICATION -> VerificationScreen(viewModel)
                    EcoScreen.PROFILE_SETUP -> ProfileSetupScreen(viewModel)
                    else -> Box {} // Dashboard handles its own UI
                }
            }
        }
    }
}

// Natural Tones Aesthetic Palette
val TextPrimary = Color(0xFF1A1C18)
val TextSecondary = Color(0xFF3E4A3B)
val BrandGreen = Color(0xFF386B41)
val BorderSage = Color(0xFFDDE5DB)
val TintCap = Color(0xFFD7E8D3)

@Composable
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = BrandGreen,
    unfocusedBorderColor = BorderSage,
    cursorColor = BrandGreen,
    focusedLabelColor = BrandGreen,
    unfocusedLabelColor = TextSecondary
)

@Composable
fun LoginScreen(viewModel: EcoViewModel) {
    val email by viewModel.loginEmail.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val scrollState = rememberScrollState()

    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // App Logo Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "App Logo",
                tint = BrandGreen,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "EcoFriend",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = "Welcome Back",
            color = TextSecondary,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.loginEmail.value = it },
                    label = { Text("Email") },
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.loginPassword.value = it },
                    label = { Text("Password") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Lock else Icons.Default.Share,
                                contentDescription = "Toggle password visibility",
                                tint = TextSecondary
                            )
                        }
                    },
                    colors = customTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("password_input"),
                    singleLine = true
                )

                if (loginError.isNotBlank()) {
                    Text(
                        text = loginError,
                        color = Color(0xFFBA1A1A),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Login Button
                Button(
                    onClick = { viewModel.performLogin() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button")
                ) {
                    Text(
                        text = "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Social SSO Connect Headers
                Text(
                    text = "Continue with student portal",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Google SSO
                    OutlinedButton(
                        onClick = { viewModel.performSocialLogin("Google") },
                        border = BorderStroke(1.dp, BorderSage),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(45.dp)
                    ) {
                        Text("Google", color = TextPrimary, fontSize = 13.sp)
                    }

                    // Microsoft SSO (School Focused)
                    OutlinedButton(
                        onClick = { viewModel.performSocialLogin("Microsoft") },
                        border = BorderStroke(1.dp, BorderSage),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(45.dp)
                    ) {
                        Text("Microsoft", color = TextPrimary, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Direct Phone OTP Trigger Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.phoneInput.value = ""; viewModel.navigateTo(EcoScreen.REGISTER_S3) }
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = TintCap),
                    border = BorderStroke(0.8.dp, BorderSage)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "OTP Login", tint = BrandGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Alternative: Phone OTP Login", color = TextPrimary, fontSize = 13.sp)
                    }
                }
            }
        }

        TextButton(
            onClick = { viewModel.navigateTo(EcoScreen.REGISTER_S1) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Don't have an account? Sign Up", color = BrandGreen, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun RegisterS1Screen(viewModel: EcoViewModel) {
    val name by viewModel.regName.collectAsState()
    val email by viewModel.regEmail.collectAsState()
    val password by viewModel.regPassword.collectAsState()
    val confirmPassword by viewModel.regConfirmPassword.collectAsState()
    val s1Error by viewModel.regS1Error.collectAsState()

    val passStrength = viewModel.checkPasswordStrength(password)
    val meterColor = when (passStrength) {
        "Weak" -> Color(0xFFBA1A1A)
        "Medium" -> Color(0xFFD38D10)
        "Strong" -> Color(0xFF386B41)
        "Very Strong" -> Color(0xFF1B5E20)
        else -> Color.DarkGray
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Student Account",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Step 1: Credential Setup",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        GlassmorphicCard(modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)) {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.regName.value = it },
                    label = { Text("Full Name") },
                    colors = customTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.regEmail.value = it },
                    label = { Text("Email Address") },
                    colors = customTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.regPassword.value = it },
                    label = { Text("Password") },
                    colors = customTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                // Password Strength Meter
                if (password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Strength: $passStrength", color = TextSecondary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(BorderSage)
                        ) {
                            val multiplier = when (passStrength) {
                                "Weak" -> 0.25f
                                "Medium" -> 0.5f
                                "Strong" -> 0.75f
                                "Very Strong" -> 1.0f
                                else -> 0f
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(multiplier)
                                    .background(meterColor)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { viewModel.regConfirmPassword.value = it },
                    label = { Text("Confirm Password") },
                    colors = customTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                if (s1Error.isNotBlank()) {
                    Text(
                        text = s1Error,
                        color = Color(0xFFBA1A1A),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.submitRegS1() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Next Step", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        TextButton(
            onClick = { viewModel.navigateTo(EcoScreen.LOGIN) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Back to Login", color = BrandGreen)
        }
    }
}

@Composable
fun RegisterS2Screen(viewModel: EcoViewModel) {
    val school by viewModel.regSchool.collectAsState()
    val cls by viewModel.regClass.collectAsState()
    val city by viewModel.regCity.collectAsState()
    val state by viewModel.regState.collectAsState()
    val s2Error by viewModel.regS2Error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Academic Details",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Step 2: School & Location Setup",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        GlassmorphicCard(modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)) {
            Column {
                OutlinedTextField(
                    value = school,
                    onValueChange = { viewModel.regSchool.value = it },
                    label = { Text("School / College Name") },
                    colors = customTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = cls,
                    onValueChange = { viewModel.regClass.value = it },
                    label = { Text("Class / Grade") },
                    colors = customTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { viewModel.regCity.value = it },
                        label = { Text("City") },
                        colors = customTextFieldColors(),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state,
                        onValueChange = { viewModel.regState.value = it },
                        label = { Text("State") },
                        colors = customTextFieldColors(),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (s2Error.isNotBlank()) {
                    Text(
                        text = s2Error,
                        color = Color(0xFFBA1A1A),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.submitRegS2() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Next Step", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = { viewModel.navigateTo(EcoScreen.REGISTER_S1) },
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text("Back to Step 1", color = BrandGreen)
        }
    }
}

@Composable
fun RegisterS3Screen(viewModel: EcoViewModel) {
    val preferredLanguage by viewModel.selectedLanguage.collectAsState()
    val phoneNum by viewModel.phoneInput.collectAsState()
    val typedOTP by viewModel.otpTypedCode.collectAsState()
    val otpStatus by viewModel.otpStatus.collectAsState()

    // CAPTCHA Bot protection fields
    val cap1 by viewModel.captchaNum1.collectAsState()
    val cap2 by viewModel.captchaNum2.collectAsState()
    val enteredCap by viewModel.captchaUserInput.collectAsState()
    val capSuccess by viewModel.captchaSuccess.collectAsState()
    val capErr by viewModel.captchaError.collectAsState()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Language & Bot Security",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Step 3: Preference & Verification",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        GlassmorphicCard(modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)) {
            Column {
                Text(
                    text = "🌱 Choose Preferred Language",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Language selector grid (English, Hindi, Telugu)
                val languages = listOf("English", "Hindi", "Telugu")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    languages.forEach { lang ->
                        val selected = preferredLanguage == lang
                        Button(
                            onClick = { viewModel.selectLanguage(lang) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) BrandGreen else Color(0xFFF1F5F1),
                                contentColor = if (selected) Color.White else TextPrimary
                            ),
                            border = if (!selected) BorderStroke(1.dp, BorderSage) else null,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when(lang) {
                                    "English" -> "EN 🇬🇧"
                                    "Hindi" -> "HI 🇮🇳"
                                    else -> "TE 🇮🇳"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Alternative Option: Phone OTP
                Text(
                    text = "🔐 Phone OTP Activation",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = phoneNum,
                        onValueChange = { viewModel.phoneInput.value = it },
                        placeholder = { Text("Mobile Number", color = TextSecondary, fontSize = 13.sp) },
                        colors = customTextFieldColors(),
                        modifier = Modifier.weight(1.3f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = { viewModel.sendOTP() },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        shape = RoundedCornerShape(8.dp),
                        enabled = otpStatus != "VERIFIED",
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text(
                            text = if (otpStatus == "SENT") "Resend" else "Send OTP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                if (otpStatus == "SENT" || otpStatus == "ERROR") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = typedOTP,
                            onValueChange = { viewModel.otpTypedCode.value = it },
                            placeholder = { Text("Enter OTP (123456)", color = TextSecondary, fontSize = 13.sp) },
                            colors = customTextFieldColors(),
                            modifier = Modifier.weight(1.3f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = { viewModel.verifyOTP() },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text("Verify", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                if (otpStatus == "ERROR") {
                    Text("Incorrect Mobile Number or OTP code.", color = Color(0xFFBA1A1A), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                } else if (otpStatus == "VERIFIED") {
                    Text("OTP Verified Successfully!", color = BrandGreen, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // BOT PROTECTION CAPTCHA
                Text(
                    text = "🤖 CAPTCHA Bot Protection",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Please solve this quick equation to proceed:",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TintCap)
                        .border(1.dp, BorderSage, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "What is $cap1 + $cap2?",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = enteredCap,
                            onValueChange = { viewModel.captchaUserInput.value = it },
                            placeholder = { Text("?", color = TextSecondary) },
                            singleLine = true,
                            colors = customTextFieldColors(),
                            shape = RoundedCornerShape(6.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(60.dp).height(50.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                val verified = viewModel.verifyCaptcha()
                                if (!verified) {
                                    viewModel.generateNewCaptcha()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (capSuccess) Icons.Default.CheckCircle else Icons.Default.Refresh,
                                contentDescription = "Verify captcha",
                                tint = if (capSuccess) BrandGreen else TextSecondary
                            )
                        }
                    }
                }

                if (capErr.isNotBlank()) {
                    Text(capErr, color = Color(0xFFBA1A1A), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Face Login Preview switch (Future Enablement check)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x0C000000))
                        .border(0.8.dp, BorderSage, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Student Face Login", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Simulate facial mapping at launch (Beta)", color = TextSecondary, fontSize = 10.sp)
                    }
                    Switch(
                        checked = false,
                        onCheckedChange = { /* Placeholder Future switch API callback */ },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandGreen,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = BorderSage
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.completeRegistration() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = capSuccess, // Enabled only if CAPTCHA passes
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Register & Activate", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = { viewModel.navigateTo(EcoScreen.REGISTER_S2) }) {
            Text("Back to Academic Details", color = BrandGreen)
        }
    }
}

@Composable
fun VerificationScreen(viewModel: EcoViewModel) {
    val emailVerified by viewModel.emailVerified.collectAsState()
    val regEmail by viewModel.regEmail.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email sent",
                    tint = BrandGreen,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Verify Your Student Email",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "We have dispatched a validation link to\n${regEmail.ifBlank { "your-email@school.edu" }}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                HorizontalDivider(color = BorderSage)

                Spacer(modifier = Modifier.height(20.dp))

                // Simulator button to activate link
                Button(
                    onClick = { viewModel.simulateEmailActivation() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Simulate Clicking Activation Link", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Note: In a live system, the student clicks this link inside their email inbox to securely activate database access.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(viewModel: EcoViewModel) {
    val favPlant by viewModel.favoritePlant.collectAsState()
    val loc by viewModel.setupLocation.collectAsState()
    val goal by viewModel.setupGoal.collectAsState()
    val trees by viewModel.setupTrees.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌱 Student Eco Profile",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Fine tune your initial planting target",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        GlassmorphicCard(modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)) {
            Column {
                OutlinedTextField(
                    value = favPlant,
                    onValueChange = { viewModel.favoritePlant.value = it },
                    label = { Text("🌱 Favorite Plant (e.g. Tulsi, Rose)") },
                    colors = customTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = loc,
                    onValueChange = { viewModel.setupLocation.value = it },
                    label = { Text("📍 Plantation Location (e.g. Balcony, Yard)") },
                    colors = customTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Plantation Goal sliders and helpers
                Text("🎯 Annual Plantation Goal: $goal Plants", color = TextPrimary, fontSize = 13.sp)
                Slider(
                    value = goal.toFloatOrNull() ?: 5f,
                    onValueChange = { viewModel.setupGoal.value = it.toInt().toString() },
                    valueRange = 1f..30f,
                    steps = 29,
                    colors = SliderDefaults.colors(
                        thumbColor = BrandGreen,
                        activeTrackColor = BrandGreen,
                        inactiveTrackColor = BorderSage
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("🌳 Trees to grow this year: $trees", color = TextPrimary, fontSize = 13.sp)
                Slider(
                    value = trees.toFloatOrNull() ?: 2f,
                    onValueChange = { viewModel.setupTrees.value = it.toInt().toString() },
                    valueRange = 0f..10f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = BrandGreen,
                        activeTrackColor = BrandGreen,
                        inactiveTrackColor = BorderSage
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.completeProfileSetup() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Confirm & Launch Dashboard", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
