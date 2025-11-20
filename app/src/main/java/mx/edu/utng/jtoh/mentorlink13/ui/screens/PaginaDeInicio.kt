package mx.edu.utng.jtoh.mentorlink13.ui.screens

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaginaDeInicio(navController: NavController) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(180, 180, 180) //230, 230, 235
    ) {
        //Columna para mantener los componentes centralizados
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(38.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            //Realiza un pequeño apartado para los botones de la pantalla
            Card {
                //Mantiene los botones centrados
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color(37, 99, 135)),
                        onClick = {
                            // Aquí navegas a registro de tutor si quieres
                            navController.navigate("pantalla_inicio")
                        }
                    ) {
                        Text("Ingresaste")
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color(37, 99, 135)),
                        onClick = {
                            navController.navigate("pantalla_inicio")
                        }
                    ) {
                        Text("Regresa")
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}