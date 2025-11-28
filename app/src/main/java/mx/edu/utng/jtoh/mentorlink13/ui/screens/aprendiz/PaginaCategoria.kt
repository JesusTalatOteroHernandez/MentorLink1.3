package mx.edu.utng.jtoh.mentorlink13.ui.screens.aprendiz

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.math.floor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaginaCategoria(
    navController: NavController, categoria: String
) {
    val firestore = FirebaseFirestore.getInstance()
    var instructores by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    var temasPorInstructor by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }

    // Cargar temas y relaciones (InstructorTema)
    LaunchedEffect("cargar_temas") {
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

    LaunchedEffect(true) {
        cargarInstructoresPorCategoria(categoria) { docs ->
            instructores = docs
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(categoria) }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar"
                    )
                }
            })
        }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (cargando) {
                Box(
                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (instructores.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text("No hay instructores para esta categoría.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(instructores) { doc ->
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(vertical = 10.dp)
                                .clickable {
                                    navController.navigate("perfil_instructor/${doc["idInstructor"].toString()}")
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(255, 255, 255)
                            )
                        ) {

                            Row {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Usuario",
                                    tint = Color.DarkGray,
                                    modifier = Modifier
                                        .size(130.dp)
                                        .padding(25.dp)
                                )

                                Spacer(Modifier.width(5.dp))

                                Column {
                                    Spacer(Modifier.height(15.dp))

                                    Text(
                                        "${doc["nombre"]} ${doc["apellidos"]}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.SansSerif
                                    )

                                    Spacer(Modifier.height(5.dp))

                                    val idInstructor = doc["idInstructor"] as? String ?: ""
                                    val temas = temasPorInstructor[idInstructor] ?: emptyList()

                                    if (temas.isNotEmpty()) {
                                        Text(
                                            text = temas.joinToString(" • "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }

                                    Spacer(Modifier.height(7.dp))

                                    val puntuacion = doc["puntuacion"]
                                        ?.toString()
                                        ?.toDoubleOrNull() ?: 0.0
                                    val estrellasLlenas = floor(puntuacion).toInt()
                                    val tieneMedia = puntuacion - estrellasLlenas in 0.25..<0.75
                                    val estrellasVacias =
                                        if (tieneMedia) 4 - estrellasLlenas else 5 - estrellasLlenas


                                    Row {
                                        repeat(estrellasLlenas) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Estrella llena",
                                                tint = Color(232, 191, 54),
                                                modifier = Modifier.size(20.dp)
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

                                        Text(
                                            "(${doc["puntuacion"]})",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    Spacer(Modifier.height(4.dp))

                                    Row {
                                        Text(
                                            "$${doc["cobro"]}/hr",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(37, 99, 235)
                                        )

                                        Spacer(Modifier.width(15.dp))

                                        Text(
                                            "• ${doc["modalidad"]}",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.ExtraLight
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
}

fun cargarInstructoresPorCategoria(
    categoriaSeleccionada: String, onResult: (List<Map<String, Any>>) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()

    firestore.collection("temas").get().addOnSuccessListener { temasSnapshot ->

        val idsTemas = mutableListOf<String>()

        for (doc in temasSnapshot.documents) {
            val data = doc.data ?: continue

            if (data.containsKey(categoriaSeleccionada)) {
                idsTemas.add(doc.id)    // ej. "tema123"
            }
        }

        if (idsTemas.isEmpty()) {
            onResult(emptyList())
            return@addOnSuccessListener
        }

        firestore.collection("instructorTema").whereIn("idTema", idsTemas).get()
            .addOnSuccessListener { relSnapshot ->

                val idsInstructores =
                    relSnapshot.documents.mapNotNull { it.getString("idInstructor") }.distinct()

                if (idsInstructores.isEmpty()) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                // Obtener instructores
                firestore.collection("instructores")
                    .whereIn(FieldPath.documentId(), idsInstructores).get()
                    .addOnSuccessListener { instructoresSnapshot ->

                        val listaFinal = mutableListOf<Map<String, Any>>()

                        instructoresSnapshot.forEach { instDoc ->
                            val idUsuario = instDoc.getString("idUsuario") ?: return@forEach

                            firestore.collection("usuarios").document(idUsuario).get()
                                .addOnSuccessListener { usuarioDoc ->

                                    val datos = mapOf(
                                        "idInstructor" to instDoc.id,
                                        "nombre" to usuarioDoc.getString("nombre").orEmpty(),
                                        "apellidos" to usuarioDoc.getString("apellidos")
                                            .orEmpty(),
                                        "modalidad" to instDoc.getString("modalidad").orEmpty(),
                                        "cobro" to (instDoc.getLong("cobro")?.toInt() ?: 0),
                                        "puntuacion" to (instDoc.getDouble("puntuacion") ?: 0.0)
                                    )

                                    listaFinal.add(datos)
                                    onResult(listaFinal.toList())
                                }
                        }
                    }
            }
    }
}