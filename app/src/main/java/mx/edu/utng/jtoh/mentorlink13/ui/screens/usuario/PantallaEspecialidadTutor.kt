package mx.edu.utng.jtoh.mentorlink13.ui.screens.usuario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EspecialidadTutor(navController: NavController){
    var materiaSeleccionada by remember { mutableStateOf("") }
    var especializacionSeleccionada by remember { mutableStateOf("") }
    var modalidadSeleccionada by remember { mutableStateOf("") }
    var cobro by remember { mutableStateOf("0") }
    var message by remember { mutableStateOf("") }

    // Lista de especializaciones agregadas
    var especializacionesAgregadas by remember { mutableStateOf<MutableList<Triple<String, String, String>>>(mutableListOf()) }

    // Estados para los temas agrupados
    var temasAgrupados by remember { mutableStateOf<Map<String, List<Pair<String, String>>>>(emptyMap()) }
    var especializaciones by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scrollState = rememberScrollState()

    // Cargar temas desde Firebase
    LaunchedEffect(Unit) {
        db.collection("temas")
            .get()
            .addOnSuccessListener { result ->
                val agrupados = mutableMapOf<String, MutableList<Pair<String, String>>>()

                for (document in result) {
                    val idTema = document.id
                    val materiaTema = document.getString("Programacion")
                        ?: document.getString("Matematicas")
                        ?: document.getString("Español")
                        ?: ""

                    // Determinar la materia principal
                    val materia = when {
                        document.contains("Programacion") -> "Programacion"
                        document.contains("Matematicas") -> "Matematicas"
                        document.contains("Español") -> "Español"
                        else -> ""
                    }

                    if (materia.isNotEmpty() && materiaTema.isNotEmpty()) {
                        if (!agrupados.containsKey(materia)) {
                            agrupados[materia] = mutableListOf()
                        }
                        agrupados[materia]?.add(Pair(idTema, materiaTema))
                    }
                }

                temasAgrupados = agrupados
            }
    }

    // Actualizar especializaciones cuando cambia la materia
    LaunchedEffect(materiaSeleccionada) {
        if (materiaSeleccionada.isNotEmpty()) {
            especializaciones = temasAgrupados[materiaSeleccionada] ?: emptyList()
            especializacionSeleccionada = ""
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(180, 180, 180)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Título
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("INFORMACIÓN DE ESPECIALIZACIÓN")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Contenido principal
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // SELECCIÓN DE MATERIA
                    Text(
                        "Selecciona la materia:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(3.dp))

                    var expandedMateria by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expandedMateria,
                        onExpandedChange = { expandedMateria = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = materiaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Materia") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMateria)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedMateria,
                            onDismissRequest = { expandedMateria = false }
                        ) {
                            temasAgrupados.keys.forEach { materia ->
                                DropdownMenuItem(
                                    text = { Text(materia) },
                                    onClick = {
                                        materiaSeleccionada = materia
                                        expandedMateria = false
                                    }
                                )
                            }
                        }
                    }

                    // SELECCIÓN DE ESPECIALIZACIÓN
                    if (materiaSeleccionada.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Selecciona la especialización:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(3.dp))

                        var expandedEspecializacion by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expandedEspecializacion,
                            onExpandedChange = { expandedEspecializacion = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = especializaciones.find { it.first == especializacionSeleccionada }?.second ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Especialización") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEspecializacion)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedEspecializacion,
                                onDismissRequest = { expandedEspecializacion = false }
                            ) {
                                especializaciones.forEach { (idTema, nombreEspecializacion) ->
                                    DropdownMenuItem(
                                        text = { Text(nombreEspecializacion) },
                                        onClick = {
                                            especializacionSeleccionada = idTema
                                            expandedEspecializacion = false
                                        }
                                    )
                                }
                            }
                        }

                        // BOTÓN AGREGAR MATERIA
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (materiaSeleccionada.isNotEmpty() && especializacionSeleccionada.isNotEmpty()) {
                                    val nombreEsp = especializaciones.find { it.first == especializacionSeleccionada }?.second ?: ""
                                    val yaExiste = especializacionesAgregadas.any { it.second == especializacionSeleccionada }

                                    if (!yaExiste) {
                                        especializacionesAgregadas.add(Triple(materiaSeleccionada, especializacionSeleccionada, nombreEsp))
                                        message = "Materia agregada"
                                        materiaSeleccionada = ""
                                        especializacionSeleccionada = ""
                                    } else {
                                        message = "Esta especialización ya fue agregada"
                                    }
                                } else {
                                    message = "Selecciona materia y especialización"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(Color(37, 99, 135)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("+ Agregar esta materia")
                        }
                    }

                    // LISTA DE MATERIAS AGREGADAS
                    if (especializacionesAgregadas.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Materias agregadas:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(37, 99, 135)
                        )
                        Spacer(Modifier.height(8.dp))

                        especializacionesAgregadas.forEachIndexed { index, triple ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(220, 240, 250)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = triple.first,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(37, 99, 135)
                                        )
                                        Text(
                                            text = triple.third,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            especializacionesAgregadas.removeAt(index)
                                            message = "Materia eliminada"
                                        },
                                        colors = ButtonDefaults.buttonColors(Color.Red),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Eliminar", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    // MODALIDAD
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Selecciona la modalidad de enseñanza:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Botón Presencial
                        Button(
                            onClick = { modalidadSeleccionada = "Presencial" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (modalidadSeleccionada == "Presencial")
                                    Color(37, 99, 135)
                                else
                                    Color.LightGray
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Text("Presencial", maxLines = 1)
                        }

                        // Botón Virtual
                        Button(
                            onClick = { modalidadSeleccionada = "Virtual" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (modalidadSeleccionada == "Virtual")
                                    Color(37, 99, 135)
                                else
                                    Color.LightGray
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Text("Virtual", maxLines = 1)
                        }

                        // Botón Virtual y Presencial
                        Button(
                            onClick = { modalidadSeleccionada = "Virtual y Presencial" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (modalidadSeleccionada == "Virtual y Presencial")
                                    Color(37, 99, 135)
                                else
                                    Color.LightGray
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Text("Ambas", maxLines = 1)
                        }
                    }

                    // COBRO POR ASESORÍA
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Ingresa el cobro por asesoría (MXN):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Si no cobrarás, déjalo en 0",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(3.dp))

                    OutlinedTextField(
                        value = cobro,
                        onValueChange = {
                            // Solo permitir números
                            if (it.all { char -> char.isDigit() }) {
                                cobro = it
                            }
                        },
                        label = { Text("Cobro") },
                        prefix = { Text("$") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    // Mensaje de error/éxito
                    if (message.isNotEmpty()) {
                        Text(
                            message,
                            color = if (message.contains("Error") || message.contains("Completa"))
                                Color.Red
                            else
                                Color.Green,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // BOTÓN GUARDAR
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color(37, 99, 135)),
                        onClick = {
                            // VALIDACIONES
                            if (especializacionesAgregadas.isEmpty()) {
                                message = "Debes agregar al menos una materia"
                                return@Button
                            }

                            if (modalidadSeleccionada.isEmpty() || cobro.isEmpty()) {
                                message = "Completa modalidad y cobro"
                                return@Button
                            }

                            val userId = auth.currentUser?.uid
                            if (userId == null) {
                                message = "Error: Usuario no autenticado"
                                return@Button
                            }

                            // Obtener el ID del instructor desde la colección instructores
                            db.collection("instructores")
                                .whereEqualTo("idUsuario", userId)
                                .get()
                                .addOnSuccessListener { instructoresSnapshot ->
                                    if (instructoresSnapshot.documents.isNotEmpty()) {
                                        val instructorDoc = instructoresSnapshot.documents[0]
                                        val instructorId = instructorDoc.id // ← Usar document.id en lugar de getString("id")

                                        // 1️⃣ ACTUALIZAR instructores con modalidad y cobro
                                        // ⭐ CAMBIO AQUÍ: Usar set() con SetOptions.merge() en lugar de update()
                                        val instructorUpdate = hashMapOf<String, Any>(
                                            "modalidad" to modalidadSeleccionada,
                                            "cobro" to (cobro.toIntOrNull() ?: 0)
                                        )

                                        db.collection("instructores")
                                            .document(instructorId)
                                            .set(instructorUpdate, SetOptions.merge()) // ← CORRECCIÓN PRINCIPAL
                                            .addOnSuccessListener {
                                                // 2️⃣ CREAR documentos en instructorTema para cada especialización
                                                var errores = 0
                                                var exitosos = 0

                                                especializacionesAgregadas.forEach { (_, idTema, _) ->
                                                    val instructorTemaId = db.collection("instructorTema").document().id

                                                    val instructorTemaData = hashMapOf<String, Any>(
                                                        "idInstructor" to instructorId,
                                                        "idTema" to idTema
                                                    )

                                                    db.collection("instructorTema")
                                                        .document(instructorTemaId)
                                                        .set(instructorTemaData)
                                                        .addOnSuccessListener {
                                                            exitosos++
                                                            if (exitosos + errores == especializacionesAgregadas.size) {
                                                                if (errores == 0) {
                                                                    message = "Todas las especializaciones registradas correctamente"
                                                                    navController.navigate("pantalla_inicio") {
                                                                        popUpTo("pantalla_inicio") { inclusive = true }
                                                                    }
                                                                } else {
                                                                    message = "Algunas especializaciones no se pudieron guardar"
                                                                }
                                                            }
                                                        }
                                                        .addOnFailureListener { exception ->
                                                            errores++
                                                            message = "Error: ${exception.localizedMessage}"
                                                            if (exitosos + errores == especializacionesAgregadas.size) {
                                                                message = "Error al guardar algunas especializaciones"
                                                            }
                                                        }
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                message = "Error al actualizar instructor: ${exception.localizedMessage}"
                                            }
                                    } else {
                                        message = "Error: Instructor no encontrado"
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    message = "Error al buscar instructor: ${exception.localizedMessage}"
                                }
                        }
                    ) {
                        Text("Guardar y Finalizar")
                    }

                    Spacer(Modifier.height(8.dp))

                    // BOTÓN REGRESAR
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color(37, 99, 135)),
                        onClick = {
                            navController.navigate("pantalla_registro_tutor")
                        }
                    ) {
                        Text("Regresar")
                    }
                }
            }
        }
    }
}