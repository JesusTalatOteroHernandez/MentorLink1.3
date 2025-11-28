package mx.edu.utng.jtoh.mentorlink13.ui.screens.usuario

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.contains

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun pantallaDeInicio(navController: NavController) {
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var messageColor by remember { mutableStateOf(Color.Red) }
    var isLoading by remember { mutableStateOf(false) }

    // Validaciones
    val isEmailValid = email.isNotEmpty() && email.contains("@") && email.contains(".")
    val isPasswordValid = password.length >= 6

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(180, 180, 180)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(150.dp))

            Text(
                text = "MentorLink",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Blue,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Conectando estudiantes con tutores",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Campo de correo
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            message = "" // Limpiar mensaje al escribir
                        },
                        label = { Text("Correo Electrónico") },
                        isError = email.isNotEmpty() && !isEmailValid,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email"
                            )
                        },
                        singleLine = true,
                        enabled = !isLoading
                    )

                    // Mensaje de error para email
                    if (email.isNotEmpty() && !isEmailValid) {
                        Text(
                            text = "Ingresa un correo válido",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Campo de contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            message = "" // Limpiar mensaje al escribir
                        },
                        label = { Text("Contraseña") },
                        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = password.isNotEmpty() && !isPasswordValid,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password"
                            )
                        },
                        trailingIcon = {
                            IconButton (onClick = { visible = !visible }) {
                                Icon(
                                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        singleLine = true,
                        enabled = !isLoading
                    )

                    // Mensaje de error para contraseña
                    if (password.isNotEmpty() && !isPasswordValid) {
                        Text(
                            text = "La contraseña debe tener al menos 6 caracteres",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Botón de iniciar sesión
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = {
                            // Validar campos vacíos
                            when {
                                email.isEmpty() -> {
                                    message = "Por favor ingresa tu correo"
                                    messageColor = Color.Red
                                }
                                password.isEmpty() -> {
                                    message = "Por favor ingresa tu contraseña"
                                    messageColor = Color.Red
                                }
                                !isEmailValid -> {
                                    message = "El correo ingresado no es válido"
                                    messageColor = Color.Red
                                }
                                !isPasswordValid -> {
                                    message = "La contraseña debe tener al menos 6 caracteres"
                                    messageColor = Color.Red
                                }
                                else -> {
                                    // Todo válido, proceder con el login
                                    isLoading = true
                                    message = ""

                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val userId = auth.currentUser?.uid

                                                if (userId == null) {
                                                    message = "Error inesperado. Intenta nuevamente"
                                                    messageColor = Color.Red
                                                    isLoading = false
                                                    return@addOnCompleteListener
                                                }

                                                // Buscar al usuario en Firestore
                                                db.collection("usuarios")
                                                    .document(userId)
                                                    .get()
                                                    .addOnSuccessListener { document ->
                                                        isLoading = false

                                                        if (!document.exists()) {
                                                            message = "Usuario no encontrado"
                                                            messageColor = Color.Red
                                                            return@addOnSuccessListener
                                                        }

                                                        val tipo = document.getString("tipoUsuario") ?: ""

                                                        when (tipo) {
                                                            "tutor" -> {
                                                                navController.navigate("pantalla_de_inicio_tutor") {
                                                                    popUpTo("pantalla_de_inicio") { inclusive = true }
                                                                }
                                                            }
                                                            "aprendiz" -> {
                                                                navController.navigate("pantalla_de_inicio_aprendiz") {
                                                                    popUpTo("pantalla_de_inicio") { inclusive = true }
                                                                }
                                                            }
                                                            else -> {
                                                                message = "Tipo de usuario no válido"
                                                                messageColor = Color.Red
                                                            }
                                                        }
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        isLoading = false
                                                        message = "Error al conectar con el servidor"
                                                        messageColor = Color.Red
                                                    }

                                            } else {
                                                isLoading = false
                                                // Mensajes específicos según el error
                                                message = when (task.exception) {
                                                    is FirebaseAuthInvalidUserException -> "Este correo no está registrado"
                                                    is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
                                                    else -> "Error al iniciar sesión. Verifica tu conexión"
                                                }
                                                messageColor = Color.Red
                                            }
                                        }
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2B5FDB)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Iniciar sesión", fontSize = 16.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Mensaje de error/éxito
                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            color = messageColor,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Botón de registro
                    OutlinedButton (
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = {
                            navController.navigate("pantalla_registro")
                        },
                        enabled = !isLoading
                    ) {
                        Text("Registrarse", fontSize = 16.sp)
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}