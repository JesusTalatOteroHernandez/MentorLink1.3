package mx.edu.utng.jtoh.mentorlink13.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilInstructorEditable(navController: NavController) {

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val idUsuario = auth.currentUser?.uid ?: ""

    var datosInstructor by remember { mutableStateOf<Map<String, Any>?>(null) }
    var idInstructor by remember { mutableStateOf("") }
    var temasPorInstructor by remember { mutableStateOf<List<String>>(emptyList()) }
    var opinionesInstructor by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // ================================
    // 1️⃣ CARGAR ID DEL INSTRUCTOR
    // ================================
    LaunchedEffect(idUsuario) {
        if (idUsuario.isBlank()) return@LaunchedEffect

        firestore.collection("instructores")
            .whereEqualTo("idUsuario", idUsuario)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.isEmpty) {
                    idInstructor = snap.documents[0].id
                }
            }
    }

    // ================================
    // 2️⃣ CARGAR DATOS DE PERFIL
    // ================================
    LaunchedEffect(idInstructor) {
        if (idInstructor.isBlank()) return@LaunchedEffect

        firestore.collection("instructores").document(idInstructor).get()
            .addOnSuccessListener { instDoc ->

                firestore.collection("usuarios").document(idUsuario).get()
                    .addOnSuccessListener { usuarioDoc ->
                        datosInstructor = mapOf(
                            "idInstructor" to idInstructor,
                            "nombre" to (usuarioDoc.getString("nombre") ?: ""),
                            "apellidos" to (usuarioDoc.getString("apellidos") ?: ""),
                            "modalidad" to (instDoc.getString("modalidad") ?: ""),
                            "estadoResidencia" to (usuarioDoc.getString("estadoResidencia") ?: ""),
                            "municipioResidencia" to (usuarioDoc.getString("municipioResidencia") ?: ""),
                            "cobro" to (instDoc.getLong("cobro")?.toInt() ?: 0),
                            "puntuacion" to (instDoc.getDouble("puntuacion") ?: 0.0)
                        )
                    }
            }
    }

    // ================================
    // 3️⃣ CARGAR TEMAS
    // ================================
    LaunchedEffect(idInstructor) {
        if (idInstructor.isBlank()) return@LaunchedEffect

        firestore.collection("instructorTema")
            .whereEqualTo("idInstructor", idInstructor)
            .get()
            .addOnSuccessListener { relSnapshot ->

                val listaTemas = mutableListOf<String>()

                relSnapshot.forEach { relacion ->
                    val idTema = relacion.getString("idTema") ?: return@forEach

                    firestore.collection("temas").document(idTema).get()
                        .addOnSuccessListener { temaDoc ->
                            val nombreTema = temaDoc.getString("nombre") ?: ""
                            if (nombreTema.isNotBlank()) {
                                listaTemas.add(nombreTema)
                                temasPorInstructor = listaTemas.toList()
                            }
                        }
                }
            }
    }

    // ================================
    // 4️⃣ CARGAR OPINIONES
    // ================================
    LaunchedEffect(idUsuario) {
        if (idUsuario.isBlank()) return@LaunchedEffect

        firestore.collection("opiniones")
            .whereEqualTo("idReceptor", idUsuario)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.documents.map { doc ->
                    mapOf(
                        "idEmisor" to (doc.getString("idEmisor") ?: ""),
                        "opinion" to (doc.getString("opinion") ?: ""),
                        "puntuacion" to (doc.getLong("puntuacion")?.toInt() ?: 0)
                    )
                }
                opinionesInstructor = lista
            }
    }

    // ================================
    // 5️⃣ DISEÑO (NO SE MODIFICÓ NADA)
    // ================================
    Scaffold(
        topBar = {
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
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Perfil del Instructor",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },

        bottomBar = {
            if (idInstructor.isNotBlank()) {
                Button(
                    onClick = { navController.navigate("solicitar_asesoria/$idInstructor") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .padding(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE67E22)
                    )
                ) {
                    Text("Solicitar Asesoría", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // ===== TARJETA PRINCIPAL =====
            item {
                Card(
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(100.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            "${datosInstructor?.get("nombre") ?: "Cargando"} ${datosInstructor?.get("apellidos") ?: ""}",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.Red
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${datosInstructor?.get("municipioResidencia") ?: ""}, ${datosInstructor?.get("estadoResidencia") ?: ""}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ===== TEMAS =====
            item {
                Spacer(Modifier.height(20.dp))
                Row(Modifier.padding(horizontal = 16.dp)) {
                    temasPorInstructor.forEach { tema ->
                        Card(
                            modifier = Modifier.padding(end = 8.dp),
                            colors = CardDefaults.cardColors(Color(219, 234, 254)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Box(
                                modifier = Modifier.padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(tema, fontSize = 13.sp, color = Color(30, 64, 175))
                            }
                        }
                    }
                }
            }

            // ===== TARIFA =====
            item {
                Spacer(Modifier.height(20.dp))
                Row(Modifier.padding(horizontal = 20.dp)) {
                    Icon(Icons.Default.MonetizationOn, null, tint = Color(254, 216, 98))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Tarifa")
                        Text("$${datosInstructor?.get("cobro") ?: 0}/hr", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ===== MODALIDAD =====
            item {
                Spacer(Modifier.height(20.dp))
                Row(Modifier.padding(horizontal = 20.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Red)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Modalidad")
                        Text(datosInstructor?.get("modalidad")?.toString() ?: "No especificada")
                    }
                }
            }

            // ===== OPINIONES =====
            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Reseñas recientes",
                    modifier = Modifier.padding(20.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            items(opinionesInstructor) { reseña ->

                var nombreEmisor by remember { mutableStateOf("Cargando...") }

                LaunchedEffect(reseña["idEmisor"]) {
                    cargarNombreAprendiz(
                        reseña["idEmisor"] as? String ?: ""
                    ) { nombreEmisor = it }
                }

                Card(
                    elevation = CardDefaults.cardElevation(10.dp),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(nombreEmisor, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text(reseña["opinion"] as String)
                    }
                }
            }
        }
    }
}
