package mx.edu.utng.jtoh.mentorlink13.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import android.util.Log
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Cake
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.floor
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilAprendizVer(idUsuarioAprendiz: String, navController: NavController) {

    val firestore = FirebaseFirestore.getInstance()
    var datosAprendiz by remember { mutableStateOf<Map<String, Any>?>(null) }
    var opinionesAprendiz by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Extraer info del aprendiz
    LaunchedEffect(idUsuarioAprendiz) {
        Log.d("PerfilAprendiz", "Cargando datos del usuario: $idUsuarioAprendiz")

        firestore.collection("usuarios").document(idUsuarioAprendiz).get()
            .addOnSuccessListener { usuarioDoc ->
                Log.d("PerfilAprendiz", "Usuario encontrado: ${usuarioDoc.exists()}")

                if (!usuarioDoc.exists()) {
                    Log.e("PerfilAprendiz", "El usuario no existe")
                    isLoading = false
                    return@addOnSuccessListener
                }

                // Buscar el documento del aprendiz usando idUsuario
                firestore.collection("aprendices")
                    .whereEqualTo("idUsuario", idUsuarioAprendiz)
                    .get()
                    .addOnSuccessListener { aprendicesSnapshot ->
                        Log.d("PerfilAprendiz", "Aprendices encontrados: ${aprendicesSnapshot.size()}")

                        val aprendizDoc = aprendicesSnapshot.documents.firstOrNull()
                        val puntuacion = aprendizDoc?.getDouble("puntuacion") ?: 0.0

                        datosAprendiz = mapOf(
                            "idUsuario" to idUsuarioAprendiz,
                            "idAprendiz" to (aprendizDoc?.id ?: ""),
                            "nombre" to usuarioDoc.getString("nombre").orEmpty(),
                            "apellidos" to usuarioDoc.getString("apellidos").orEmpty(),
                            "estadoResidencia" to usuarioDoc.getString("estadoResidencia").orEmpty(),
                            "municipioResidencia" to usuarioDoc.getString("municipioResidencia").orEmpty(),
                            "edad" to (usuarioDoc.getLong("edad")?.toInt() ?: 0),
                            "correoElectronico" to usuarioDoc.getString("correoElectronico").orEmpty(),
                            "puntuacion" to puntuacion
                        )

                        Log.d("PerfilAprendiz", "Datos del aprendiz: $datosAprendiz")
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        Log.e("PerfilAprendiz", "Error al obtener aprendiz: ${e.message}")

                        // Si no existe en aprendices, cargar solo datos de usuario
                        datosAprendiz = mapOf(
                            "idUsuario" to idUsuarioAprendiz,
                            "idAprendiz" to "",
                            "nombre" to usuarioDoc.getString("nombre").orEmpty(),
                            "apellidos" to usuarioDoc.getString("apellidos").orEmpty(),
                            "estadoResidencia" to usuarioDoc.getString("estadoResidencia").orEmpty(),
                            "municipioResidencia" to usuarioDoc.getString("municipioResidencia").orEmpty(),
                            "edad" to (usuarioDoc.getLong("edad")?.toInt() ?: 0),
                            "correoElectronico" to usuarioDoc.getString("correoElectronico").orEmpty(),
                            "puntuacion" to 0.0
                        )
                        isLoading = false
                    }
            }
            .addOnFailureListener { e ->
                Log.e("PerfilAprendiz", "Error al obtener usuario: ${e.message}")
                isLoading = false
            }
    }

    // Extraer reseñas del aprendiz
    LaunchedEffect(idUsuarioAprendiz) {
        if (idUsuarioAprendiz.isBlank()) return@LaunchedEffect

        Log.d("PerfilAprendiz", "Buscando opiniones para receptor: $idUsuarioAprendiz")

        firestore.collection("opiniones")
            .whereEqualTo("idReceptor", idUsuarioAprendiz)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("PerfilAprendiz", "Opiniones encontradas: ${snapshot.size()}")

                val lista = snapshot.documents.map { doc ->
                    mapOf(
                        "idEmisor" to doc.getString("idEmisor").orEmpty(),
                        "opinion" to doc.getString("opinion").orEmpty(),
                        "puntuacion" to (doc.getLong("puntuacion")?.toInt() ?: 0)
                    )
                }
                opinionesAprendiz = lista
                Log.d("PerfilAprendiz", "Opiniones cargadas: ${opinionesAprendiz.size}")
            }
            .addOnFailureListener { e ->
                Log.e("PerfilAprendiz", "Error al obtener opiniones: ${e.message}")
            }
    }


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
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Perfil del Aprendiz",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // CARD PRINCIPAL CON INFO DEL APRENDIZ
                item {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(255, 255, 255)
                        )
                    ) {
                        Column(
                            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                        ) {
                            Spacer(Modifier.height(30.dp))

                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Usuario",
                                tint = Color.DarkGray,
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                            Spacer(Modifier.height(15.dp))
                            Text(
                                datosAprendiz?.get("nombre")
                                    .toString() + " " + datosAprendiz?.get("apellidos").toString(),
                                fontSize = 25.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Spacer(Modifier.height(15.dp))
                            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Ubicación",
                                    tint = Color.Red,
                                    modifier = Modifier.size(25.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    datosAprendiz?.get("municipioResidencia")
                                        .toString() + ". " + datosAprendiz?.get("estadoResidencia")
                                        .toString(),
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(Modifier.height(30.dp))
                        }
                    }
                }

                // INFORMACIÓN ADICIONAL
                item {
                    Spacer(Modifier.height(20.dp))
                    Column {
                        Row {
                            Spacer(Modifier.width(25.dp))
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color(25, 80, 254),
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(Modifier.width(15.dp))
                            Column {
                                Text(
                                    "Correo Electrónico",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraLight
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    datosAprendiz?.get("correoElectronico").toString(),
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // EDAD
                item {
                    Spacer(Modifier.height(20.dp))
                    Column {
                        Row {
                            Spacer(Modifier.width(25.dp))
                            Icon(
                                imageVector = Icons.Default.Cake,
                                contentDescription = "Edad",
                                tint = Color(232, 191, 54),
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(Modifier.width(15.dp))
                            Column {
                                Text(
                                    "Edad",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraLight
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    "${datosAprendiz?.get("edad")} años",
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // PUNTUACIÓN CON ESTRELLAS
                item {
                    val puntuacion =
                        datosAprendiz?.get("puntuacion").toString().toDoubleOrNull() ?: 0.0
                    val estrellasLlenas = floor(puntuacion).toInt()
                    val tieneMedia = puntuacion - estrellasLlenas in 0.25..<0.75
                    val estrellasVacias =
                        if (tieneMedia) 4 - estrellasLlenas else 5 - estrellasLlenas

                    Spacer(Modifier.height(30.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(estrellasLlenas) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Estrella llena",
                                tint = Color(232, 191, 54),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(1.dp))
                        }

                        // Medias estrellas
                        if (tieneMedia) {
                            Icon(
                                imageVector = Icons.Default.StarHalf,
                                contentDescription = "Media estrella",
                                tint = Color(232, 191, 54),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(1.dp))
                        }

                        Spacer(Modifier.width(8.dp))

                        //Estrellas vacías
                        repeat(estrellasVacias) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Estrella vacía",
                                tint = Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(1.dp))
                        }

                        Spacer(Modifier.width(8.dp))
                        // Texto puntuación
                        Text(
                            text = String.format("%.1f/5.0", puntuacion),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )

                        Spacer(Modifier.width(6.dp))
                    }
                }

                // TÍTULO DE RESEÑAS
                item {
                    Row(modifier = Modifier.padding(20.dp)) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "Reseñas",
                            tint = Color.LightGray,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Text(
                            "Reseñas Recientes",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }

                // RESEÑAS O MENSAJE SI NO HAY
                if (opinionesAprendiz.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.LightGray
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Aún no hay reseñas para este aprendiz",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(opinionesAprendiz) { reseña ->
                        val opinion = reseña["opinion"] as String
                        val puntuacion = reseña["puntuacion"] as Int

                        var nombreEmisor by remember { mutableStateOf("Cargando...") }

                        LaunchedEffect(reseña["idEmisor"]) {
                            val idEmisor = reseña["idEmisor"] as String
                            if (idEmisor.isNotEmpty()) {
                                cargarNombreEmisorVer(idEmisor) {
                                    nombreEmisor = it
                                }
                            }
                        }

                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 25.dp, vertical = 10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(255, 255, 255)
                            )
                        ) {
                            Row {
                                // Borde lateral izquierdo
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .fillMaxHeight()
                                        .background(Color(16, 185, 129))
                                )
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            nombreEmisor,
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.ExtraBold
                                        )

                                        Spacer(Modifier.width(8.dp))

                                        // Estrellas dinámicas según puntuación
                                        repeat(puntuacion) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Estrella",
                                                tint = Color(232, 191, 54),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(2.dp))
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        opinion,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
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

fun cargarNombreEmisorVer(idEmisor: String, callback: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()

    firestore.collection("usuarios")
        .document(idEmisor)
        .get()
        .addOnSuccessListener { doc ->
            val nombre = doc.getString("nombre").orEmpty()
            val apellidos = doc.getString("apellidos").orEmpty()
            callback("$nombre $apellidos")
        }
        .addOnFailureListener {
            callback("Usuario")
        }
}