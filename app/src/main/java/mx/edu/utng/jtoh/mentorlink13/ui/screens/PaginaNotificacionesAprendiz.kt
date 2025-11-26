package mx.edu.utng.jtoh.mentorlink13.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaginaNotificacionesAprendiz(
    navController: NavController
) {
    val firestore: FirebaseFirestore = Firebase.firestore
    val auth: FirebaseAuth = Firebase.auth
    var evaluacionesPendientes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val currentUserId = auth.currentUser?.uid ?: "" // Aprendiz logueado

    LaunchedEffect(Unit) {
        loading = true

        // Obtener la fecha y hora actual
        val ahora = Calendar.getInstance()

        // Obtener todas las asesorías donde el usuario actual es el aprendiz
        firestore.collection("asesorias")
            .whereEqualTo("idAprendiz", currentUserId)
            .whereEqualTo("estado", "Aceptada")
            .get()
            .addOnSuccessListener { asesoriasSnapshot ->
                val asesoriasList = mutableListOf<Map<String, Any>>()

                asesoriasSnapshot.documents.forEach { asesoriaDoc ->
                    val idAsesoria = asesoriaDoc.id
                    val fechaStr = asesoriaDoc.getString("fecha") ?: ""
                    val horaStr = asesoriaDoc.getString("hora") ?: ""

                    // Parsear fecha y hora para verificar si ya pasó
                    try {
                        val partesFecha = fechaStr.split("/")
                        val partesHora = horaStr.split(" - ")[0].split(":")

                        val fechaAsesoria = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, partesFecha[0].toInt())
                            set(Calendar.MONTH, partesFecha[1].toInt() - 1)
                            set(Calendar.YEAR, partesFecha[2].toInt())
                            set(Calendar.HOUR_OF_DAY, partesHora[0].toInt())
                            set(Calendar.MINUTE, partesHora[1].toInt())
                        }

                        // Verificar si ya pasó la fecha
                        if (fechaAsesoria.before(ahora)) {
                            val idInstructor = asesoriaDoc.getString("idInstructor") ?: ""

                            // Verificar si ya existe una opinión del aprendiz hacia este instructor
                            firestore.collection("opiniones")
                                .whereEqualTo("idAsesoria", idAsesoria)
                                .whereEqualTo("idReceptor", idInstructor)
                                .whereEqualTo("idEmisor", currentUserId)
                                .get()
                                .addOnSuccessListener { opinionesSnapshot ->
                                    if (opinionesSnapshot.isEmpty) {
                                        // Obtener nombre del instructor
                                        firestore.collection("instructores")
                                            .document(idInstructor)
                                            .get()
                                            .addOnSuccessListener { instructorDoc ->
                                                val idUsuarioInstructor = instructorDoc.getString("idUsuario") ?: ""

                                                firestore.collection("usuarios")
                                                    .document(idUsuarioInstructor)
                                                    .get()
                                                    .addOnSuccessListener { usuarioDoc ->
                                                        val nombreInstructor = usuarioDoc.getString("nombre") ?: "Instructor"

                                                        val asesoriaData = mapOf(
                                                            "id" to idAsesoria,
                                                            "idInstructor" to idInstructor,
                                                            "nombreInstructor" to nombreInstructor,
                                                            "tema" to (asesoriaDoc.getString("tema") ?: ""),
                                                            "fecha" to fechaStr,
                                                            "hora" to horaStr
                                                        )

                                                        asesoriasList.add(asesoriaData)
                                                        evaluacionesPendientes = asesoriasList.toList()
                                                        loading = false
                                                    }
                                            }
                                    }
                                }
                        }
                    } catch (e: Exception) {
                        Log.e("NotificacionesAprendiz", "Error al parsear fecha: ${e.message}")
                    }
                }

                if (asesoriasList.isEmpty()) {
                    loading = false
                }
            }
            .addOnFailureListener { e ->
                Log.e("NotificacionesAprendiz", "Error al obtener asesorías: ${e.message}")
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2563EB),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
        ) {
            when {
                loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2563EB))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando evaluaciones pendientes...",
                            color = Color.Gray
                        )
                    }
                }

                evaluacionesPendientes.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aún no tienes evaluaciones pendientes",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(evaluacionesPendientes) { asesoria ->
                            EvaluacionCardAprendiz(
                                asesoria = asesoria,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvaluacionCardAprendiz(
    asesoria: Map<String, Any>,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 4.dp,
                color = Color(0xFFFBBF24),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Encabezado con ícono y nombre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFFDCEBFE),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Evalúa a ${asesoria["nombreInstructor"]}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = asesoria["tema"].toString(),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información de fecha y hora
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📅 ${asesoria["fecha"]}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "🕐 ${asesoria["hora"]}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mensaje motivacional
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCEBFE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Cuéntanos, ¿cómo fue tu experiencia con este instructor?",
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mensaje de ayuda
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Si tienes alguna duda puedes preguntarme",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de evaluación
            Button(
                onClick = {
                    navController.navigate("calificar_instructor/${asesoria["idInstructor"]}")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Evaluar Ahora",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}