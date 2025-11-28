package mx.edu.utng.jtoh.mentorlink13.ui.screens.instructor

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MarkunreadMailbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.CheckCircle

fun esFechaYHoraPasada(fecha: String, hora: String): Boolean {
    try {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaHoraStr = "$fecha $hora"
        val fechaHoraAsesoria = formatoFecha.parse(fechaHoraStr)
        val fechaActual = Date()

        return fechaActual.after(fechaHoraAsesoria)
    } catch (e: Exception) {
        return false
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaginaInstructor(navController: NavController){
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUserId = auth.currentUser?.uid

    var instructorId by remember { mutableStateOf("") }
    var instructorNombre by remember { mutableStateOf("Instructor") }
    var calificacionPromedio by remember { mutableStateOf(0.0) }
    var totalReseñas by remember { mutableStateOf(0) }
    var asesoriasCompletadas by remember { mutableStateOf(0) }

    var solicitudesPendientes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var asesoriasPendientes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    var showAceptarDialog by remember { mutableStateOf(false) }
    var showRechazarDialog by remember { mutableStateOf(false) }
    var selectedAsesoria by remember { mutableStateOf<Map<String, Any>?>(null) }
    var selectedAprendizNombre by remember { mutableStateOf("") }

    var showMenuCerrarSesion by remember { mutableStateOf(false) }
    var showDialogCerrarSesion by remember { mutableStateOf(false) }
    var showDialogSalir by remember { mutableStateOf(false) }



    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            // 1. Obtener datos del instructor
            db.collection("usuarios").document(currentUserId).get()
                .addOnSuccessListener { userDoc ->
                    if (userDoc.exists()) {
                        instructorNombre = userDoc.getString("nombre") ?: "Instructor"

                        // Buscar el instructor usando el idUsuario
                        db.collection("instructores")
                            .whereEqualTo("idUsuario", currentUserId)
                            .get()
                            .addOnSuccessListener { instructorDocs ->
                                if (!instructorDocs.isEmpty) {
                                    val instructorDoc = instructorDocs.documents[0]
                                    instructorId = instructorDoc.id

                                    // ✅ CARGAR contador de asesorías completadas desde instructores
                                    asesoriasCompletadas = instructorDoc.getLong("asesoriasCompletadas")?.toInt() ?: 0

                                    // 2. Calcular calificación promedio y total de reseñas
                                    db.collection("opiniones")
                                        .whereEqualTo("idReceptor", instructorId)
                                        .get()
                                        .addOnSuccessListener { opiniones ->
                                            if (!opiniones.isEmpty) {
                                                var sumaPuntuacion = 0
                                                opiniones.documents.forEach { doc ->
                                                    sumaPuntuacion += (doc.getLong("puntuacion")?.toInt() ?: 0)
                                                }
                                                totalReseñas = opiniones.size()
                                                calificacionPromedio = sumaPuntuacion.toDouble() / totalReseñas
                                            }
                                        }

                                    // 3. Obtener solicitudes pendientes
                                    db.collection("asesorias")
                                        .whereEqualTo("idInstructor", instructorId)
                                        .whereEqualTo("estado", "Pendiente")
                                        .get()
                                        .addOnSuccessListener { asesorias ->
                                            val solicitudes = mutableListOf<Map<String, Any>>()
                                            asesorias.documents.forEach { doc ->
                                                val asesoriaData = doc.data?.toMutableMap() ?: mutableMapOf()
                                                asesoriaData["asesoriaId"] = doc.id

                                                // Obtener datos del aprendiz
                                                val idAprendiz = doc.getString("idAprendiz") ?: ""
                                                db.collection("usuarios").document(idAprendiz).get()
                                                    .addOnSuccessListener { userDoc ->
                                                        if (userDoc.exists()) {
                                                            asesoriaData["nombreAprendiz"] = userDoc.getString("nombre") ?: ""
                                                            asesoriaData["correoAprendiz"] = userDoc.getString("correoElectronico") ?: ""
                                                            solicitudes.add(asesoriaData)
                                                            solicitudesPendientes = solicitudes.toList()
                                                        }
                                                    }
                                            }
                                        }

                                    // 4. Obtener asesorías aceptadas (pendientes de realizarse)
                                    db.collection("asesorias")
                                        .whereEqualTo("idInstructor", instructorId)
                                        .whereEqualTo("estado", "Aceptada")
                                        .get()
                                        .addOnSuccessListener { asesorias ->
                                            val pendientes = mutableListOf<Map<String, Any>>()
                                            val fechaActual = System.currentTimeMillis()

                                            asesorias.documents.forEach { doc ->
                                                val asesoriaData = doc.data?.toMutableMap() ?: mutableMapOf()
                                                asesoriaData["asesoriaId"] = doc.id

                                                // Verificar si la fecha aún no ha pasado
                                                val fechaStr = doc.getString("fecha") ?: ""
                                                // Aquí puedes agregar lógica de comparación de fechas si es necesario

                                                val idAprendiz = doc.getString("idAprendiz") ?: ""
                                                db.collection("usuarios").document(idAprendiz).get()
                                                    .addOnSuccessListener { userDoc ->
                                                        if (userDoc.exists()) {
                                                            asesoriaData["nombreAprendiz"] = userDoc.getString("nombre") ?: ""
                                                            asesoriaData["correoAprendiz"] = userDoc.getString("correoElectronico") ?: ""
                                                            pendientes.add(asesoriaData)
                                                            asesoriasPendientes = pendientes.toList()
                                                        }
                                                    }
                                            }
                                        }
                                }
                            }
                    }
                }
        }
    }

    // Diálogo para aceptar solicitud
    if (showAceptarDialog && selectedAsesoria != null) {
        AlertDialog(
            onDismissRequest = { showAceptarDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Solicitud Aceptada", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showAceptarDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
            },
            text = {
                Text("Solicitud aceptada, por favor comuníquese con el aprendiz $selectedAprendizNombre en la brevedad de lo posible.")
            },
            confirmButton = {},
            containerColor = Color.White
        )
    }

    // Diálogo para rechazar solicitud
    if (showRechazarDialog && selectedAsesoria != null) {
        AlertDialog(
            onDismissRequest = { showRechazarDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Solicitud Rechazada", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showRechazarDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
            },
            text = {
                Text("Gracias por su tiempo. Avisaremos de inmediato al aprendiz $selectedAprendizNombre que su solicitud ha sido rechazada.")
            },
            confirmButton = {},
            containerColor = Color.White
        )
    }

    // Diálogo con opciones: Cerrar sesión y Salir
    if (showMenuCerrarSesion) {
        AlertDialog(
            onDismissRequest = { showMenuCerrarSesion = false },
            title = {
                Text("Opciones de cuenta", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Cerrar sesión",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMenuCerrarSesion = false
                                showDialogCerrarSesion = true
                            }
                            .padding(8.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Salir",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMenuCerrarSesion = false
                                showDialogSalir = true
                            }
                            .padding(8.dp)
                    )
                }
            },
            confirmButton = {}
        )
    }
    if (showDialogCerrarSesion) {
        AlertDialog(
            onDismissRequest = { showDialogCerrarSesion = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que deseas cerrar sesión?") },
            confirmButton = {
                Button(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("pantalla_inicio") {
                        popUpTo(0)
                    }
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                Button(onClick = { showDialogCerrarSesion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDialogSalir) {
        AlertDialog(
            onDismissRequest = { showDialogSalir = false },
            title = { Text("Salir de la aplicación") },
            text = { Text("¿Deseas cerrar la aplicación por completo?") },
            confirmButton = {
                Button(onClick = {
                    showDialogSalir = false
                    System.exit(0)
                }) {
                    Text("Salir")
                }
            },
            dismissButton = {
                Button(onClick = { showDialogSalir = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                elevation = CardDefaults.cardElevation(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(37, 99, 235)
                ),
                shape = RoundedCornerShape(30.dp)
            ){
                Column(
                    modifier = Modifier.padding(25.dp)
                        .padding(top = 25.dp)
                ) {
                    Row {
                        Column {
                            Text(
                                "MentorLink",
                                fontSize = 25.sp,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(7.dp)
                            )
                        }
                        Spacer(Modifier.width(150.dp))

                        Column {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Usuario",
                                tint = Color.DarkGray,
                                modifier = Modifier
                                    .size(25.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .clickable { showMenuCerrarSesion = true }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(78, 117, 215)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ){
                        Column(modifier = Modifier.padding(15.dp)) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Usuario",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Row {
                                Text("Hola, $instructorNombre",
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.SansSerif
                                )
                                Spacer(Modifier.width(7.dp))
                                Icon(
                                    imageVector = Icons.Default.WavingHand,
                                    contentDescription = "Usuario",
                                    tint = Color(255, 190, 66),
                                    modifier = Modifier.size(25.dp)
                                )

                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp),
                elevation = CardDefaults.cardElevation(5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(255, 255, 255)
                )
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(25.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        modifier = Modifier
                            .size(width = 90.dp, height = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Evaluaciones",
                            tint = Color(255, 190, 66),
                            modifier = Modifier.size(40.dp)
                                .clickable(
                                    onClick = {
                                        navController.navigate("notificaciones_instructor")
                                    }
                                )
                        )
                        Spacer(Modifier.height(5.dp))
                        Text("Evaluaciones",
                            fontFamily = FontFamily.SansSerif)
                    }

                    Column(
                        modifier = Modifier
                            .size(width = 90.dp, height = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Usuario",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(40.dp)
                                .clickable {
                                    navController.navigate("pantalla_editable_de_instructor/{idInstructor}")
                                }
                        )
                        Spacer(Modifier.height(5.dp))
                        Text("Perfil",
                            fontFamily = FontFamily.SansSerif)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
        {
            // Card de Resumen
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(15.dp),
                    elevation = CardDefaults.cardElevation(5.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(255, 255, 255)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.padding(25.dp)) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Estadisticas",
                                tint = Color(13, 176, 123),
                                modifier = Modifier.size(25.dp)
                            )
                            Text(
                                "Resumen",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.padding(5.dp)
                            )
                        }
                        Row {
                            Spacer(Modifier.width(10.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    String.format("%.1f", calificacionPromedio),
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(37, 99, 235)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Calificación",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Light
                                )
                                Spacer(Modifier.height(6.dp))
                                Row {
                                    repeat(5) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Estrella",
                                            tint = Color(232, 191, 54),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        if (it < 4) Spacer(Modifier.width(1.dp))
                                    }
                                }
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    "($totalReseñas reseñas)",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraLight,
                                    fontSize = 12.sp
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "$asesoriasCompletadas",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(16, 185, 129)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Asesorías",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Light
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "completadas",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraLight,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                        }
                    }
                }
            }

            // Título Solicitudes Pendientes
            item {
                Spacer(Modifier.height(5.dp))
                Row(modifier = Modifier.padding(20.dp)) {
                    Icon(
                        imageVector = Icons.Default.MarkunreadMailbox,
                        contentDescription = "Buzón",
                        tint = Color.Red,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Solicitudes Pendientes",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            // Cards de Solicitudes Pendientes
            items(solicitudesPendientes) { asesoria ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(15.dp),
                    elevation = CardDefaults.cardElevation(5.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(255, 255, 255)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Row {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Usuario",
                                tint = Color.Gray,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    asesoria["nombreAprendiz"] as? String ?: "Aprendiz",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            "Tema solicitado:",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraLight,
                            fontSize = 11.sp
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            asesoria["tema"] as? String ?: "",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Spacer(Modifier.height(10.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(vertical = 5.dp),
                            elevation = CardDefaults.cardElevation(5.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(249, 250, 251)
                            ),
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            Text(
                                asesoria["adicional"] as? String
                                    ?: "Sin especificaciones adicionales",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraLight,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Row {
                            Text(
                                "Fecha:",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraLight,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                asesoria["fecha"] as? String ?: "",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(5.dp))

                        Row {
                            Text(
                                "Hora:",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraLight,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                asesoria["hora"] as? String ?: "",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(5.dp))

                        Row {
                            Text(
                                "Modalidad:",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraLight,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                asesoria["modalidad"] as? String ?: "",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(37, 99, 235)
                            )
                        }

                        Spacer(Modifier.height(15.dp))

                        // BOTÓN VER PERFIL DEL APRENDIZ
                        Button(
                            onClick = {
                                val idAprendiz = asesoria["idAprendiz"] as? String ?: ""
                                if (idAprendiz.isNotEmpty()) {
                                    navController.navigate("perfil_aprendiz/$idAprendiz")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(37, 99, 235)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Ver perfil",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Ver Perfil del Aprendiz",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(15.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Card(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable {
                                        selectedAsesoria = asesoria
                                        selectedAprendizNombre =
                                            asesoria["nombreAprendiz"] as? String ?: ""

                                        // Actualizar estado en Firebase
                                        val asesoriaId = asesoria["asesoriaId"] as? String ?: ""
                                        db.collection("asesorias").document(asesoriaId)
                                            .update("estado", "Aceptada")
                                            .addOnSuccessListener {
                                                // ✅ INCREMENTAR contador en la colección instructores
                                                if (instructorId.isNotEmpty()) {
                                                    db.collection("instructores")
                                                        .document(instructorId)
                                                        .get()
                                                        .addOnSuccessListener { doc ->
                                                            val asesoriaActuales =
                                                                doc.getLong("asesoriasCompletadas")
                                                                    ?.toInt() ?: 0
                                                            db.collection("instructores")
                                                                .document(instructorId)
                                                                .update(
                                                                    "asesoriasCompletadas",
                                                                    asesoriaActuales + 1
                                                                )
                                                                .addOnSuccessListener {
                                                                    // Actualizar el estado local
                                                                    asesoriasCompletadas += 1
                                                                    showAceptarDialog = true
                                                                }
                                                        }
                                                }
                                            }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(13, 176, 123)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Aceptar",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable {
                                        selectedAsesoria = asesoria
                                        selectedAprendizNombre =
                                            asesoria["nombreAprendiz"] as? String ?: ""

                                        // Eliminar o actualizar estado en Firebase
                                        val asesoriaId = asesoria["asesoriaId"] as? String ?: ""
                                        db.collection("asesorias").document(asesoriaId)
                                            .update("estado", "Rechazada")
                                            .addOnSuccessListener {
                                                showRechazarDialog = true
                                            }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(239, 68, 68)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Rechazar",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Título Asesorías Pendientes
            item {
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.padding(20.dp)) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Reloj",
                        tint = Color(37, 99, 235),
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Asesorías Pendientes",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            // Cards de Asesorías Pendientes (Aceptadas)
            items(asesoriasPendientes) { asesoria ->
                val fechaPasada = esFechaYHoraPasada(
                    asesoria["fecha"] as? String ?: "",
                    asesoria["hora"] as? String ?: ""
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(15.dp),
                    elevation = CardDefaults.cardElevation(5.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(255, 255, 255)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Text(
                            "Asesoría con:",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraLight,
                            fontSize = 11.sp
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            asesoria["nombreAprendiz"] as? String ?: "Aprendiz",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(Modifier.height(10.dp))

                        Row {
                            Text(
                                "Fecha:",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraLight,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                asesoria["fecha"] as? String ?: "",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(5.dp))

                        Row {
                            Text(
                                "Hora:",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraLight,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                asesoria["hora"] as? String ?: "",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(5.dp))

                        Row {
                            Text(
                                "Modalidad:",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraLight,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                asesoria["modalidad"] as? String ?: "",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if ((asesoria["modalidad"] as? String ?: "") == "Virtual")
                                    Color(139, 92, 246) else Color(37, 99, 235)
                            )
                        }

                        Spacer(Modifier.height(5.dp))

                        Row {
                            Text(
                                "Tema:",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraLight,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                asesoria["tema"] as? String ?: "",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(249, 250, 251)
                            ),
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "Correo del aprendiz:",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Light,
                                    fontSize = 11.sp
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    asesoria["correoAprendiz"] as? String ?: "",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(37, 99, 235)
                                )
                            }
                        }

                        Spacer(Modifier.height(15.dp))

                        // ✅ BOTÓN PARA FINALIZAR (solo aparece si la fecha ya pasó)
                        if (fechaPasada) {
                            Button(
                                onClick = {
                                    val asesoriaId = asesoria["asesoriaId"] as? String ?: ""
                                    if (asesoriaId.isNotEmpty()) {
                                        // Actualizar estado a Finalizada
                                        db.collection("asesorias").document(asesoriaId)
                                            .update("estado", "Finalizada")
                                            .addOnSuccessListener {
                                                // Actualizar contador en instructores
                                                if (instructorId.isNotEmpty()) {
                                                    db.collection("instructores")
                                                        .document(instructorId)
                                                        .get()
                                                        .addOnSuccessListener { doc ->
                                                            val asesoriaActuales =
                                                                doc.getLong("asesoriasCompletadas")
                                                                    ?.toInt() ?: 0
                                                            db.collection("instructores")
                                                                .document(instructorId)
                                                                .update(
                                                                    "asesoriasCompletadas",
                                                                    asesoriaActuales + 1
                                                                )
                                                                .addOnSuccessListener {
                                                                    // Actualizar estado local
                                                                    asesoriasCompletadas += 1

                                                                    // Remover de la lista de pendientes
                                                                    asesoriasPendientes =
                                                                        asesoriasPendientes.filter {
                                                                            it["asesoriaId"] != asesoriaId
                                                                        }

                                                                    Toast.makeText(
                                                                        context,
                                                                        "Asesoría finalizada exitosamente",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                        }
                                                }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    context,
                                                    "Error al finalizar asesoría",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(16, 185, 129)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Finalizar",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Finalizar Asesoría",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            // Mostrar indicador de que aún no es tiempo
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(254, 243, 199)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Pendiente",
                                        tint = Color(217, 119, 6),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Asesoría programada - Podrás finalizarla después de la fecha",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 11.sp,
                                        color = Color(120, 53, 15)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}