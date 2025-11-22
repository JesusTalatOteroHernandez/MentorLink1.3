package mx.edu.utng.jtoh.mentorlink13.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroAsesoria(navController: NavController){
    var tema by remember { mutableStateOf("") }
    var modalidad by remember { mutableStateOf("") }
    var adicional by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize(), color = Color(180,180,180)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REGISTRO DE ASESORÍA")
                }
            }

            Spacer(Modifier.height(16.dp))

            Card {
                Column(modifier = Modifier.padding(16.dp)) {

                    OutlinedTextField(
                        value = tema,
                        onValueChange = { tema = it },
                        label = { Text("Tema de la asesoría") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = modalidad,
                        onValueChange = { modalidad = it },
                        label = { Text("Modalidad (Presencial / En línea)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = adicional,
                        onValueChange = { adicional = it },
                        label = { Text("Información adicional") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha (dd/mm/aaaa)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        label = { Text("Hora (HH:MM)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    var message by remember { mutableStateOf("") }
                    FirebaseApp.initializeApp(LocalContext.current)

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color(37,99,135)),
                        onClick = {
                            val usuario = auth.currentUser ?: return@Button.also { message = "Error: no hay usuario activo" }

                            if (tema.isBlank() || modalidad.isBlank() || fecha.isBlank() || hora.isBlank()) {
                                message = "Completa todos los campos obligatorios"
                                return@Button
                            }

                            val asesoriaId = db.collection("asesorias").document().id

                            val data = hashMapOf(
                                "id" to asesoriaId,
                                "idInstructor" to usuario.uid,
                                "idAprendiz" to "", // se llenará cuando el aprendiz agende
                                "tema" to tema,
                                "modalidad" to modalidad,
                                "adicional" to adicional,
                                "fecha" to fecha,
                                "hora" to hora
                            )

                            db.collection("asesorias")
                                .document(asesoriaId)
                                .set(data)
                                .addOnSuccessListener {
                                    message = "Asesoría registrada correctamente"
                                    navController.navigate("pantalla_inicio")
                                }
                                .addOnFailureListener {
                                    message = "Error al registrar la asesoría"
                                }
                        }
                    ) {
                        Text("Registrar asesoría")
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color(37,99,135)),
                        onClick = { navController.navigate("pantalla_inicio") }
                    ) { Text("Regresar") }

                    Spacer(Modifier.height(16.dp))

                    if (message.isNotEmpty()) Text(message, color = Color.Red)
                }
            }
        }
    }

}