package mx.edu.utng.jtoh.mentorlink13.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.contains

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun pantallaDeInicio(navController: NavController) {
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val isValid = email.contains("@")
    var visible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(180, 180, 180) //230, 230, 235
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(150.dp))  // <-- mueve hacia abajo

            Text(
                text = "MentorLink",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Blue
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
                modifier = Modifier.padding(8.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electronico") },
                    isError = !isValid,
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    //Valida que cumpla con los requerimientos para un correo electronico
                    if (!isValid) {
                        Text(
                            "Correo Invalido", color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                //Apartado para ingresar contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)

                )
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Checkbox(
                        checked = visible,
                        onCheckedChange = { visible = it }
                    )
                    Text("Ver contraseña")
                }

                //Boton para iniciar sesion
                val auth = FirebaseAuth.getInstance()
                val db = FirebaseFirestore.getInstance()

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 50.dp),
                    onClick = {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    val userId = auth.currentUser?.uid

                                    if (userId == null) {
                                        message = "Error inesperado"
                                        return@addOnCompleteListener
                                    }

                                    // 🔥 Buscar al usuario en Firestore
                                    db.collection("usuarios")
                                        .document(userId)
                                        .get()
                                        .addOnSuccessListener { document ->

                                            if (!document.exists()) {
                                                message = "Usuario no encontrado en Firestore"
                                                return@addOnSuccessListener
                                            }

                                            val tipo = document.getString("tipoUsuario") ?: ""

                                            when (tipo) {

                                                "tutor" -> {
                                                    // Ir a la pantalla del instructor
                                                    navController.navigate("pantalla_de_inicio_tutor")
                                                }

                                                "aprendiz" -> {
                                                    // Ir a la pantalla principal del aprendiz
                                                    navController.navigate("pantalla_de_inicio_aprendiz")
                                                }

                                                else -> {
                                                    message = "Tipo de usuario no válido: $tipo"
                                                }
                                            }
                                        }
                                        .addOnFailureListener {
                                            message = "Error al obtener datos del usuario"
                                        }

                                } else {
                                    message = "Correo o contraseña incorrectas"
                                }
                            }
                    }
                ) {
                    Text("Iniciar sesión")
                }

                Spacer(Modifier.height(2.dp))
                Text(message)

                // 🔥 Botón que navega a la pantalla de registro
                Button(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 50.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    onClick = {
                        navController.navigate("pantalla_registro")
                    }
                ) {
                    Text("Registrate")
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

