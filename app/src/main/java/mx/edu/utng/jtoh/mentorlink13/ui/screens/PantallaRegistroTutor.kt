package mx.edu.utng.jtoh.mentorlink13.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.contains


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun registroTutor(navController: NavController){
    var passwordNuevamente by remember { mutableStateOf("") }
    var nuevoPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val isValid = email.contains("@")
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }

    var estadoResidencia by remember { mutableStateOf("") }
    var municipioResidencia by remember { mutableStateOf("") }

    val municipios = estadosConMunicipios[estadoResidencia] ?: emptyList()

    //Variable para el Scrol de la pantalla
    val scrollState = rememberScrollState()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(180, 180, 180) //230, 230, 235
    ) {
        //Mantiene todos los componentes adnetro centralizado
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(scrollState)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top

        ) {
            //Realiza un pequeño apartado para el titulo de la pantalla
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TUTOR")
                }
            }

            Spacer(Modifier.height(16.dp))

            //Apartado para que los apartados de texto esten centrados
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
                            "Ingresa una contraseña:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar contraseña
                        OutlinedTextField(
                            value = nuevoPassword,
                            onValueChange = { nuevoPassword = it },
                            visualTransformation = PasswordVisualTransformation(),
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
                            visualTransformation = PasswordVisualTransformation(),
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

                        //COMBOBOX PARA EL ESTADO DE RESIDENCIA
                        Spacer(Modifier.height(16.dp))
                        Text("Estado de residencia:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(3.dp))

                        var expandedEstado by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expandedEstado,
                            onExpandedChange = { expandedEstado = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            OutlinedTextField(
                                value = estadoResidencia,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Selecciona un estado") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstado)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedEstado,
                                onDismissRequest = { expandedEstado = false }
                            ) {
                                estadosConMunicipios.keys.forEach { estado ->
                                    DropdownMenuItem(
                                        text = { Text(estado) },
                                        onClick = {
                                            estadoResidencia = estado
                                            municipioResidencia = ""
                                            expandedEstado = false
                                        }
                                    )
                                }
                            }
                        }

                        //COMBOBOX PARA EL MUNICIPIO DE RESIDENCIA
                        Spacer(Modifier.height(16.dp))
                        Text("Municipio de residencia:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(3.dp))

                        var expandedMunicipio by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expandedMunicipio,
                            onExpandedChange = { expandedMunicipio = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            OutlinedTextField(
                                value = municipioResidencia,
                                onValueChange = {},
                                readOnly = true,
                                enabled = municipios.isNotEmpty(),
                                label = {
                                    Text(
                                        if (estadoResidencia.isEmpty())
                                            "Seleccione un estado primero"
                                        else
                                            "Seleccione un municipio"
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMunicipio)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedMunicipio,
                                onDismissRequest = { expandedMunicipio = false }
                            ) {
                                municipios.forEach { municipio ->
                                    DropdownMenuItem(
                                        text = { Text(municipio) },
                                        onClick = {
                                            municipioResidencia = municipio
                                            expandedMunicipio = false
                                        }
                                    )
                                }
                            }
                        }

                    }

                    Spacer(Modifier.height(16.dp))
                    //Boton para registrar tutor
                    var message by remember { mutableStateOf("") }
                    val auth = FirebaseAuth.getInstance()
                    val db = FirebaseFirestore.getInstance()
                    val context = LocalContext.current
                    FirebaseApp.initializeApp(context)
                    Button(
                        modifier = Modifier,
                        colors = ButtonDefaults.buttonColors(Color(37, 99, 135)),
                        onClick = {
                            // VALIDACIONES
                            if (email.isBlank() || nuevoPassword.isBlank() || passwordNuevamente.isBlank() ||
                                nombre.isBlank() || apellido.isBlank() || edad.isBlank() ||
                                estadoResidencia.isBlank() || municipioResidencia.isBlank()
                            ) {
                                message = "Completa todos los campos"
                                return@Button
                            }

                            if (nuevoPassword != passwordNuevamente) {
                                message = "Las contraseñas no coinciden"
                                return@Button
                            }

                            // CREAR USUARIO EN AUTH
                            auth.createUserWithEmailAndPassword(email, nuevoPassword)
                                .addOnSuccessListener { authResult ->

                                    val userId = authResult.user?.uid ?: return@addOnSuccessListener

                                    // =======================================
                                    // 1️⃣ CREAR DOCUMENTO EN "instructores"
                                    // =======================================
                                    val instructorId = db.collection("instructores").document().id

                                    val instructorData = hashMapOf(
                                        "id" to instructorId,
                                        "idUsuario" to userId,
                                        "puntuacion" to "" //Estara asi hasta que implementemos el sistema de puntuacion
                                    )

                                    db.collection("instructores")
                                        .document(instructorId)
                                        .set(instructorData)
                                        .addOnSuccessListener {

                                            // =======================================
                                            // 2️⃣ GUARDAR DATOS EN "usuarios"
                                            // =======================================
                                            val usuarioData = hashMapOf(
                                                "id" to userId,
                                                "nombre" to nombre,
                                                "apellidos" to apellido,
                                                "edad" to edad.toIntOrNull(),
                                                "correoElectronico" to email,
                                                "municipioResidencia" to municipioResidencia,
                                                "estadoResidencia" to estadoResidencia,
                                                "password" to "", // NO guardar contraseñas reales
                                                "tipoUsuario" to "tutor"   // ⭐ para diferenciarlos
                                            )

                                            db.collection("usuarios")
                                                .document(userId)
                                                .set(usuarioData)
                                                .addOnSuccessListener {
                                                    message = "Tutor registrado correctamente"
                                                    navController.navigate("pantalla_registro_asesoria")
                                                }
                                                .addOnFailureListener {
                                                    message = "Error al guardar en usuarios"
                                                }
                                        }
                                        .addOnFailureListener {
                                            message = "Error al guardar en instructores"
                                        }
                                }
                                .addOnFailureListener {
                                    message = "Error al crear usuario"
                                }
                        }

                    ) {
                        Text("Siguiente")
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
