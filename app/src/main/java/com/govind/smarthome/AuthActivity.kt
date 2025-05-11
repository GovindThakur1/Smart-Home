package com.govind.smarthome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.govind.smarthome.sessionmanager.SessionManager
import com.govind.smarthome.ui.screens.LoginScreen
import com.govind.smarthome.ui.screens.SignupScreen
import com.govind.smarthome.ui.theme.SmartHomeTheme

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Checking if already logged in
        val context = this
        if (SessionManager.isLoggedIn(context)) {
            startActivity(Intent(context, MainActivity::class.java))
            finish()
        } else {
            setContent {
                SmartHomeTheme {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                    ) {
                        AuthScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreen() {
    var isLogin by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Button(onClick = { isLogin = true }, modifier = Modifier.weight(1f)) { Text("Login") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { isLogin = false },
                modifier = Modifier.weight(1f)
            ) { Text("Sign Up") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLogin) {
            LoginScreen(onLoginSuccess = {
                context.startActivity(Intent(context, MainActivity::class.java))
                (context as? Activity)?.finish()
            })
        } else {
            SignupScreen(onSignupSuccess = {
                context.startActivity(Intent(context, MainActivity::class.java))
                (context as? Activity)?.finish()
            })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    MaterialTheme {
        AuthScreen()
    }
}
