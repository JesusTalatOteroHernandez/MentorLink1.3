package mx.edu.utng.jtoh.mentorlink13.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.contains

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun registroAprendiz(navController: NavController){
    var passwordNuevamente by remember { mutableStateOf("") }
    var nuevoPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val isValid = email.contains("@")
    var visible by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(180, 180, 180) //230, 230, 235
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            //Realiza un pequeño apartado para el titulo de la pantalla
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Aprendiz")
                }
            }

            Spacer(Modifier.height(16.dp))
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column {
                        Text(
                            "Ingresa tu correo electronico:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar el correo electronico
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Electronico") },
                            isError = !isValid,
                            modifier = Modifier.fillMaxWidth()
                        )
                        //Valida que cumpla con los requerimientos para un correo electronico
                        if (!isValid) {
                            Text(
                                "Correo Invalido", color = Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Ingresa una contraseña minimo de 6 caracteres:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar contraseña
                        OutlinedTextField(
                            value = nuevoPassword,
                            onValueChange = { nuevoPassword = it },
                            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()

                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Ingresa Nuevamente la contraseña:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar contraseña nuevamente
                        OutlinedTextField(
                            value = passwordNuevamente,
                            onValueChange = { passwordNuevamente = it },
                            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Ingrese su nombre:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar nombre
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Ingrese su apellido:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar apellido
                        OutlinedTextField(
                            value = apellido,
                            onValueChange = { apellido = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Ingrese su edad:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar edad
                        OutlinedTextField(
                            value = edad,
                            onValueChange = { edad = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                    }

                    Spacer(Modifier.height(16.dp))

                    //Boton para registrar el aprendiz
                    var message by remember { mutableStateOf("") }
                    val auth = FirebaseAuth.getInstance()
                    val db = FirebaseFirestore.getInstance()
                    Button(
                        modifier = Modifier,
                        colors = ButtonDefaults.buttonColors(Color(37, 99, 135)),
                        onClick = {
                                // 1. Validaciones básicas
                            if (email.isBlank() || nuevoPassword.isBlank() || passwordNuevamente.isBlank() ||
                                nombre.isBlank() || apellido.isBlank() || edad.isBlank()
                            ) {
                                message = "Completa todos los campos"
                                return@Button
                            }

                            if (nuevoPassword != passwordNuevamente) {
                                message = "Las contraseñas no coinciden"
                                return@Button
                            }

                            // 2. Crear usuario en Firebase Auth
                            auth.createUserWithEmailAndPassword(email, nuevoPassword)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                                        // 3. Crear objeto con los datos del aprendiz
                                        val aprendizData = hashMapOf(
                                            "nombre" to nombre,
                                            "apellido" to apellido,
                                            "edad" to edad,
                                            "correo" to email,
                                            "rol" to "aprendiz" // 👈 importante si manejarás roles
                                        )

                                        // 4. Guardar datos en Firestore
                                        db.collection("aprendices")
                                            .document(userId)
                                            .set(aprendizData)
                                            .addOnSuccessListener {
                                                message = "Registro exitoso"
                                            }
                                            .addOnFailureListener {
                                                message = "Error al guardar datos"
                                            }
                                            navController.navigate("pantalla_inicio")
                                    } else {
                                        message = "Error al crear usuario"
                                    }
                                }
                        }

                    ) {
                        Text("Registrar")
                    }

                    Spacer(Modifier.height(16.dp))

                    //Botom para regresar
                    Button(
                        modifier = Modifier,
                        colors = ButtonDefaults.buttonColors(Color(37, 99, 135)),
                        onClick = {
                            navController.navigate("pantalla_inicio")
                        }

                    ) {
                        Text("Regresar")
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

    }
}
