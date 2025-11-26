package mx.edu.utng.jtoh.mentorlink13.ui.screens

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.ui.focus.onFocusChanged
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MarkunreadMailbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCalificarInstructor(
    navController: NavController,
    idInstructor: String,
) {
    val firestore = Firebase.firestore
    val auth = Firebase.auth
    var experiencia by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var puntuacionSeleccionada by remember { mutableStateOf(5) }
    var nombreInstructor by remember { mutableStateOf("Cargando...") }
    var enviando by remember { mutableStateOf(false) }
    var mostrarExito by remember { mutableStateOf(false) }

    val currentUserId = auth.currentUser?.uid ?: ""

    // Cargar datos del instructor
    LaunchedEffect(idInstructor) {
        firestore.collection("instructores")
            .document(idInstructor)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Buscar el usuario asociado al instructor
                    val idUsuario = document.getString("idUsuario") ?: ""
                    firestore.collection("usuarios")
                        .document(idUsuario)
                        .get()
                        .addOnSuccessListener { usuarioDoc ->
                            nombreInstructor = usuarioDoc.getString("nombre") ?: "Instructor"
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("CalificarInstructor", "Error al cargar instructor: ${e.message}")
                nombreInstructor = "Instructor"
            }
    }

    // Función para obtener el texto según la puntuación
    fun getTextoCalificacion(puntuacion: Int): String {
        return when (puntuacion) {
            1 -> "Muy malo"
            2 -> "Malo"
            3 -> "Regular"
            4 -> "Bueno"
            5 -> "Excelente"
            else -> ""
        }
    }

    // Función para enviar la calificación
    fun enviarCalificacion() {
        if (enviando) return
        enviando = true

        val opinion = hashMapOf(
            "idAsesoria" to "",
            "idEmisor" to currentUserId,
            "idReceptor" to idInstructor,
            "opinion" to experiencia,
            "puntuacion" to puntuacionSeleccionada
        )

        firestore.collection("opiniones")
            .add(opinion)
            .addOnSuccessListener {
                // Calcular nuevo promedio de puntuación
                firestore.collection("opiniones")
                    .whereEqualTo("idReceptor", idInstructor)
                    .get()
                    .addOnSuccessListener { opinionesSnapshot ->
                        var sumaPuntuaciones = 0
                        var totalOpiniones = 0

                        opinionesSnapshot.documents.forEach { doc ->
                            val puntuacion = doc.getLong("puntuacion")?.toInt() ?: 0
                            sumaPuntuaciones += puntuacion
                            totalOpiniones++
                        }

                        val promedio = if (totalOpiniones > 0) {
                            sumaPuntuaciones.toDouble() / totalOpiniones
                        } else {
                            0.0
                        }

                        // Actualizar puntuación en instructores
                        firestore.collection("instructores")
                            .document(idInstructor)
                            .update("puntuacion", promedio)
                            .addOnSuccessListener {
                                enviando = false
                                mostrarExito = true
                                // Regresar después de 1.5 segundos
                                Handler(Looper.getMainLooper()).postDelayed({
                                    navController.popBackStack()
                                }, 1500)
                            }
                            .addOnFailureListener { e ->
                                Log.e("CalificarInstructor", "Error al actualizar puntuación: ${e.message}")
                                enviando = false
                                navController.popBackStack()
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("CalificarInstructor", "Error al calcular promedio: ${e.message}")
                        enviando = false
                        navController.popBackStack()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("CalificarInstructor", "Error al guardar opinión: ${e.message}")
                enviando = false
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
                    text = "Calificar Instructor",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        bottomBar = {
            // BottomBar con botón de enviar
            Button(
                onClick = { enviarCalificacion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .padding(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(13, 176, 123)
                ),
                shape = MaterialTheme.shapes.medium,
                enabled = !enviando
            ) {
                if (enviando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
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
        Box(modifier = Modifier.fillMaxSize()) {
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
                            .size(width = 180.dp, height = 340.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Instructor",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            nombreInstructor,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 25.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "¿Cómo fue tu experiencia con este instructor?",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraLight
                        )
                        Spacer(Modifier.height(15.dp))

                        // Estrellas clickeables
                        Row {
                            for (i in 1..5) {
                                IconButton(
                                    onClick = { puntuacionSeleccionada = i },
                                    modifier = Modifier.size(55.dp)
                                ) {
                                    Icon(
                                        imageVector = if (i <= puntuacionSeleccionada) {
                                            Icons.Default.Star
                                        } else {
                                            Icons.Default.StarBorder
                                        },
                                        contentDescription = "Estrella $i",
                                        tint = Color(232, 191, 54),
                                        modifier = Modifier.size(55.dp)
                                    )
                                }
                                if (i < 5) {
                                    Spacer(Modifier.width(11.dp))
                                }
                            }
                        }

                        Spacer(Modifier.height(15.dp))
                        Text(
                            "$puntuacionSeleccionada.0",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = Color(245, 158, 11)
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            getTextoCalificacion(puntuacionSeleccionada),
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraLight,
                            fontSize = 12.sp
                        )
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
                                    "¿Qué te pareció la asesoría? ¿El instructor fue claro y paciente?",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraLight
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .size(width = 180.dp, height = 200.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (isFocused) Color.Blue else Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .onFocusChanged { isFocused = it.isFocused },
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
            }

            // Mensaje de éxito
            if (mostrarExito) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Éxito",
                                tint = Color(13, 176, 123),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "¡Calificación enviada!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Gracias por tu opinión",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}