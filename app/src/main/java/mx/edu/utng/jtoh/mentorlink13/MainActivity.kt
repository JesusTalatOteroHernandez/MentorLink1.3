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
import mx.edu.utng.jtoh.mentorlink13.ui.screens.PaginaCategoria
import mx.edu.utng.jtoh.mentorlink13.ui.screens.PaginaInstructor
import mx.edu.utng.jtoh.mentorlink13.ui.screens.PaginaNotificacionesAprendiz
import mx.edu.utng.jtoh.mentorlink13.ui.screens.PaginaNotificacionesInstructor
import mx.edu.utng.jtoh.mentorlink13.ui.screens.PaginaPrincipalAprendiz
import mx.edu.utng.jtoh.mentorlink13.ui.screens.PantallaCalificarAprendiz
import mx.edu.utng.jtoh.mentorlink13.ui.screens.PantallaCalificarInstructor
import mx.edu.utng.jtoh.mentorlink13.ui.screens.PerfilInstructor
import mx.edu.utng.jtoh.mentorlink13.ui.screens.PerfilInstructorEditable
import mx.edu.utng.jtoh.mentorlink13.ui.screens.SolicitarAsesoria
import mx.edu.utng.jtoh.mentorlink13.ui.screens.registroAprendiz
import mx.edu.utng.jtoh.mentorlink13.ui.screens.registroTutor
import mx.edu.utng.jtoh.mentorlink13.ui.splash.SplashScreenAvanzado


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
                    composable("spalsh"){
                        SplashScreenAvanzado(navController)
                    }
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


                    composable("pantalla_de_inicio_aprendiz"){
                        PaginaPrincipalAprendiz(navController)
                    }
                    composable("pantalla_de_inicio_tutor"){
                        PaginaInstructor(navController)
                    }
                    composable("pantalla_epecialidad_tutor") {
                        EspecialidadTutor(navController)
                    }
                    composable("categoria/{nombreCategoria}") { backStack ->
                        val categoria = backStack.arguments?.getString("nombreCategoria") ?: ""
                        PaginaCategoria(navController, categoria)
                    }
                    composable("perfil_instructor/{idInstructor}"){ backStackEntry ->
                        val idInstructor = backStackEntry.arguments?.getString("idInstructor") ?: ""
                        PerfilInstructor(idInstructor, navController)
                    }
                    composable("solicitar_asesoria/{idInstructor}"){ backStackEntry ->
                        val idInstructor = backStackEntry.arguments?.getString("idInstructor") ?: ""
                        SolicitarAsesoria(navController, idInstructor)
                    }

                    composable("notificaciones_aprendiz"){
                        PaginaNotificacionesAprendiz(navController)
                    }

                    composable("notificaciones_instructor") {
                        PaginaNotificacionesInstructor(navController)
                    }

                    composable("calificar_aprendiz/{idAprendiz/{idAsesoria}") { backStackEntry ->
                        val idAprendiz = backStackEntry.arguments?.getString("idAprendiz") ?: ""
                        val idAsesoria = backStackEntry.arguments?.getString("idAsesoria") ?: ""
                        PantallaCalificarAprendiz(navController,idAprendiz, idAsesoria)
                    }

                    composable("calificar_instructor/{idInstructor}") { backStackEntry ->
                        val idInstructor = backStackEntry.arguments?.getString("idInstructor") ?: ""
                        PantallaCalificarInstructor(navController, idInstructor)
                    }

                    composable("pantalla_editable_de_instructor/{idInstructor}") { backStackEntry ->
                        val idInstructor = backStackEntry.arguments?.getString("idInstructor") ?: ""
                        PerfilInstructorEditable(navController)
                    }

                }
            }
        }
    }
}
