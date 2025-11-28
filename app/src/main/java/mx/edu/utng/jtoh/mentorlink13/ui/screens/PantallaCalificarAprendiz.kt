package mx.edu.utng.jtoh.mentorlink13.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCalificarAprendiz(
    navController: NavController,
    idAprendiz: String,
    idAsesoria: String
) {
    var experiencia by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var calificacion by remember { mutableStateOf(0) }
    var nombreAprendiz by remember { mutableStateOf("Cargando...") }
    var apellidosAprendiz by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var showMessage by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    // Cargar información del aprendiz
    LaunchedEffect(idAprendiz) {
        db.collection("aprendices")
            .document(idAprendiz)
            .get()
            .addOnSuccessListener { aprendizDoc ->
                val idUsuario = aprendizDoc.getString("idUsuario") ?: ""

                // Obtener datos del usuario
                db.collection("usuarios")
                    .document(idUsuario)
                    .get()
                    .addOnSuccessListener { usuarioDoc ->
                        nombreAprendiz = usuarioDoc.getString("nombre") ?: "Aprendiz"
                        apellidosAprendiz = usuarioDoc.getString("apellidos") ?: ""
                    }
            }
    }

    // Función para obtener el texto descriptivo de la calificación
    fun getCalificacionTexto(cal: Int): String {
        return when (cal) {
            1 -> "Muy malo"
            2 -> "Malo"
            3 -> "Regular"
            4 -> "Bueno"
            5 -> "Excelente"
            else -> "Sin calificar"
        }
    }

    Scaffold(
        topBar = {
            // TopBar Azul Superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B5FDB))
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Calificar Aprendiz",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        bottomBar = {
            // BottomBar Verde (Botón clickeable)
            Button(
                onClick = {
                    // VALIDACIONES
                    if (calificacion == 0) {
                        message = "Por favor selecciona una calificación"
                        showMessage = true
                        return@Button
                    }

                    if (currentUserId == null) {
                        message = "Error: Usuario no autenticado"
                        showMessage = true
                        return@Button
                    }

                    isLoading = true

                    // Obtener el ID del instructor actual
                    db.collection("instructores")
                        .whereEqualTo("idUsuario", currentUserId)
                        .get()
                        .addOnSuccessListener { instructorSnapshot ->
                            if (instructorSnapshot.documents.isNotEmpty()) {
                                val instructorDoc = instructorSnapshot.documents[0]
                                val instructorId = instructorDoc.getString("id") ?: ""

                                // Obtener idUsuario del aprendiz
                                db.collection("aprendices")
                                    .document(idAprendiz)
                                    .get()
                                    .addOnSuccessListener { aprendizDoc ->
                                        val idUsuarioAprendiz = aprendizDoc.getString("idUsuario") ?: ""

                                        // Crear documento en opiniones
                                        val opinionId = db.collection("opiniones").document().id

                                        val opinionData = hashMapOf<String, Any>(
                                            "idAsesoria" to idAsesoria,
                                            "idEmisor" to currentUserId,
                                            "idReceptor" to idUsuarioAprendiz,
                                            "opinion" to experiencia,
                                            "puntuacion" to calificacion
                                        )

                                        db.collection("opiniones")
                                            .document(opinionId)
                                            .set(opinionData)
                                            .addOnSuccessListener {
                                                // Actualizar puntuación del aprendiz
                                                db.collection("opiniones")
                                                    .whereEqualTo("idReceptor", idUsuarioAprendiz)
                                                    .get()
                                                    .addOnSuccessListener { opiniones ->
                                                        val totalOpiniones = opiniones.documents.size
                                                        val sumaPuntuaciones = opiniones.documents
                                                            .mapNotNull { it.getLong("puntuacion")?.toInt() }
                                                            .sum()

                                                        val promedio = if (totalOpiniones > 0) {
                                                            sumaPuntuaciones.toDouble() / totalOpiniones
                                                        } else {
                                                            0.0
                                                        }

                                                        // Actualizar puntuación en aprendices
                                                        db.collection("aprendices")
                                                            .document(idAprendiz)
                                                            .update("puntuacion", promedio)
                                                            .addOnSuccessListener {
                                                                isLoading = false
                                                                message = "Calificación enviada correctamente"
                                                                showMessage = true

                                                                // Navegar de regreso después de 1 segundo
                                                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                                    navController.popBackStack()
                                                                }, 1000)
                                                            }
                                                            .addOnFailureListener {
                                                                isLoading = false
                                                                message = "Error al actualizar puntuación"
                                                                showMessage = true
                                                            }
                                                    }
                                            }
                                            .addOnFailureListener {
                                                isLoading = false
                                                message = "Error al guardar opinión"
                                                showMessage = true
                                            }
                                    }
                            } else {
                                isLoading = false
                                message = "Error: Instructor no encontrado"
                                showMessage = true
                            }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            message = "Error al buscar instructor"
                            showMessage = true
                        }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .padding(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(13, 176, 123)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Row {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Enviar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Enviar Calificación",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Spacer(Modifier.height(25.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Aprendiz",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "$nombreAprendiz $apellidosAprendiz",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 25.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "¿Cómo fue tu experiencia con este aprendiz?",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraLight
                    )
                    Spacer(Modifier.height(15.dp))

                    // ESTRELLAS INTERACTIVAS
                    Row {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= calificacion) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Estrella $i",
                                tint = if (i <= calificacion) Color(232, 191, 54) else Color.LightGray,
                                modifier = Modifier
                                    .size(55.dp)
                                    .clickable { calificacion = i }
                            )
                            if (i < 5) Spacer(Modifier.width(11.dp))
                        }
                    }

                    Spacer(Modifier.height(15.dp))

                    // MOSTRAR CALIFICACIÓN
                    if (calificacion > 0) {
                        Text(
                            "$calificacion.0",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = Color(245, 158, 11)
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            getCalificacionTexto(calificacion),
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraLight,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            "Toca las estrellas para calificar",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraLight,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(45.dp))
                Column(modifier = Modifier.padding(15.dp)) {
                    Text(
                        "Cuéntanos más sobre tu experiencia (opcional)",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = experiencia,
                        onValueChange = { experiencia = it },
                        placeholder = {
                            Text(
                                "¿El aprendiz fue puntual? ¿Mostró interés y participación?",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraLight
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(
                                width = 1.dp,
                                color = if (isFocused) Color.Blue else Color.LightGray,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )
                }
            }

            // MENSAJE DE RESPUESTA
            if (showMessage) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        message,
                        modifier = Modifier.padding(horizontal = 15.dp),
                        color = if (message.contains("Error") || message.contains("Por favor"))
                            Color.Red
                        else
                            Color.Green,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}