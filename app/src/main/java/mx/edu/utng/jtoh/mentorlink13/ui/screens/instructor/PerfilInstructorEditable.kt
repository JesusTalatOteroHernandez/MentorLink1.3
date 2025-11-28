package mx.edu.utng.jtoh.mentorlink13.ui.screens.instructor

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
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
    val currentUser = auth.currentUser

    var datosInstructor by remember { mutableStateOf<Map<String, Any>?>(null) }
    var temasPorInstructor by remember { mutableStateOf<List<String>>(emptyList()) }
    var opinionesInstructor by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var modoEdicion by remember { mutableStateOf(false) }
    var idInstructor by remember { mutableStateOf("") }

    // Estados para edición
    var modalidadEdit by remember { mutableStateOf("") }
    var cobroEdit by remember { mutableStateOf("") }
    var disponibilidadEdit by remember { mutableStateOf("") }

    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    // Extraer info del instructor actual
    LaunchedEffect(currentUser?.uid) {
        if (currentUser == null) return@LaunchedEffect

        firestore.collection("instructores")
            .whereEqualTo("idUsuario", currentUser.uid)
            .get()
            .addOnSuccessListener { instSnapshot ->
                if (!instSnapshot.isEmpty) {
                    val instDoc = instSnapshot.documents[0]
                    idInstructor = instDoc.id

                    firestore.collection("usuarios").document(currentUser.uid).get()
                        .addOnSuccessListener { usuarioDoc ->
                            datosInstructor = mapOf(
                                "idInstructor" to instDoc.id,
                                "nombre" to usuarioDoc.getString("nombre").orEmpty(),
                                "apellidos" to usuarioDoc.getString("apellidos").orEmpty(),
                                "modalidad" to instDoc.getString("modalidad").orEmpty(),
                                "disponibilidad" to instDoc.getString("disponibilidad").orEmpty(),
                                "estadoResidencia" to usuarioDoc.getString("estadoResidencia")
                                    .orEmpty(),
                                "municipioResidencia" to usuarioDoc.getString("municipioResidencia")
                                    .orEmpty(),
                                "cobro" to (instDoc.getLong("cobro")?.toInt() ?: 0),
                                "puntuacion" to (instDoc.getDouble("puntuacion") ?: 0.0)
                            )
                            modalidadEdit = instDoc.getString("modalidad").orEmpty()
                            disponibilidadEdit = instDoc.getString("disponibilidad").orEmpty()
                            cobroEdit = (instDoc.getLong("cobro")?.toInt() ?: 0).toString()

                        }
                }
            }
    }

    /*
    // Extraer temas del instructor
    LaunchedEffect(idInstructor) {
        if (idInstructor.isEmpty()) return@LaunchedEffect

        firestore.collection("temas")
            .get()
            .addOnSuccessListener { temasSnapshot ->
                val mapaTemas = mutableMapOf<String, String>()

                temasSnapshot.forEach { doc ->
                    doc.data.forEach { (_, valor) ->
                        mapaTemas[doc.id] = valor.toString()
                    }
                }

                firestore.collection("instructorTema")
                    .whereEqualTo("idInstructor", idInstructor)
                    .get()
                    .addOnSuccessListener { relSnapshot ->
                        val listaTemas = mutableListOf<String>()

                        relSnapshot.forEach { relDoc ->
                            val idTema = relDoc.getString("idTema") ?: return@forEach
                            val nombreTema = mapaTemas[idTema] ?: ""

                            if (nombreTema.isNotBlank()) {
                                listaTemas.add(nombreTema)
                            }
                        }

                        temasPorInstructor = listaTemas
                    }
            }
    }

     */

    // Extraer reseñas del instructor
    LaunchedEffect(idInstructor) {
        if (idInstructor.isEmpty()) return@LaunchedEffect

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

    //Extraer la disponibilidad del instructor en la asesoria
    LaunchedEffect(idInstructor) {
        if (idInstructor.isEmpty()) return@LaunchedEffect

        firestore.collection("asesorias")
            .whereEqualTo("idInstructor", idInstructor)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val asesoria = documents.documents[0]
                    val modalidad = asesoria.getString("modalidad") ?: "No especificada"

                    // Actualizar datosInstructor con los datos de asesoría
                    datosInstructor = datosInstructor?.toMutableMap()?.apply {
                        put("modalidad", modalidad)
                    }

                    modalidadEdit = modalidad
                }
            }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B5FDB))
                    .padding(horizontal = 30.dp, vertical = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                IconButton(onClick = {
                    navController.navigate("pantalla_de_inicio_tutor") {
                        popUpTo("pantalla_de_inicio_tutor") { inclusive = true }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White,
                        modifier = Modifier.size(35.dp)
                            .clickable(
                                onClick = {
                                    navController.navigate("pantalla_de_inicio_tutor")
                                }
                            )
                    )
                }

                Text(
                    text = "Mi Perfil",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = {
                        mostrarDialogoCerrarSesion = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Cerrar Sesión",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tarjeta de información principal
            item {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally)
                            .padding(vertical = 30.dp)
                    ) {
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
                            datosInstructor?.get("nombre").toString() + " " +
                                    datosInstructor?.get("apellidos").toString(),
                            fontSize = 25.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                        )

                        Spacer(Modifier.height(15.dp))

                        Row(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Ubicación",
                                tint = Color.Red,
                                modifier = Modifier.size(25.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                datosInstructor?.get("municipioResidencia").toString() + ". " +
                                        datosInstructor?.get("estadoResidencia").toString(),
                                fontSize = 15.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Temas/Especialidades
            item {
                if (temasPorInstructor.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        temasPorInstructor.forEach { tema ->
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
            }

            // Sección editable o vista
            if (!modoEdicion) {
                // Vista normal
                item {
                    Spacer(Modifier.height(20.dp))
                    Column {
                        Row {
                            Spacer(Modifier.width(25.dp))
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Dinero",
                                tint = Color(254, 216, 98),
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(Modifier.width(15.dp))
                            Column {
                                Text(
                                    "Tarifa",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraLight
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    "$" + datosInstructor?.get("cobro").toString() + "/hr",
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold
                                )
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
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(Modifier.width(15.dp))
                            Column {
                                Text(
                                    "Disponibilidad",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraLight
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    datosInstructor?.get("disponibilidad").toString(),
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold
                                )
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
                                contentDescription = "Ubicación",
                                tint = Color.Red,
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(Modifier.width(15.dp))
                            Column {
                                Text(
                                    "Modalidad",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraLight
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    datosInstructor?.get("modalidad").toString(),
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Botón Editar
                item {
                    Button(
                        onClick = { modoEdicion = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp, vertical = 20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2B5FDB)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Editar Perfil", fontSize = 16.sp)
                    }
                }

            } else {
                // Modo edición
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                "Editar Información",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )

                            Spacer(Modifier.height(20.dp))

                            // Campo Modalidad
                            var expandedModalidad by remember { mutableStateOf(false) }
                            val opcionesModalidad = listOf("Presencial", "Virtual", "Virtual y Presencial")

                            ExposedDropdownMenuBox (
                                expanded = expandedModalidad,
                                onExpandedChange = { expandedModalidad = it }
                            ) {
                                OutlinedTextField(
                                    value = modalidadEdit,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Modalidad") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModalidad)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Modalidad"
                                        )
                                    },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                )

                                ExposedDropdownMenu(
                                    expanded = expandedModalidad,
                                    onDismissRequest = { expandedModalidad = false }
                                ) {
                                    opcionesModalidad.forEach { opcion ->
                                        DropdownMenuItem(
                                            text = { Text(opcion) },
                                            onClick = {
                                                modalidadEdit = opcion
                                                expandedModalidad = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Campo Disponibilidad
                            OutlinedTextField(
                                value = disponibilidadEdit,
                                onValueChange = { disponibilidadEdit = it },
                                label = { Text("Disponibilidad") },
                                placeholder = { Text("Ej: Lun-Vie 6-9pm") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Disponibilidad"
                                    )
                                }
                            )

                            Spacer(Modifier.height(16.dp))

                            // Campo Cobro
                            OutlinedTextField(
                                value = cobroEdit,
                                onValueChange = { cobroEdit = it },
                                label = { Text("Tarifa por hora ($)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = "Cobro"
                                    )
                                }
                            )

                            Spacer(Modifier.height(24.dp))

                            // Botones
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        modoEdicion = false
                                        modalidadEdit = datosInstructor?.get("modalidad").toString()
                                        cobroEdit = datosInstructor?.get("cobro").toString()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancelar")
                                }

                                Button(
                                    onClick = {
                                        val cobroInt = cobroEdit.toIntOrNull() ?: 0

                                        if (idInstructor.isNotEmpty()) {
                                            // Actualizar TODO en instructores
                                            firestore.collection("instructores")
                                                .document(idInstructor)
                                                .update(
                                                    mapOf(
                                                        "cobro" to cobroInt,
                                                        "modalidad" to modalidadEdit,
                                                        "disponibilidad" to disponibilidadEdit
                                                    )
                                                )
                                                .addOnSuccessListener {
                                                    datosInstructor = datosInstructor?.toMutableMap()?.apply {
                                                        put("modalidad", modalidadEdit)
                                                        put("cobro", cobroInt)
                                                        put("disponibilidad", disponibilidadEdit)
                                                    }
                                                    modoEdicion = false
                                                    Toast.makeText(
                                                        navController.context,
                                                        "Perfil actualizado",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(
                                                        navController.context,
                                                        "Error al actualizar",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2B5FDB)
                                    )
                                ) {
                                    Text("Guardar")
                                }
                            }
                        }
                    }
                }
            }

            // Puntuación
            item {
                val puntuacion = datosInstructor?.get("puntuacion").toString().toDoubleOrNull() ?: 0.0
                val estrellasLlenas = floor(puntuacion).toInt()
                val tieneMedia = puntuacion - estrellasLlenas in 0.25..<0.75
                val estrellasVacias = if (tieneMedia) 4 - estrellasLlenas else 5 - estrellasLlenas

                Spacer(Modifier.height(30.dp))

                Row(
                    modifier = Modifier.padding(horizontal = 25.dp),
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

                    Text(
                        text = String.format("%.1f/5.0", puntuacion),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            // Sección de reseñas
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
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Reseñas Recientes",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Lista de reseñas
            items(opinionesInstructor) { reseña ->
                val opinion = reseña["opinion"] as String
                val puntuacion = reseña["puntuacion"] as Int
                var nombreEmisor by remember { mutableStateOf("Cargando...") }

                LaunchedEffect(reseña["idEmisor"]) {
                    val idEmisor = reseña["idEmisor"] as String
                    if (idEmisor.isNotEmpty()) {
                        firestore.collection("usuarios").document(idEmisor)
                            .get()
                            .addOnSuccessListener { doc ->
                                nombreEmisor = doc.getString("nombre").orEmpty() + " " +
                                        doc.getString("apellidos").orEmpty()
                            }
                    }
                }

                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(100.dp)
                                .background(Color(16, 185, 129))
                        )

                        Column(
                            modifier = Modifier.padding(12.dp)
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

                                repeat(puntuacion) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Estrella",
                                        tint = Color(232, 191, 54),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                opinion,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            // Espaciado final
            item {
                Spacer(Modifier.height(40.dp))
            }
        }
        if (mostrarDialogoCerrarSesion) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoCerrarSesion = false },
                title = {
                    Text(
                        text = "Cerrar Sesión",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("¿Estás seguro de que deseas cerrar sesión?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            auth.signOut()
                            navController.navigate("pantalla_inicio") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2B5FDB)
                        )
                    ) {
                        Text("Sí, cerrar sesión")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { mostrarDialogoCerrarSesion = false }
                    ) {
                        Text("Cancelar")
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

