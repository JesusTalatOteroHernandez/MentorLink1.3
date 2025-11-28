package mx.edu.utng.jtoh.mentorlink13.ui.screens.usuario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var visible by remember { mutableStateOf(false) }
    var visibleConfirm by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var estadoResidencia by remember { mutableStateOf("") }
    var municipioResidencia by remember { mutableStateOf("") }
    var expandedEstado by remember { mutableStateOf(false) }
    var expandedMunicipio by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var messageColor by remember { mutableStateOf(Color.Red) }
    var isLoading by remember { mutableStateOf(false) }
    var emailExistsMessage by remember { mutableStateOf("") }
    var isCheckingEmail by remember { mutableStateOf(false) }

    val municipios = estadosConMunicipios[estadoResidencia] ?: emptyList()

    // Validaciones mejoradas
    val isEmailValid = email.isNotEmpty() && email.contains("@") && email.contains(".")
    val isPasswordValid = nuevoPassword.length >= 8
    val passwordsMatch = nuevoPassword == passwordNuevamente && passwordNuevamente.isNotEmpty()

    // Verificar seguridad de contraseña
    val hasUpperCase = nuevoPassword.any { it.isUpperCase() }
    val hasLowerCase = nuevoPassword.any { it.isLowerCase() }
    val hasDigit = nuevoPassword.any { it.isDigit() }
    val hasSpecialChar = nuevoPassword.any { !it.isLetterOrDigit() }
    val isPasswordSecure = hasUpperCase && hasLowerCase && hasDigit && isPasswordValid

    val scrollState = rememberScrollState()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Verificar si el email ya existe en tiempo real
    LaunchedEffect (email) {
        if (email.isNotEmpty() && email.contains("@") && email.contains(".")) {
            isCheckingEmail = true
            emailExistsMessage = ""

            // Delay para no hacer demasiadas peticiones mientras el usuario escribe
            kotlinx.coroutines.delay(800)

            auth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener { result ->
                    isCheckingEmail = false
                    if (result.signInMethods?.isNotEmpty() == true) {
                        emailExistsMessage = "Este correo ya está registrado"
                    } else {
                        emailExistsMessage = ""
                    }
                }
                .addOnFailureListener {
                    isCheckingEmail = false
                    emailExistsMessage = ""
                }
        } else {
            emailExistsMessage = ""
            isCheckingEmail = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(180, 180, 180)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Título
            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Aprendiz",
                        tint = Color(0xFF2B5FDB),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Registro de Aprendiz",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Correo electrónico
                    Text(
                        "Correo electrónico",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            message = ""
                        },
                        label = { Text("ejemplo@correo.com") },
                        isError = (email.isNotEmpty() && !isEmailValid) || emailExistsMessage.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email"
                            )
                        },
                        trailingIcon = {
                            if (isCheckingEmail) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        singleLine = true,
                        enabled = !isLoading
                    )
                    if (email.isNotEmpty() && !isEmailValid) {
                        Text(
                            "Ingresa un correo válido",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }
                    if (emailExistsMessage.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                emailExistsMessage,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Contraseña
                    Text(
                        "Contraseña",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevoPassword,
                        onValueChange = {
                            nuevoPassword = it
                            message = ""
                        },
                        label = { Text("Mínimo 8 caracteres") },
                        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = nuevoPassword.isNotEmpty() && !isPasswordValid,
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
                                    contentDescription = if (visible) "Ocultar" else "Mostrar"
                                )
                            }
                        },
                        singleLine = true,
                        enabled = !isLoading
                    )

                    // Indicadores de seguridad de contraseña
                    if (nuevoPassword.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 8.dp)
                        ) {
                            Text(
                                "Requisitos de seguridad:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                            Spacer(Modifier.height(4.dp))

                            PasswordRequirement("Al menos 8 caracteres", isPasswordValid)
                            PasswordRequirement("Una letra mayúscula", hasUpperCase)
                            PasswordRequirement("Una letra minúscula", hasLowerCase)
                            PasswordRequirement("Un número", hasDigit)
                            PasswordRequirement("Un carácter especial (@, #, $, etc.)", hasSpecialChar)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Confirmar contraseña
                    Text(
                        "Confirmar contraseña",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordNuevamente,
                        onValueChange = {
                            passwordNuevamente = it
                            message = ""
                        },
                        label = { Text("Repite la contraseña") },
                        visualTransformation = if (visibleConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordNuevamente.isNotEmpty() && !passwordsMatch,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Confirm Password"
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { visibleConfirm = !visibleConfirm }) {
                                Icon(
                                    imageVector = if (visibleConfirm) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (visibleConfirm) "Ocultar" else "Mostrar"
                                )
                            }
                        },
                        singleLine = true,
                        enabled = !isLoading
                    )
                    if (passwordNuevamente.isNotEmpty() && !passwordsMatch) {
                        Text(
                            "Las contraseñas no coinciden",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Nombre
                    Text(
                        "Nombre",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            message = ""
                        },
                        label = { Text("Tu nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(16.dp))

                    // Apellido
                    Text(
                        "Apellido",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apellido,
                        onValueChange = {
                            apellido = it
                            message = ""
                        },
                        label = { Text("Tus apellidos") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(16.dp))

                    // Edad
                    Text(
                        "Edad",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = edad,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                edad = it
                                message = ""
                            }
                        },
                        label = { Text("Tu edad") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    if (edad.isNotEmpty() && (edad.toIntOrNull() == null || edad.toInt() < 12 || edad.toInt() > 100)) {
                        Text(
                            "Ingresa una edad válida (12-100)",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Estado de residencia
                    Text(
                        "Estado de residencia",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedEstado,
                        onExpandedChange = { expandedEstado = it && !isLoading },
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
                                .fillMaxWidth(),
                            enabled = !isLoading
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
                                        message = ""
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Municipio de residencia
                    Text(
                        "Municipio de residencia",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedMunicipio,
                        onExpandedChange = { expandedMunicipio = it && municipios.isNotEmpty() && !isLoading },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = municipioResidencia,
                            onValueChange = {},
                            readOnly = true,
                            enabled = municipios.isNotEmpty() && !isLoading,
                            label = {
                                Text(
                                    if (estadoResidencia.isEmpty())
                                        "Selecciona un estado primero"
                                    else
                                        "Selecciona un municipio"
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
                                        message = ""
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Mensaje de error/éxito
                    if (message.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (messageColor == Color.Red)
                                    Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = message,
                                color = messageColor,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // Botón de registrar
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2B5FDB)
                        ),
                        enabled = !isLoading,
                        onClick = {
                            // VALIDACIONES COMPLETAS
                            when {
                                email.isBlank() -> {
                                    message = "Por favor ingresa tu correo electrónico"
                                    messageColor = Color.Red
                                }
                                !isEmailValid -> {
                                    message = "El correo electrónico no es válido"
                                    messageColor = Color.Red
                                }
                                emailExistsMessage.isNotEmpty() -> {
                                    message = "Este correo ya está registrado. Usa otro correo"
                                    messageColor = Color.Red
                                }
                                nuevoPassword.isBlank() -> {
                                    message = "Por favor ingresa una contraseña"
                                    messageColor = Color.Red
                                }
                                !isPasswordValid -> {
                                    message = "La contraseña debe tener al menos 8 caracteres"
                                    messageColor = Color.Red
                                }
                                !isPasswordSecure -> {
                                    message = "La contraseña debe incluir mayúsculas, minúsculas y números"
                                    messageColor = Color.Red
                                }
                                passwordNuevamente.isBlank() -> {
                                    message = "Por favor confirma tu contraseña"
                                    messageColor = Color.Red
                                }
                                !passwordsMatch -> {
                                    message = "Las contraseñas no coinciden"
                                    messageColor = Color.Red
                                }
                                nombre.isBlank() -> {
                                    message = "Por favor ingresa tu nombre"
                                    messageColor = Color.Red
                                }
                                apellido.isBlank() -> {
                                    message = "Por favor ingresa tus apellidos"
                                    messageColor = Color.Red
                                }
                                edad.isBlank() -> {
                                    message = "Por favor ingresa tu edad"
                                    messageColor = Color.Red
                                }
                                edad.toIntOrNull() == null || edad.toInt() < 12 || edad.toInt() > 100 -> {
                                    message = "Por favor ingresa una edad válida (12-100)"
                                    messageColor = Color.Red
                                }
                                estadoResidencia.isBlank() -> {
                                    message = "Por favor selecciona tu estado de residencia"
                                    messageColor = Color.Red
                                }
                                municipioResidencia.isBlank() -> {
                                    message = "Por favor selecciona tu municipio de residencia"
                                    messageColor = Color.Red
                                }
                                else -> {
                                    // Todas las validaciones pasaron
                                    isLoading = true
                                    message = ""

                                    auth.createUserWithEmailAndPassword(email, nuevoPassword)
                                        .addOnSuccessListener { authResult ->
                                            val userId = authResult.user?.uid

                                            if (userId == null) {
                                                isLoading = false
                                                message = "Error al crear usuario"
                                                messageColor = Color.Red
                                                return@addOnSuccessListener
                                            }

                                            // Crear documento en aprendices
                                            val aprendizId = db.collection("aprendices").document().id
                                            val aprendizData = hashMapOf(
                                                "id" to aprendizId,
                                                "idUsuario" to userId,
                                                "puntuacion" to 0
                                            )

                                            db.collection("aprendices")
                                                .document(aprendizId)
                                                .set(aprendizData)
                                                .addOnSuccessListener {
                                                    // Crear documento en usuarios
                                                    val usuarioData = hashMapOf(
                                                        "id" to userId,
                                                        "nombre" to nombre.trim(),
                                                        "apellidos" to apellido.trim(),
                                                        "edad" to edad.toInt(),
                                                        "correoElectronico" to email.trim(),
                                                        "municipioResidencia" to municipioResidencia,
                                                        "estadoResidencia" to estadoResidencia,
                                                        "password" to "",
                                                        "tipoUsuario" to "aprendiz"
                                                    )

                                                    db.collection("usuarios")
                                                        .document(userId)
                                                        .set(usuarioData)
                                                        .addOnSuccessListener {
                                                            isLoading = false
                                                            message = "¡Registro exitoso! Redirigiendo..."
                                                            messageColor = Color(0xFF4CAF50)

                                                            // Navegar después de un breve delay
                                                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                                navController.navigate("pantalla_inicio") {
                                                                    popUpTo(0) { inclusive = true }
                                                                }
                                                            }, 1500)
                                                        }
                                                        .addOnFailureListener { exception ->
                                                            isLoading = false
                                                            message = "Error al guardar datos: ${exception.localizedMessage}"
                                                            messageColor = Color.Red
                                                        }
                                                }
                                                .addOnFailureListener { exception ->
                                                    isLoading = false
                                                    message = "Error al crear perfil: ${exception.localizedMessage}"
                                                    messageColor = Color.Red
                                                }
                                        }
                                        .addOnFailureListener { exception ->
                                            isLoading = false
                                            message = when {
                                                exception.message?.contains("email address is already") == true ->
                                                    "Este correo ya está registrado"
                                                exception.message?.contains("network") == true ->
                                                    "Error de conexión. Verifica tu internet"
                                                else -> "Error al crear cuenta: ${exception.localizedMessage}"
                                            }
                                            messageColor = Color.Red
                                        }
                                }
                            }
                        }
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Registrar", fontSize = 16.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Botón de regresar
                    OutlinedButton (
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = {
                            navController.navigate("pantalla_inicio") {
                                popUpTo("pantalla_inicio") { inclusive = true }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Regresar", fontSize = 16.sp)
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PasswordRequirement(text: String, isMet: Boolean) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isMet) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMet) Color(0xFF4CAF50) else Color.Gray
        )
    }
}