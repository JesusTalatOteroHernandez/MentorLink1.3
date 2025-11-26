package mx.edu.utng.jtoh.mentorlink13

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.edu.utng.jtoh.mentorlink13.ui.screens.Registro
import mx.edu.utng.jtoh.mentorlink13.ui.screens.pantallaDeInicio
import mx.edu.utng.jtoh.mentorlink13.ui.theme.MentorLink13Theme
import androidx.navigation.compose.rememberNavController

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import mx.edu.utng.jtoh.mentorlink13.ui.screens.EspecialidadTutor
import mx.edu.utng.jtoh.mentorlink13.ui.screens.PaginaDeInicioAprendiz
import mx.edu.utng.jtoh.mentorlink13.ui.screens.PaginaDeInicioInstructor
import mx.edu.utng.jtoh.mentorlink13.ui.screens.registroAprendiz
import mx.edu.utng.jtoh.mentorlink13.ui.screens.registroTutor


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        setContent {
            MentorLink13Theme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "pantalla_inicio"
                ) {
                    composable("pantalla_inicio") {
                        pantallaDeInicio(navController)
                    }
                    composable("pantalla_registro") {
                        Registro(navController)
                    }
                    composable("pantalla_registro_aprendiz") {
                        registroAprendiz(navController)
                    }
                    composable("pantalla_registro_tutor") {
                        registroTutor(navController)
                    }
                    composable("pantalla_de_inicio_instructor") {
                        PaginaDeInicioInstructor(navController)
                    }
                    composable("pantalla_de_inicio_aprendiz"){
                        PaginaDeInicioAprendiz(navController)
                    }
                    composable("pantalla_epecialidad_tutor") {
                        EspecialidadTutor(navController)
                    }
                }
            }
        }
    }
}
