package mx.edu.utng.jtoh.mentorlink13.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun PaginaNotificacionesInstructor(
    navController: NavController,
) {
    val firestore = Firebase.firestore
    val auth = Firebase.auth
    var evaluacionesPendientes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val currentUserId = auth.currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        loading = true

        // Primero obtener el idInstructor
        firestore.collection("instructores")
            .whereEqualTo("idUsuario", currentUserId)
            .get()
            .addOnSuccessListener { instructoresSnapshot ->
                val instructorDoc = instructoresSnapshot.documents.firstOrNull()
                val idInstructor = instructorDoc?.id ?: ""

                if (idInstructor.isEmpty()) {
                    Log.e("NotificacionesScreen", "No se encontró instructor para este usuario")
                    loading = false
                    return@addOnSuccessListener
                }

                // Obtener la fecha y hora actual
                val ahora = Calendar.getInstance()

                // AHORA SÍ obtener las asesorías (dentro del success anterior)
                firestore.collection("asesorias")
                    .whereEqualTo("idInstructor", idInstructor)
                    .whereEqualTo("estado", "Finalizada")
                    .get()
                    .addOnSuccessListener { asesoriasSnapshot ->
                        val asesoriasList = mutableListOf<Map<String, Any>>()
                        var procesadas = 0
                        val totalAsesorias = asesoriasSnapshot.documents.size

                        if (totalAsesorias == 0) {
                            loading = false
                            evaluacionesPendientes = emptyList()
                            return@addOnSuccessListener
                        }

                        asesoriasSnapshot.documents.forEach { asesoriaDoc ->
                            val idAsesoria = asesoriaDoc.id
                            val idAprendiz = asesoriaDoc.getString("idAprendiz") ?: "" // Declarar aquí dentro

                            if (idAprendiz.isEmpty()) {
                                Log.e("NotificacionesScreen", "ERROR: idAprendiz vacío en asesoría $idAsesoria")
                                procesadas++
                                if (procesadas == totalAsesorias) {
                                    evaluacionesPendientes = asesoriasList.toList()
                                    loading = false
                                }
                                return@forEach
                            }

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
                                    // Verificar si ya existe una opinión para esta asesoría
                                    firestore.collection("opiniones")
                                        .whereEqualTo("idAsesoria", idAsesoria)
                                        .whereEqualTo("idEmisor", idInstructor)
                                        .whereEqualTo("idReceptor", idAprendiz)
                                        .get()
                                        .addOnSuccessListener { opinionesSnapshot ->
                                            if (opinionesSnapshot.isEmpty) {
                                                firestore.collection("aprendices")
                                                    .whereEqualTo("idUsuario", idAprendiz)
                                                    .get().
                                                    addOnSuccessListener { aprendicesSnapshot ->
                                                        val aprendizDoc =
                                                            aprendicesSnapshot.documents.firstOrNull()
                                                        val idAprendizReal =
                                                            aprendizDoc?.id ?: ""

                                                        firestore.collection("usuarios")
                                                            .document(idAprendiz)
                                                            .get()
                                                            .addOnSuccessListener { usuarioDoc ->
                                                                val nombreAprendiz =
                                                                    usuarioDoc.getString("nombre")
                                                                        ?: "Aprendiz"
                                                                val apellidosAprendiz =
                                                                    usuarioDoc.getString("apellidos")
                                                                        ?: ""

                                                                val asesoriaData = mapOf(
                                                                    "id" to idAsesoria,
                                                                    "idAprendiz" to idAprendizReal,  // Usar el ID real del documento aprendices
                                                                    "nombreAprendiz" to "$nombreAprendiz $apellidosAprendiz",
                                                                    "tema" to (asesoriaDoc.getString(
                                                                        "tema"
                                                                    ) ?: ""),
                                                                    "fecha" to fechaStr,
                                                                    "hora" to horaStr
                                                                )

                                                                asesoriasList.add(asesoriaData)

                                                                procesadas++
                                                                if (procesadas == totalAsesorias) {
                                                                    evaluacionesPendientes =
                                                                        asesoriasList.toList()
                                                                    loading = false
                                                                }
                                                            }
                                                            .addOnFailureListener {
                                                                procesadas++
                                                                if (procesadas == totalAsesorias) {
                                                                    evaluacionesPendientes =
                                                                        asesoriasList.toList()
                                                                    loading = false
                                                                }
                                                            }
                                                    }
                                                    .addOnFailureListener {
                                                        procesadas++
                                                        if (procesadas == totalAsesorias) {
                                                            evaluacionesPendientes = asesoriasList.toList()
                                                            loading = false
                                                        }
                                                    }
                                                /** Obtener nombre del aprendiz
                                                firestore.collection("usuarios")
                                                .document()
                                                .get()
                                                .addOnSuccessListener { usuarioDoc ->
                                                val nombreAprendiz = usuarioDoc.getString("nombre") ?: "Aprendiz"
                                                val apellidosAprendiz = usuarioDoc.getString("apellidos") ?: ""

                                                val asesoriaData = mapOf(
                                                "id" to idAsesoria,
                                                "idAprendiz" to idAprendiz,
                                                "nombreAprendiz" to "$nombreAprendiz $apellidosAprendiz",
                                                "tema" to (asesoriaDoc.getString("tema") ?: ""),
                                                "fecha" to fechaStr,
                                                "hora" to horaStr
                                                )

                                                asesoriasList.add(asesoriaData)

                                                procesadas++
                                                if (procesadas == totalAsesorias) {
                                                evaluacionesPendientes = asesoriasList.toList()
                                                loading = false
                                                }
                                                }
                                                .addOnFailureListener {
                                                procesadas++
                                                if (procesadas == totalAsesorias) {
                                                evaluacionesPendientes = asesoriasList.toList()
                                                loading = false
                                                }
                                                }*/
                                            } else {
                                                procesadas++
                                                if (procesadas == totalAsesorias) {
                                                    evaluacionesPendientes = asesoriasList.toList()
                                                    loading = false
                                                }
                                            }
                                        }
                                        .addOnFailureListener {
                                            procesadas++
                                            if (procesadas == totalAsesorias) {
                                                evaluacionesPendientes = asesoriasList.toList()
                                                loading = false
                                            }
                                        }
                                } else {
                                    procesadas++
                                    if (procesadas == totalAsesorias) {
                                        evaluacionesPendientes = asesoriasList.toList()
                                        loading = false
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("NotificacionesScreen", "Error al parsear fecha: ${e.message}")
                                procesadas++
                                if (procesadas == totalAsesorias) {
                                    evaluacionesPendientes = asesoriasList.toList()
                                    loading = false
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("NotificacionesScreen", "Error al obtener asesorías: ${e.message}")
                        loading = false
                    }
            }
            .addOnFailureListener { e ->
                Log.e("NotificacionesScreen", "Error al obtener instructor: ${e.message}")
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
                            EvaluacionCard(
                                asesoria = asesoria,
                                onEvaluarClick = {
                                    val idAprendiz = asesoria["idAprendiz"] as? String ?: ""
                                    Log.d("EvaluacionCard", "Navegando con idAprendiz: '$idAprendiz'")

                                    if (idAprendiz.isNotEmpty()) {
                                        navController.navigate("calificar_aprendiz/$idAprendiz/${asesoria["idAsesoria"]}")
                                    } else {
                                        Log.e("EvaluacionCard", "ERROR: idAprendiz está vacío, no se puede navegar")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvaluacionCard(
    asesoria: Map<String, Any>,
    onEvaluarClick: () -> Unit
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
                        text = "Evalúa a ${asesoria["nombreAprendiz"]}",
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
                    text = "Cuéntanos, ¿cómo fue tu experiencia con este aprendiz?",
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
                onClick = onEvaluarClick,
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