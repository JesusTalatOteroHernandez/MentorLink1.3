package mx.edu.utng.jtoh.mentorlink13.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedButton
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
    var asesoriasProximas by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val currentUserId = auth.currentUser?.uid ?: "" // Aprendiz logueado

    LaunchedEffect(Unit) {
        loading = true
        Log.d("Notificaciones", "Iniciando carga para usuario: $currentUserId")

        // Obtener la fecha y hora actual
        val ahora = Calendar.getInstance()

        // Obtener todas las asesorías donde el usuario actual es el aprendiz
        firestore.collection("asesorias")
            .whereEqualTo("idAprendiz", currentUserId)
            .whereEqualTo("estado", "Aceptada")
            .get()
            .addOnSuccessListener { asesoriasSnapshot ->
                Log.d("Notificaciones", "Asesorías aceptadas encontradas: ${asesoriasSnapshot.size()}")

                if (asesoriasSnapshot.isEmpty) {
                    evaluacionesPendientes = emptyList()
                    asesoriasProximas = emptyList()
                    loading = false
                    return@addOnSuccessListener
                }

                val evaluacionesList = mutableListOf<Map<String, Any>>()
                val proximasList = mutableListOf<Map<String, Any>>()
                var asesoriasRestantes = asesoriasSnapshot.size()

                asesoriasSnapshot.documents.forEach { asesoriaDoc ->
                    val idAsesoria = asesoriaDoc.id
                    val fechaStr = asesoriaDoc.getString("fecha") ?: ""
                    val horaStr = asesoriaDoc.getString("hora") ?: ""
                    val idInstructor = asesoriaDoc.getString("idInstructor") ?: ""

                    // Parsear fecha y hora
                    try {
                        val partesFecha = fechaStr.split("/")
                        val partesHora = horaStr.split(" - ")[0].split(":")

                        if (partesFecha.size != 3 || partesHora.size != 2) {
                            Log.e("Notificaciones", "Formato de fecha/hora inválido: $fechaStr / $horaStr")
                            asesoriasRestantes--
                            if (asesoriasRestantes == 0) {
                                evaluacionesPendientes = evaluacionesList.toList()
                                asesoriasProximas = proximasList.toList()
                                loading = false
                            }
                            return@forEach
                        }

                        val fechaAsesoria = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, partesFecha[0].toInt())
                            set(Calendar.MONTH, partesFecha[1].toInt() - 1)
                            set(Calendar.YEAR, partesFecha[2].toInt())
                            set(Calendar.HOUR_OF_DAY, partesHora[0].toInt())
                            set(Calendar.MINUTE, partesHora[1].toInt())
                            set(Calendar.SECOND, 0)
                        }

                        // Verificar si ya pasó o es próxima
                        if (fechaAsesoria.before(ahora)) {
                            // ASESORÍA YA PASÓ - Verificar si necesita evaluación
                            Log.d("Notificaciones", "Asesoría $idAsesoria ya pasó")

                            firestore.collection("opiniones")
                                .whereEqualTo("idAsesoria", idAsesoria)
                                .whereEqualTo("idReceptor", idInstructor)
                                .whereEqualTo("idEmisor", currentUserId)
                                .get()
                                .addOnSuccessListener { opinionesSnapshot ->
                                    if (opinionesSnapshot.isEmpty) {
                                        // Obtener datos del instructor para evaluación
                                        obtenerDatosInstructor(firestore, idInstructor) { nombreInstructor ->
                                            val asesoriaData = mapOf(
                                                "id" to idAsesoria,
                                                "idInstructor" to idInstructor,
                                                "nombreInstructor" to nombreInstructor,
                                                "tema" to (asesoriaDoc.getString("tema") ?: "Sin tema"),
                                                "fecha" to fechaStr,
                                                "hora" to horaStr
                                            )
                                            evaluacionesList.add(asesoriaData)

                                            asesoriasRestantes--
                                            if (asesoriasRestantes == 0) {
                                                evaluacionesPendientes = evaluacionesList.toList()
                                                asesoriasProximas = proximasList.toList()
                                                loading = false
                                            }
                                        }
                                    } else {
                                        asesoriasRestantes--
                                        if (asesoriasRestantes == 0) {
                                            evaluacionesPendientes = evaluacionesList.toList()
                                            asesoriasProximas = proximasList.toList()
                                            loading = false
                                        }
                                    }
                                }
                        } else {
                            // ASESORÍA PRÓXIMA
                            Log.d("Notificaciones", "Asesoría $idAsesoria es próxima")

                            obtenerDatosInstructor(firestore, idInstructor) { nombreInstructor ->
                                val asesoriaData = mapOf(
                                    "id" to idAsesoria,
                                    "idInstructor" to idInstructor,
                                    "nombreInstructor" to nombreInstructor,
                                    "tema" to (asesoriaDoc.getString("tema") ?: "Sin tema"),
                                    "fecha" to fechaStr,
                                    "hora" to horaStr,
                                    "modalidad" to (asesoriaDoc.getString("modalidad") ?: "Virtual"),
                                    "fechaAsesoria" to fechaAsesoria.timeInMillis
                                )
                                proximasList.add(asesoriaData)

                                asesoriasRestantes--
                                if (asesoriasRestantes == 0) {
                                    // Ordenar asesorías próximas por fecha (más cercanas primero)
                                    val proximasOrdenadas = proximasList.sortedBy {
                                        it["fechaAsesoria"] as Long
                                    }

                                    evaluacionesPendientes = evaluacionesList.toList()
                                    asesoriasProximas = proximasOrdenadas
                                    loading = false
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Notificaciones", "Error al parsear fecha: ${e.message}")
                        asesoriasRestantes--
                        if (asesoriasRestantes == 0) {
                            evaluacionesPendientes = evaluacionesList.toList()
                            asesoriasProximas = proximasList.toList()
                            loading = false
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Notificaciones", "Error al obtener asesorías: ${e.message}")
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
                            text = "Cargando notificaciones...",
                            color = Color.Gray
                        )
                    }
                }

                evaluacionesPendientes.isEmpty() && asesoriasProximas.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tienes notificaciones",
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
                        // SECCIÓN: Evaluaciones Pendientes
                        if (evaluacionesPendientes.isNotEmpty()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Evaluaciones Pendientes",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                }
                            }

                            items(evaluacionesPendientes) { asesoria ->
                                EvaluacionCardAprendiz(
                                    asesoria = asesoria,
                                    navController = navController
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // SECCIÓN: Asesorías Próximas
                        if (asesoriasProximas.isNotEmpty()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Asesorías Próximas",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                }
                            }

                            items(asesoriasProximas) { asesoria ->
                                AsesoriaProximaCard(
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
}

// Función auxiliar para obtener datos del instructor
fun obtenerDatosInstructor(
    firestore: FirebaseFirestore,
    idInstructor: String,
    callback: (String) -> Unit
) {
    firestore.collection("instructores")
        .document(idInstructor)
        .get()
        .addOnSuccessListener { instructorDoc ->
            val idUsuarioInstructor = instructorDoc.getString("idUsuario") ?: ""

            if (idUsuarioInstructor.isEmpty()) {
                callback("Instructor")
                return@addOnSuccessListener
            }

            firestore.collection("usuarios")
                .document(idUsuarioInstructor)
                .get()
                .addOnSuccessListener { usuarioDoc ->
                    val nombre = usuarioDoc.getString("nombre") ?: "Instructor"
                    val apellidos = usuarioDoc.getString("apellidos") ?: ""
                    val nombreCompleto = if (apellidos.isNotEmpty()) {
                        "$nombre $apellidos"
                    } else {
                        nombre
                    }
                    callback(nombreCompleto)
                }
                .addOnFailureListener {
                    callback("Instructor")
                }
        }
        .addOnFailureListener {
            callback("Instructor")
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

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de evaluación
            Button(
                onClick = {
                    navController.navigate("calificar_instructor/${asesoria["idInstructor"]}/${asesoria["id"]}")
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

@Composable
fun AsesoriaProximaCard(
    asesoria: Map<String, Any>,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 3.dp,
                color = Color(0xFF2563EB),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Encabezado
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asesoria["nombreInstructor"].toString(),
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

                // Badge de modalidad
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (asesoria["modalidad"] == "Virtual")
                            Color(0xFFDCEBFE) else Color(0xFFDCFCE7)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = asesoria["modalidad"].toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (asesoria["modalidad"] == "Virtual")
                            Color(0xFF2563EB) else Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información de fecha y hora destacada
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = asesoria["fecha"].toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color(0xFFE5E7EB))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = asesoria["hora"].toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mensaje informativo
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
                    text = "Recuerda estar listo unos minutos antes",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para ver detalles
            OutlinedButton (
                onClick = {
                    navController.navigate("perfil_instructor/${asesoria["idInstructor"]}")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF2563EB)
                ),
                border = BorderStroke(2.dp, Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ver Perfil del Instructor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}