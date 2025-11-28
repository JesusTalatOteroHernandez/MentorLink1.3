package mx.edu.utng.jtoh.mentorlink13.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilInstructor(idInstructor: String, navController: NavController) {

    val firestore = FirebaseFirestore.getInstance()
    var datosInstructor by remember { mutableStateOf<Map<String, Any>?>(null) }

    var temasPorInstructor by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }

    var opinionesInstructor by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    //Extraer info de instructor
    LaunchedEffect(idInstructor) {
        firestore.collection("instructores").document(idInstructor).get()
            .addOnSuccessListener { instDoc ->
                val idUsuario = instDoc.getString("idUsuario") ?: return@addOnSuccessListener

                firestore.collection("usuarios").document(idUsuario).get()
                    .addOnSuccessListener { usuarioDoc ->
                        datosInstructor = mapOf(
                            "idInstructor" to idInstructor,  // ✅ AGREGADO
                            "nombre" to usuarioDoc.getString("nombre").orEmpty(),
                            "apellidos" to usuarioDoc.getString("apellidos").orEmpty(),
                            "modalidad" to instDoc.getString("modalidad").orEmpty(),
                            "estadoResidencia" to usuarioDoc.getString("estadoResidencia").orEmpty(),
                            "municipioResidencia" to usuarioDoc.getString("municipioResidencia").orEmpty(),
                            "cobro" to (instDoc.getLong("cobro")?.toInt() ?: 0),
                            "puntuacion" to (instDoc.getDouble("puntuacion") ?: 0.0)
                        )
                    }
            }
    }

    //Extraer temas de instructor
    LaunchedEffect(true) {
        // 1. Cargar documentos de Temas
        firestore.collection("temas")
            .get()
            .addOnSuccessListener { temasSnapshot ->

                // Mapa temporal para guardar temas por IDtema
                val mapaTemas = mutableMapOf<String, String>()

                temasSnapshot.forEach { doc ->
                    doc.data.forEach { (campo, valor) ->
                        mapaTemas[doc.id] = valor.toString()  // Valor del campo: "Calculo integral"
                    }
                }

                // 2. Cargar relaciones instructor-tema
                firestore.collection("instructorTema")
                    .get()
                    .addOnSuccessListener { relSnapshot ->

                        val mapaFinal = mutableMapOf<String, MutableList<String>>()

                        relSnapshot.forEach { relDoc ->
                            val idInstructor = relDoc.getString("idInstructor") ?: return@forEach
                            val idTema = relDoc.getString("idTema") ?: return@forEach
                            val nombreTema = mapaTemas[idTema] ?: ""

                            if (nombreTema.isNotBlank()) {
                                val lista = mapaFinal.getOrPut(idInstructor) { mutableListOf() }
                                lista.add(nombreTema)
                            }
                        }

                        temasPorInstructor = mapaFinal.toMap()
                    }
            }
    }

    //Extraer reseñas de instructor
    LaunchedEffect(idInstructor) {
        if (idInstructor.isBlank()) return@LaunchedEffect

        firestore.collection("opiniones")
            .whereEqualTo("idReceptor", idInstructor)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.documents.map { doc ->
                    mapOf(
                        "idEmisor" to doc.getString("idEmisor").orEmpty(),
                        "opinion" to doc.getString("opinion").orEmpty(),
                        "puntuacion" to (doc.getLong("puntuacion")?.toInt() ?: 0)
                    )
                }
                opinionesInstructor = lista
            }
    }

    Scaffold(
        topBar = {
            // TopBar Azul Superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B5FDB))
                    .padding(horizontal = 25.dp, vertical = 25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    navController.navigate("principal_aprendiz") {
                        popUpTo("principal_aprendiz") { inclusive = true }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White,
                        modifier = Modifier.size(35.dp)
                            .clickable(
                                onClick = {
                                    navController.navigate("pantalla_de_inicio_aprendiz")
                                }
                            )
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
            // BottomBar Naranja (Botón clickeable)
            Button(
                onClick = {
                    // ✅ OPCIÓN 1: Usar directamente el parámetro idInstructor (MÁS SIMPLE)
                    navController.navigate("solicitar_asesoria/$idInstructor")

                    // O si prefieres usar datosInstructor (comentar la línea de arriba y descomentar esta):
                    // navController.navigate("solicitar_asesoria/${datosInstructor?.get("idInstructor")}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE67E22)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Solicitar Asesoría",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    ) { innerPadding ->
        // Contenido
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding))
        {
            item {
                Column {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(255, 255, 255)
                        )
                    ) {
                        Column(modifier = Modifier.align(alignment = Alignment.CenterHorizontally)) {
                            Spacer(Modifier.height(30.dp))

                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Usuario",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally)
                            )
                            Spacer(Modifier.height(15.dp))
                            Text(
                                datosInstructor?.get("nombre").toString()+" "+
                                        datosInstructor?.get("apellidos").toString(),
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
                                    datosInstructor?.get("municipioResidencia").toString()+". "+
                                            datosInstructor?.get("estadoResidencia").toString(),
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(Modifier.height(30.dp))
                        }
                    }
                }
            }

            item {
                // ✅ CORREGIDO: Usar el parámetro idInstructor directamente
                val temas = temasPorInstructor[idInstructor] ?: emptyList()

                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    temas.forEach { tema ->
                        Card(
                            modifier = Modifier
                                .height(30.dp)
                                .padding(end = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(219, 234, 254)
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tema,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color(30, 64, 175),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
                Column {
                    Row {
                        Spacer(Modifier.width(25.dp))
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Dinero",
                            tint = Color(254, 216, 98),
                            modifier = Modifier.size(30.dp).align(Alignment.CenterVertically)
                        )
                        Spacer(Modifier.width(15.dp))
                        Column {
                            Text("Tarifa",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraLight)
                            Spacer(Modifier.height(5.dp))
                            Text(
                                "$"+datosInstructor?.get("cobro").toString()+"/hr",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(20.dp))
                Column {
                    Row {
                        Spacer(Modifier.width(25.dp))
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Tiempo",
                            tint = Color(25, 80, 254),
                            modifier = Modifier.size(30.dp).align(Alignment.CenterVertically)
                        )
                        Spacer(Modifier.width(15.dp))
                        Column {
                            Text("Disponibilidad",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraLight)
                            Spacer(Modifier.height(5.dp))
                            Text("Lun-Vie 6-9pm",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(20.dp))
                Column {
                    Row {
                        Spacer(Modifier.width(25.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Univación",
                            tint = Color.Red,
                            modifier = Modifier.size(30.dp).align(Alignment.CenterVertically)
                        )
                        Spacer(Modifier.width(15.dp))
                        Column {
                            Text("Modalidad",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraLight)
                            Spacer(Modifier.height(5.dp))
                            Text(
                                datosInstructor?.get("modalidad").toString(),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item{
                val puntuacion = datosInstructor?.get("puntuacion").toString().toDoubleOrNull() ?: 0.0
                val estrellasLlenas = floor(puntuacion).toInt()
                val tieneMedia = puntuacion - estrellasLlenas in 0.25..<0.75
                val estrellasVacias = if (tieneMedia) 4 - estrellasLlenas else 5 - estrellasLlenas

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
                    // Texto 4.9/5.0
                    Text(
                        text = String.format("%.1f/5.0", puntuacion),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(Modifier.width(6.dp))
                }
            }
            item {

                Row(modifier = Modifier.padding(20.dp)) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "Reseñas",
                        tint = Color.LightGray,
                        modifier = Modifier.size(20.dp).align(Alignment.CenterVertically)
                    )
                    Text("Reseñas Recientes",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(2.dp))
                }
            }

            items(opinionesInstructor) { reseña ->

                val opinion = reseña["opinion"] as String
                val puntuacion = reseña["puntuacion"] as Int
                val nombre = reseña["idEmisor"] as String   // Luego puedes cambiarlo por su nombre real

                var nombreEmisor by remember {mutableStateOf("Cargando...")}

                LaunchedEffect(reseña["idEmisor"]) {
                    val idEmisor = reseña["idEmisor"] as String
                    if (idEmisor.isNotEmpty()) {
                        cargarNombreAprendiz(idEmisor) {
                            nombreEmisor = it
                        }
                    }
                }

                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .size(120.dp, 180.dp)
                        .padding(25.dp),
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
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Row {
                                Text(
                                    nombreEmisor,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(Modifier.width(5.dp))

                                // ⭐ Pintar estrellas dinámicas según puntuación
                                repeat(puntuacion) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Estrella",
                                        tint = Color(232, 191, 54),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(1.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        opinion,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

fun cargarNombreAprendiz(idAprendiz: String, callback: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()

    firestore.collection("usuarios")
        .document(idAprendiz)
        .get()
        .addOnSuccessListener { doc ->
            val nombre = doc.getString("nombre").orEmpty()
            val apellidos = doc.getString("apellidos").orEmpty()
            callback("$nombre $apellidos")
        }
}