package mx.edu.utng.jtoh.mentorlink13.ui.screens.aprendiz

import android.util.Log
import androidx.compose.foundation.BorderStroke
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
    var asesoriasRechazadas by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val currentUserId = auth.currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        loading = true
        Log.d("Notificaciones", "Iniciando carga para usuario: $currentUserId")

        val ahora = Calendar.getInstance()
        val hace30Dias = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
        }

        // Primero obtener todas las opiniones del usuario
        firestore.collection("opiniones")
            .whereEqualTo("idEmisor", currentUserId)
            .get()
            .addOnSuccessListener { opinionesSnapshot ->
                val asesoriasEvaluadas = opinionesSnapshot.documents
                    .mapNotNull { doc ->
                        val idAsesoria = doc.getString("idAsesoria")
                        Log.d("Notificaciones", "Opinión encontrada - idAsesoria: $idAsesoria, idReceptor: ${doc.getString("idReceptor")}")
                        idAsesoria
                    }
                    .toSet()

                Log.d("Notificaciones", "Total opiniones del usuario: ${opinionesSnapshot.size()}")
                Log.d("Notificaciones", "IDs de asesorías evaluadas: $asesoriasEvaluadas")

                // Ahora obtener asesorías finalizadas
                firestore.collection("asesorias")
                    .whereEqualTo("idAprendiz", currentUserId)
                    .whereEqualTo("estado", "Finalizada")
                    .get()
                    .addOnSuccessListener { asesoriasSnapshot ->
                        Log.d("Notificaciones", "Asesorías finalizadas encontradas: ${asesoriasSnapshot.size()}")

                        val evaluacionesList = mutableListOf<Map<String, Any>>()
                        val proximasList = mutableListOf<Map<String, Any>>()
                        var asesoriasRestantes = asesoriasSnapshot.size()

                        if (asesoriasSnapshot.isEmpty) {
                            asesoriasRestantes = 0
                        }

                        asesoriasSnapshot.documents.forEach { asesoriaDoc ->
                            val idAsesoria = asesoriaDoc.id
                            val fechaStr = asesoriaDoc.getString("fecha") ?: ""
                            val horaStr = asesoriaDoc.getString("hora") ?: ""
                            val idInstructor = asesoriaDoc.getString("idInstructor") ?: ""

                            Log.d("Notificaciones", "Procesando asesoría: $idAsesoria")
                            Log.d("Notificaciones", "¿Ya evaluada?: ${asesoriasEvaluadas.contains(idAsesoria)}")

                            // Verificar si ya fue evaluada
                            if (asesoriasEvaluadas.contains(idAsesoria)) {
                                Log.d("Notificaciones", "Asesoría $idAsesoria YA EVALUADA, omitiendo...")
                                asesoriasRestantes--
                                if (asesoriasRestantes == 0) {
                                    evaluacionesPendientes = evaluacionesList.toList()
                                    asesoriasProximas = proximasList.toList()
                                    Log.d("Notificaciones", "Finalizando - Evaluaciones: ${evaluacionesList.size}, Próximas: ${proximasList.size}")
                                }
                                return@forEach
                            }

                            // Parsear fecha y hora
                            try {
                                val partesFecha = fechaStr.split("/")
                                val partesHora = horaStr.split(" - ")

                                if (partesHora.size < 2) {
                                    Log.e("Notificaciones", "Formato de hora inválido: $horaStr")
                                    asesoriasRestantes--
                                    return@forEach
                                }

                                val partesHoraFin = partesHora[1].trim().split(":")

                                if (partesFecha.size != 3 || partesHoraFin.size != 2) {
                                    Log.e("Notificaciones", "Formato de fecha/hora inválido: $fechaStr / $horaStr")
                                    asesoriasRestantes--
                                    return@forEach
                                }

                                val fechaAsesoria = Calendar.getInstance().apply {
                                    set(Calendar.DAY_OF_MONTH, partesFecha[0].toInt())
                                    set(Calendar.MONTH, partesFecha[1].toInt() - 1)
                                    set(Calendar.YEAR, partesFecha[2].toInt())
                                    set(Calendar.HOUR_OF_DAY, partesHoraFin[0].toInt())
                                    set(Calendar.MINUTE, partesHoraFin[1].toInt())
                                    set(Calendar.SECOND, 0)
                                }

                                Log.d("Notificaciones", "Fecha asesoría: ${fechaAsesoria.time}, Ahora: ${ahora.time}")

                                if (fechaAsesoria.before(ahora)) {
                                    // ASESORÍA YA PASÓ - Pendiente de evaluación
                                    Log.d("Notificaciones", "Asesoría $idAsesoria PENDIENTE DE EVALUACIÓN")

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
                                        Log.d("Notificaciones", "Agregada a evaluaciones pendientes: $idAsesoria")

                                        asesoriasRestantes--
                                        if (asesoriasRestantes == 0) {
                                            evaluacionesPendientes = evaluacionesList.toList()
                                            asesoriasProximas = proximasList.toList()
                                            Log.d("Notificaciones", "FINAL - Evaluaciones: ${evaluacionesList.size}, Próximas: ${proximasList.size}")
                                        }
                                    }
                                } else {
                                    // ASESORÍA PRÓXIMA
                                    Log.d("Notificaciones", "Asesoría $idAsesoria es PRÓXIMA")

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
                                            val proximasOrdenadas = proximasList.sortedBy {
                                                it["fechaAsesoria"] as Long
                                            }

                                            evaluacionesPendientes = evaluacionesList.toList()
                                            asesoriasProximas = proximasOrdenadas
                                            Log.d("Notificaciones", "FINAL - Evaluaciones: ${evaluacionesList.size}, Próximas: ${proximasOrdenadas.size}")
                                        }
                                    }
                                }

                            } catch (e: Exception) {
                                Log.e("Notificaciones", "Error al parsear fecha: ${e.message}", e)
                                asesoriasRestantes--
                            }
                        }

                        // Obtener asesorías rechazadas de los últimos 30 días
                        firestore.collection("asesorias")
                            .whereEqualTo("idAprendiz", currentUserId)
                            .whereEqualTo("estado", "Rechazada")
                            .get()
                            .addOnSuccessListener { rechazadasSnapshot ->
                                Log.d("Notificaciones", "Asesorías rechazadas encontradas: ${rechazadasSnapshot.size()}")

                                val rechazadasList = mutableListOf<Map<String, Any>>()
                                var rechazadasRestantes = rechazadasSnapshot.size()

                                if (rechazadasSnapshot.isEmpty) {
                                    asesoriasRechazadas = emptyList()
                                    loading = false
                                    return@addOnSuccessListener
                                }

                                rechazadasSnapshot.documents.forEach { asesoriaDoc ->
                                    val idAsesoria = asesoriaDoc.id
                                    val fechaStr = asesoriaDoc.getString("fecha") ?: ""
                                    val horaStr = asesoriaDoc.getString("hora") ?: ""
                                    val idInstructor = asesoriaDoc.getString("idInstructor") ?: ""

                                    try {
                                        val partesFecha = fechaStr.split("/")

                                        if (partesFecha.size != 3) {
                                            rechazadasRestantes--
                                            if (rechazadasRestantes == 0) {
                                                asesoriasRechazadas = rechazadasList.sortedByDescending {
                                                    it["fechaAsesoria"] as Long
                                                }
                                                loading = false
                                            }
                                            return@forEach
                                        }

                                        val fechaAsesoria = Calendar.getInstance().apply {
                                            set(Calendar.DAY_OF_MONTH, partesFecha[0].toInt())
                                            set(Calendar.MONTH, partesFecha[1].toInt() - 1)
                                            set(Calendar.YEAR, partesFecha[2].toInt())
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                        }

                                        // Filtrar solo las de los últimos 30 días
                                        if (fechaAsesoria.after(hace30Dias)) {
                                            obtenerDatosInstructor(firestore, idInstructor) { nombreInstructor ->
                                                val asesoriaData = mapOf(
                                                    "id" to idAsesoria,
                                                    "idInstructor" to idInstructor,
                                                    "nombreInstructor" to nombreInstructor,
                                                    "tema" to (asesoriaDoc.getString("tema") ?: "Sin tema"),
                                                    "fecha" to fechaStr,
                                                    "hora" to horaStr,
                                                    "fechaAsesoria" to fechaAsesoria.timeInMillis
                                                )
                                                rechazadasList.add(asesoriaData)

                                                rechazadasRestantes--
                                                if (rechazadasRestantes == 0) {
                                                    asesoriasRechazadas = rechazadasList.sortedByDescending {
                                                        it["fechaAsesoria"] as Long
                                                    }
                                                    loading = false
                                                }
                                            }
                                        } else {
                                            rechazadasRestantes--
                                            if (rechazadasRestantes == 0) {
                                                asesoriasRechazadas = rechazadasList.sortedByDescending {
                                                    it["fechaAsesoria"] as Long
                                                }
                                                loading = false
                                            }
                                        }

                                    } catch (e: Exception) {
                                        Log.e("Notificaciones", "Error al parsear fecha rechazada: ${e.message}", e)
                                        rechazadasRestantes--
                                        if (rechazadasRestantes == 0) {
                                            asesoriasRechazadas = rechazadasList.sortedByDescending {
                                                it["fechaAsesoria"] as Long
                                            }
                                            loading = false
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Notificaciones", "Error al obtener rechazadas: ${e.message}", e)
                                asesoriasRechazadas = emptyList()
                                loading = false
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Notificaciones", "Error al obtener asesorías: ${e.message}", e)
                        loading = false
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Notificaciones", "Error al obtener opiniones: ${e.message}", e)
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

                evaluacionesPendientes.isEmpty() && asesoriasProximas.isEmpty() && asesoriasRechazadas.isEmpty() -> {
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

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // SECCIÓN: Asesorías Rechazadas
                        if (asesoriasRechazadas.isNotEmpty()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Asesorías Rechazadas",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                }
                            }

                            items(asesoriasRechazadas) { asesoria ->
                                AsesoriaRechazadaCard(
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

// Card para Asesorías Rechazadas
@Composable
fun AsesoriaRechazadaCard(
    asesoria: Map<String, Any>,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Encabezado con ícono
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Solicitud Rechazada",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B7280)
                )
            }

            // Mensaje principal
            Text(
                text = "Tu solicitud de asesoría sobre ${asesoria["tema"]} programada para ${asesoria["fecha"]} a las ${asesoria["hora"]} fue rechazada por ${asesoria["nombreInstructor"]}.",
                fontSize = 14.sp,
                color = Color(0xFF4B5563),
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Botón para ver perfil
            Button(
                onClick = {
                    navController.navigate("perfil_instructor/${asesoria["idInstructor"]}")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B7280)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver perfil del instructor")
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