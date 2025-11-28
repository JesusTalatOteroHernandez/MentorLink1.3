package mx.edu.utng.jtoh.mentorlink13.ui.screens.aprendiz

import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.style.TextAlign
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
//import com.android.identity.util.UUID
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarAsesoria(
    navController: NavController,
    idInstructor: String
) {

    // ---------------- ESTADOS ----------------
    val db = Firebase.firestore
    val auth = FirebaseAuth.getInstance()

    var nombreInstructor by remember { mutableStateOf("Cargando...") }

    var tema by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var modalidad by remember { mutableStateOf("Virtual") }   // Default

    var fecha by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }

    // Estado para mostrar el diálogo de éxito
    var mostrarDialogoExito by remember { mutableStateOf(false) }

    // Obtener el ID del usuario autenticado actual
    val idUsuarioActual = auth.currentUser?.uid ?: ""

    // -------- OBTENER NOMBRE DEL INSTRUCTOR --------
    LaunchedEffect("cargar_instructor") {
        Log.d("Asesoria", "=== INICIO DE CARGA ===")
        Log.d("Asesoria", "ID del instructor recibido: $idInstructor")

        db.collection("instructores").document(idInstructor).get()
            .addOnSuccessListener { doc ->
                Log.d("Asesoria", "¿Documento existe?: ${doc.exists()}")

                if (!doc.exists()) {
                    Log.e("Asesoria", "El documento del instructor NO existe")
                    nombreInstructor = "Instructor no encontrado"
                    isLoading = false
                    return@addOnSuccessListener
                }

                // Imprimir TODOS los campos del documento
                Log.d("Asesoria", "Todos los datos del instructor: ${doc.data}")

                val idUsuario = doc.getString("idUsuario")
                Log.d("Asesoria", "Campo 'idUsuario' = $idUsuario")

                if (idUsuario.isNullOrEmpty()) {
                    Log.e("Asesoria", "El campo 'idUsuario' está vacío o no existe")
                    Log.e("Asesoria", "Campos disponibles: ${doc.data?.keys}")
                    nombreInstructor = "ID de usuario no encontrado"
                    isLoading = false
                    return@addOnSuccessListener
                }

                Log.d("Asesoria", "Buscando usuario con ID: $idUsuario")

                db.collection("usuarios").document(idUsuario).get()
                    .addOnSuccessListener { u ->
                        Log.d("Asesoria", "¿Usuario existe?: ${u.exists()}")

                        if (!u.exists()) {
                            Log.e("Asesoria", "El usuario con ID $idUsuario NO existe")
                            nombreInstructor = "Usuario no encontrado"
                            isLoading = false
                            return@addOnSuccessListener
                        }

                        Log.d("Asesoria", "Datos del usuario: ${u.data}")

                        val nombre = u.getString("nombre") ?: "Sin nombre"
                        val apellidos = u.getString("apellidos") ?: ""

                        nombreInstructor = if (apellidos.isNotEmpty()) {
                            "$nombre $apellidos"
                        } else {
                            nombre
                        }

                        Log.d("Asesoria", "✅ Nombre completo obtenido: $nombreInstructor")
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        Log.e("Asesoria", "❌ Error al obtener usuario: ${e.message}")
                        Log.e("Asesoria", "Tipo de error: ${e.javaClass.simpleName}")
                        nombreInstructor = "Error: ${e.message}"
                        isLoading = false
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Asesoria", "❌ Error al obtener instructor: ${e.message}")
                Log.e("Asesoria", "Tipo de error: ${e.javaClass.simpleName}")
                nombreInstructor = "Error: ${e.message}"
                isLoading = false
            }
    }

    // ---------------- UI ----------------
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
                Text("Nueva Solicitud", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },

        bottomBar = {
            Button(
                onClick = {
                    // Validar que haya un usuario autenticado
                    if (idUsuarioActual.isEmpty()) {
                        Log.e("Asesoria", "No hay usuario autenticado")
                        // Aquí podrías mostrar un Toast o mensaje de error
                        return@Button
                    }

                    // Validar campos obligatorios
                    if (tema.isEmpty() || fecha.isEmpty() || horaInicio.isEmpty() || horaFin.isEmpty()) {
                        Log.e("Asesoria", "Campos incompletos")
                        // Aquí podrías mostrar un Toast o mensaje de error
                        return@Button
                    }

                    Log.d("Asesoria", "Guardando asesoría con idAprendiz: $idUsuarioActual")

                    // -------- GUARDAR EN FIRESTORE --------
                    val nuevaAsesoria = hashMapOf(
                        "tema" to tema,
                        "modalidad" to modalidad,
                        "adicional" to mensaje,
                        "fecha" to fecha,
                        "hora" to "$horaInicio - $horaFin",
                        "idInstructor" to idInstructor,
                        "idAprendiz" to idUsuarioActual,
                        "estado" to "Pendiente"
                    )

                    db.collection("asesorias")
                        .add(nuevaAsesoria)
                        .addOnSuccessListener { docRef ->
                            Log.d("Asesoria", "Asesoría guardada exitosamente: ${docRef.id}")
                            // Mostrar diálogo de éxito
                            mostrarDialogoExito = true
                        }
                        .addOnFailureListener { e ->
                            Log.e("Asesoria", "Error al guardar asesoría: ${e.message}")
                            // Mostrar mensaje de error
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(13, 176, 123)),
                enabled = !isLoading  // Deshabilitar mientras carga
            ) {
                Text("Enviar Solicitud", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // -------- CARD DE INSTRUCTOR --------
            item {
                Card(
                    elevation = CardDefaults.cardElevation(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(25.dp)
                ) {
                    Column(Modifier.padding(15.dp)) {
                        Text("Instructor:", fontSize = 12.sp, fontWeight = FontWeight.Light)
                        Row {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Usuario",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(Modifier.width(10.dp))

                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.CenterVertically)
                                )
                            } else {
                                Text(
                                    text = nombreInstructor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                }
            }

            // -------- CARD PRINCIPAL --------
            item {
                Card(
                    elevation = CardDefaults.cardElevation(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(25.dp)
                ) {
                    Column(Modifier.padding(25.dp)) {

                        // --- TEMA ---
                        Text("Tema de Interés *", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.height(10.dp))
                        TextField(
                            value = tema,
                            onValueChange = { tema = it },
                            placeholder = { Text("Ej: Python para desarrollo web") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(Modifier.height(20.dp))

                        // -------- MODALIDAD --------
                        Text("Modalidad Preferida", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.height(10.dp))

                        Row {
                            // -------- VIRTUAL --------
                            Card(
                                modifier = Modifier
                                    .size(145.dp, 50.dp)
                                    .clickable { modalidad = "Virtual" },
                                border = BorderStroke(
                                    width = 2.dp,
                                    color = if (modalidad == "Virtual") Color(37, 99, 235) else Color(229, 231, 235)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Virtual",
                                        fontWeight = FontWeight.Bold,
                                        color = if (modalidad == "Virtual") Color(30, 64, 175) else Color.Black
                                    )
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            // -------- PRESENCIAL --------
                            Card(
                                modifier = Modifier
                                    .size(145.dp, 50.dp)
                                    .clickable { modalidad = "Presencial" },
                                border = BorderStroke(
                                    width = 2.dp,
                                    color = if (modalidad == "Presencial") Color(37, 99, 235) else Color(229, 231, 235)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Presencial",
                                        fontWeight = FontWeight.Bold,
                                        color = if (modalidad == "Presencial") Color(30, 64, 175) else Color.Black
                                    )
                                }
                            }
                        }

                        // -------- FECHA Y HORAS --------
                        Spacer(Modifier.height(20.dp))
                        Text("Fecha *", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        TextField(
                            value = fecha,
                            onValueChange = { fecha = it },
                            placeholder = { Text("Ej: 12/12/2025") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(Modifier.height(20.dp))
                        Text("Hora inicio *", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        TextField(
                            value = horaInicio,
                            onValueChange = { horaInicio = it },
                            placeholder = { Text("Ej: 14:00") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(Modifier.height(20.dp))
                        Text("Hora fin *", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        TextField(
                            value = horaFin,
                            onValueChange = { horaFin = it },
                            placeholder = { Text("Ej: 15:00") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // -------- MENSAJE --------
                        Spacer(Modifier.height(20.dp))
                        Text("Mensaje Adicional (opcional)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        TextField(
                            value = mensaje,
                            onValueChange = { mensaje = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            placeholder = { Text("Cuéntale al instructor sobre tus objetivos...") },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }
    }

    // Diálogo de éxito
    if (mostrarDialogoExito) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoExito = false
                navController.popBackStack()
            },
            containerColor = Color.White,
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Botón X para cerrar en la esquina superior derecha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                mostrarDialogoExito = false
                                navController.popBackStack()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Icono de éxito
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Éxito",
                        tint = Color(13, 176, 123),
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Título
                    Text(
                        text = "¡Solicitud Enviada!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(13, 176, 123)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Mensaje
                    Text(
                        text = "Gracias por tu solicitud, pronto se te avisará si la asesoría fue aceptada y el instructor se pondrá en contacto contigo en la brevedad de lo posible.",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(8.dp))
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}