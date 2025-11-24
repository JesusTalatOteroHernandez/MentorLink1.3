package mx.edu.utng.jtoh.mentorlink13.ui.screens

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.contains

val estadosConMunicipios = mapOf(
    "Aguascalientes" to listOf("Aguascalientes", "Asientos", "Calvillo", "Jesús María"),
    "Baja California" to listOf("Ensenada", "Mexicali", "Tecate", "Tijuana"),
    "Baja California Sur" to listOf("La Paz", "Los Cabos", "Comondú", "Loreto"),
    "Campeche" to listOf("Carmen", "Campeche", "Champotón"),
    "Coahuila" to listOf("Saltillo", "Torreón", "Monclova"),
    "Colima" to listOf("Colima", "Manzanillo", "Tecomán"),
    "Chiapas" to listOf("Tuxtla", "Tapachula", "San Cristóbal"),
    "Chihuahua" to listOf("Chihuahua", "Juárez", "Cuauhtémoc"),
    "Ciudad de México" to listOf(
        "Álvaro Obregón", "Azcapotzalco", "Benito Juárez", "Coyoacán", "Cuajimalpa",
        "Cuauhtémoc", "Gustavo A. Madero", "Iztacalco", "Iztapalapa", "Magdalena Contreras",
        "Miguel Hidalgo", "Milpa Alta", "Tláhuac", "Tlalpan", "Venustiano Carranza", "Xochimilco"
    ),
    "Durango" to listOf("Durango", "Lerdo", "Gómez Palacio"),
    "Guanajuato" to listOf(
        "León", "Irapuato", "Celaya", "Salamanca", "Guanajuato",
        "Silao", "San Miguel de Allende", "Dolores Hidalgo", "Valle de Santiago",
        "Abasolo", "Pénjamo", "Cortazar", "San Luis de la Paz", "Moroleón"
    ),
    "Jalisco" to listOf("Guadalajara", "Zapopan", "Tlaquepaque", "Tonalá", "Puerto Vallarta"),
    "México" to listOf("Toluca", "Ecatepec", "Naucalpan", "Tlalnepantla"),
    "Nuevo León" to listOf("Monterrey", "San Nicolás", "San Pedro", "Apodaca"),
    "Puebla" to listOf("Puebla", "Tehuacán", "Atlixco"),
    "Querétaro" to listOf("Querétaro", "San Juan del Río", "Corregidora"),
    "Veracruz" to listOf("Veracruz", "Xalapa", "Coatzacoalcos"),
    "Yucatán" to listOf("Mérida", "Valladolid", "Tizimín")
)
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
    //var estado by remember { mutableStateOf("") }
    //var municipio by remember { mutableStateOf("") }

    var estadoResidencia by remember { mutableStateOf("") }
    var municipioResidencia by remember { mutableStateOf("") }

    var expandedEstado by remember { mutableStateOf(false) }
    var expandedMunicipio by remember { mutableStateOf(false) }

    val municipios = estadosConMunicipios[estadoResidencia] ?: emptyList()

    val scrollState = rememberScrollState()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(180, 180, 180) //230, 230, 235
    ) {
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

                    //Boton para registrar el aprendiz
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
                                nombre.isBlank() || apellido.isBlank() || edad.isBlank() || estadoResidencia.isBlank() || municipioResidencia.isBlank()
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

                                    // ============================
                                    // 1️⃣ DATOS PARA "aprendices"
                                    // ============================
                                    val aprendizId = db.collection("aprendices").document().id

                                    val aprendizData = hashMapOf(
                                        "id" to aprendizId,
                                        "idUsuario" to userId,
                                        "puntuacion" to 0 //Estara asi hasta que implementemos el sistema de puntuacion
                                    )

                                    db.collection("aprendices")
                                        .document(aprendizId)
                                        .set(aprendizData)
                                        .addOnSuccessListener {

                                            // ============================
                                            // 2️⃣ DATOS PARA "usuarios"
                                            // ============================
                                            val usuarioData = hashMapOf(
                                                "id" to userId,
                                                "nombre" to nombre,
                                                "apellidos" to apellido,
                                                "edad" to edad.toIntOrNull(),
                                                "correoElectronico" to email,
                                                "municipioResidencia" to municipioResidencia,
                                                "estadoResidencia" to estadoResidencia,
                                                "password" to "", // NO guardes contraseñas reales aquí
                                                "tipoUsuario" to "aprendiz"
                                            )

                                            db.collection("usuarios")
                                                .document(userId)
                                                .set(usuarioData)
                                                .addOnSuccessListener {
                                                    message = "Registro completado"
                                                    navController.navigate("pantalla_inicio")
                                                }
                                                .addOnFailureListener {
                                                    message = "Error al guardar en usuarios"
                                                }

                                        }
                                        .addOnFailureListener {
                                            message = "Error al guardar en aprendices"
                                        }

                                }
                                .addOnFailureListener {
                                    message = "Error al crear usuario"
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
