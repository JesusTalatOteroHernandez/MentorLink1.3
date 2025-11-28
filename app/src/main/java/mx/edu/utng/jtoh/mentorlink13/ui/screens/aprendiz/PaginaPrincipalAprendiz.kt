package mx.edu.utng.jtoh.mentorlink13.ui.screens.aprendiz

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaginaPrincipalAprendiz(navController: NavController) {
    var searchText by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var instructores by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var todosInstructores by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var instructoresFiltrados by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var temasPorInstructor by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }

    var userId by remember { mutableStateOf("") }

    // Dialog de confirmación de cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Cerrar sesión",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Estás seguro que deseas cerrar sesión?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        auth.signOut()
                        showLogoutDialog = false
                        navController.navigate("pantalla_inicio") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Sí, cerrar sesión", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    //Consultar informacion en la firestore para instructores destacados
    LaunchedEffect("cargar_destacados") {
        firestore.collection("instructores")
            .whereEqualTo("puntuacion", 5.0) // Solo destacados
            .get()
            .addOnSuccessListener { instructoresSnapshot ->

                val listaTemp = mutableListOf<Map<String, Any>>()
                var consultasRestantes = instructoresSnapshot.size()

                val user = FirebaseAuth.getInstance().currentUser
                userId = user?.uid ?: ""
                if (consultasRestantes == 0) {
                    instructores = emptyList()
                    return@addOnSuccessListener
                }

                instructoresSnapshot.forEach { instDoc ->

                    val idUsuario = instDoc.getString("idUsuario") ?: return@forEach

                    // Obtener datos del usuario
                    firestore.collection("usuarios")
                        .document(idUsuario)
                        .get()
                        .addOnSuccessListener { usuarioDoc ->

                            val datosCombinados = mapOf(
                                "idInstructor" to instDoc.id,
                                "nombre" to usuarioDoc.getString("nombre").orEmpty(),
                                "apellidos" to usuarioDoc.getString("apellidos").orEmpty(),
                                "modalidad" to instDoc.getString("modalidad").orEmpty(),
                                "cobro" to (instDoc.getLong("cobro")?.toInt() ?: 0),
                                "puntuacion" to (instDoc.getDouble("puntuacion") ?: 0.0)
                            )

                            listaTemp.add(datosCombinados)
                        }
                        .addOnCompleteListener {
                            consultasRestantes--
                            if (consultasRestantes == 0) {
                                instructores = listaTemp
                            }
                        }
                }
            }
    }

    //Lista de TODOS los Instructores
    LaunchedEffect("cargar_todos") {
        firestore.collection("instructores")
            .get()
            .addOnSuccessListener { instructoresSnapshot ->

                val listaTemp = mutableListOf<Map<String, Any>>()
                var consultasRestantes = instructoresSnapshot.size()

                if (consultasRestantes == 0) {
                    todosInstructores = emptyList()
                    return@addOnSuccessListener
                }

                instructoresSnapshot.forEach { instDoc ->

                    val idUsuario = instDoc.getString("idUsuario") ?: return@forEach

                    firestore.collection("usuarios")
                        .document(idUsuario)
                        .get()
                        .addOnSuccessListener { usuarioDoc ->

                            val datos = mapOf(
                                "idInstructor" to instDoc.id,
                                "nombre" to usuarioDoc.getString("nombre").orEmpty(),
                                "apellidos" to usuarioDoc.getString("apellidos").orEmpty(),
                                "modalidad" to instDoc.getString("modalidad").orEmpty(),
                                "cobro" to (instDoc.getLong("cobro")?.toInt() ?: 0),
                                "puntuacion" to (instDoc.getDouble("puntuacion") ?: 0.0)
                            )

                            listaTemp.add(datos)
                        }
                        .addOnCompleteListener {
                            consultasRestantes--
                            if (consultasRestantes == 0) {
                                todosInstructores = listaTemp
                            }
                        }
                }
            }
    }

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

    //Columna principal
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
                //.padding(top = 20.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(37, 99, 235)
            ),
            shape = RoundedCornerShape(30.dp)

        ) {
            Column(
                modifier = Modifier.padding(16.dp)
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "MentorLink",
                        fontSize = 25.sp,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(7.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            navController.navigate("notificaciones_aprendiz")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color.Yellow,
                                modifier = Modifier.size(25.dp)
                            )
                        }

                        IconButton(onClick = {
                            navController.navigate("perfil_aprendiz/$userId")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil",
                                tint = Color.White,
                                modifier = Modifier.size(25.dp)
                            )
                        }

                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Cerrar sesión",
                                tint = Color.White,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                TextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        val query = it.lowercase().trim()

                        instructoresFiltrados =
                            if (query.isEmpty()) {
                                emptyList()
                            } else {
                                todosInstructores.filter { inst ->
                                    val nombre = inst["nombre"].toString().lowercase()
                                    val apellidos = inst["apellidos"].toString().lowercase()
                                    val modalidad = inst["modalidad"].toString().lowercase()

                                    val idInstructor = inst["idInstructor"]?.toString() ?: ""

                                    val temas = temasPorInstructor[idInstructor] ?: emptyList()

                                    val temasCoinciden =
                                        temas.any { it.lowercase().contains(query) }

                                    nombre.contains(query) ||
                                            apellidos.contains(query) ||
                                            modalidad.contains(query) ||
                                            temasCoinciden
                                }
                            }
                    },
                    placeholder = {
                        Text("Buscar instructor o tema...")
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color.Gray,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                )
            }
        }

        // Resultados de búsqueda
        if (searchText.isNotEmpty()) {
            Column(modifier = Modifier.padding(16.dp)) {
                instructoresFiltrados.forEach { inst ->

                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 10.dp)
                            .clickable {
                                navController.navigate("perfil_instructor/${inst["idInstructor"].toString()}")
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
                                    "${inst["nombre"]} ${inst["apellidos"]}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.SansSerif
                                )

                                Spacer(Modifier.height(5.dp))

                                val idInstructor = inst["idInstructor"] as? String ?: ""
                                val temas = temasPorInstructor[idInstructor] ?: emptyList()

                                if (temas.isNotEmpty()) {
                                    Text(
                                        text = temas.joinToString(" • "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(Modifier.height(7.dp))

                                val puntuacion = inst["puntuacion"]
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
                                        "(${inst["puntuacion"]})",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }

                                Spacer(Modifier.height(4.dp))

                                Row {
                                    Text(
                                        "$${inst["cobro"]}/hr",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(37, 99, 235)
                                    )

                                    Spacer(Modifier.width(15.dp))

                                    Text(
                                        "• ${inst["modalidad"]}",
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

        // Solo mostrar categorías e instructores destacados cuando NO hay búsqueda
        if (searchText.isEmpty()) {
            Spacer(Modifier.height(18.dp))

            Row {
                Text("          ")
                Icon(
                    imageVector = Icons.Default.CollectionsBookmark,
                    contentDescription = "Categorías",
                    tint = Color.Red,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    "Categorías", fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(1.dp),
                    fontFamily = FontFamily.SansSerif
                )
            }
            Spacer(Modifier.height(17.dp))

            Row {
                Spacer(Modifier.width(18.dp))
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(170.dp, 100.dp)
                        .clickable {
                            navController.navigate("categoria/Programacion")
                        },
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
                                .background(Color(245, 158, 11))
                        )
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = "Computadora",
                                tint = Color(69, 187, 242),
                                modifier = Modifier.size(25.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("Programación", fontWeight = FontWeight.Bold)

                        }
                    }
                }
                Spacer(Modifier.width(18.dp))
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(170.dp, 100.dp)
                        .clickable {
                            navController.navigate("categoria/Idiomas")
                        },
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
                            Icon(
                                imageVector = Icons.Default.SignLanguage,
                                contentDescription = "Idiomas",
                                tint = Color(104, 173, 110),
                                modifier = Modifier.size(25.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("Idiomas", fontWeight = FontWeight.Bold)

                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row {
                Spacer(Modifier.width(18.dp))
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(170.dp, 100.dp)
                        .clickable {
                            navController.navigate("categoria/Musica")
                        },
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
                                .background(Color.Blue)
                        )
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Música",
                                tint = Color(105, 95, 155),
                                modifier = Modifier.size(25.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("Música", fontWeight = FontWeight.Bold)

                        }
                    }
                }
                Spacer(Modifier.width(18.dp))
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(170.dp, 100.dp)
                        .clickable {
                            navController.navigate("categoria/Matematicas")
                        },
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
                                .background(Color(245, 158, 11))
                        )
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Matemáticas",
                                tint = Color(255, 173, 11),
                                modifier = Modifier.size(25.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("Matemáticas", fontWeight = FontWeight.Bold)

                        }
                    }
                }
            }
            Spacer(Modifier.height(45.dp))

            Row {
                Spacer(Modifier.width(20.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Destacados",
                    tint = Color(232, 191, 54),
                    modifier = Modifier.size(25.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    "Instructores Destacados",
                    modifier = Modifier.padding(2.dp),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                instructores.forEach { inst ->

                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 10.dp)
                            .clickable {
                                navController.navigate("perfil_instructor/${inst["idInstructor"].toString()}")
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
                                    "${inst["nombre"]} ${inst["apellidos"]}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.SansSerif
                                )

                                Spacer(Modifier.height(5.dp))

                                Text(
                                    "Modalidad: ${inst["modalidad"]}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraLight,
                                    fontFamily = FontFamily.SansSerif
                                )

                                Spacer(Modifier.height(7.dp))

                                val puntuacion = inst["puntuacion"]
                                    ?.toString()
                                    ?.toDoubleOrNull() ?: 0.0
                                val estrellasLlenas = floor(puntuacion).toInt()

                                Row {
                                    repeat(estrellasLlenas) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Estrella",
                                            tint = Color(232, 191, 54),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(1.dp))
                                    }

                                    repeat(5 - estrellasLlenas) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Estrella vacía",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(1.dp))
                                    }

                                    Text(
                                        "(${inst["puntuacion"]})",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        modifier = Modifier.padding(5.dp)
                                    )
                                }

                                Spacer(Modifier.height(4.dp))

                                Row {
                                    Text(
                                        "$${inst["cobro"]}/hr",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(37, 99, 235)
                                    )

                                    Spacer(Modifier.width(15.dp))

                                    Text(
                                        "• ${inst["modalidad"]}",
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