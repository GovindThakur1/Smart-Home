package com.govind.smarthome.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.govind.smarthome.R
import com.govind.smarthome.api.ApiManager
import com.govind.smarthome.sessionmanager.SessionManager
import com.govind.smarthome.ui.theme.SmartHomeTheme

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit = {}) {
    val context = LocalContext.current
    val apiManager = remember { ApiManager() }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var loginClicked by remember { mutableStateOf(false) }

    LaunchedEffect(loginClicked) {
        if (loginClicked) {
            isLoading = true
            error = ""

            val storedUsername = apiManager.getESPUsername()
            val storedPassword = apiManager.getESPPassword()

            if (storedUsername == null || storedPassword == null) {
                error = "Failed to connect to ESP"
            } else if (username == storedUsername && password == storedPassword) {
                SessionManager.login(context, username)
                onLoginSuccess()
            } else {
                error = "Invalid credentials"
            }

            isLoading = false
            loginClicked = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // app logo
        Image(
            painter = painterResource(id = R.drawable.smarthomelogo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // app titile
        Text(
            text = "Login to Your Account",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Pw field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Login button
        Button(
            onClick = { loginClicked = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank() && password.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Login", color = Color.White)
        }

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = Color.Red,
                fontSize = 16.sp
            )
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SmartHomeTheme {
        LoginScreen()
    }
}
