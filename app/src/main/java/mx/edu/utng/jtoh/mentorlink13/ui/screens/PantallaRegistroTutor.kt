package mx.edu.utng.jtoh.mentorlink13.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.text.contains


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun registroTutor(navController: NavController){
    var passwordNuevamente by remember { mutableStateOf("") }
    var nuevoPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val isValid = email.contains("@")
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(180, 180, 180) //230, 230, 235
    ) {
        //Mantiene todos los componentes adnetro centralizado
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            //Realiza un pequeño apartado para el titulo de la pantalla
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TUTOR")
                }
            }

            Spacer(Modifier.height(16.dp))

            //Apartado para que los apartados de texto esten centrados
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column {
                        Text(
                            "Ingresa tu correo electronico:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar el correo electronico
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Electronico") },
                            isError = !isValid,
                            modifier = Modifier.fillMaxWidth()
                        )
                        //Valida que cumpla con los requerimientos para un correo electronico
                        if (!isValid) {
                            Text(
                                "Correo Invalido", color = Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Ingresa una contraseña:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar contraseña
                        OutlinedTextField(
                            value = nuevoPassword,
                            onValueChange = { nuevoPassword = it },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()

                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Ingresa Nuevamente la contraseña:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar contraseña nuevamente
                        OutlinedTextField(
                            value = passwordNuevamente,
                            onValueChange = { passwordNuevamente = it },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Ingrese su nombre:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar nombre
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Ingrese su apellido:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar apellido
                        OutlinedTextField(
                            value = apellido,
                            onValueChange = { apellido = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Ingrese su edad:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(3.dp))
                        //Apartado para ingresar edad
                        OutlinedTextField(
                            value = edad,
                            onValueChange = { edad = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                    }

                    Spacer(Modifier.height(16.dp))
                    //Boton para continuar
                    Button(
                        modifier = Modifier,
                        colors = ButtonDefaults.buttonColors(Color(37, 99, 135)),
                        onClick = {

                        }

                    ) {
                        Text("Siguiente")
                    }

                    Spacer(Modifier.height(16.dp))
                    //Botom para regresar
                    Button(
                        modifier = Modifier,
                        colors = ButtonDefaults.buttonColors(Color(37, 99, 135)),
                        onClick = {
                            navController.navigate("pantalla_inicio")
                        }

                    ) {
                        Text("Regresar")
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
