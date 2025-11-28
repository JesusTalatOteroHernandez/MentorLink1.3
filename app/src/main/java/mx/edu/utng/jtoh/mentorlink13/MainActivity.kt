package mx.edu.utng.jtoh.mentorlink13

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.edu.utng.jtoh.mentorlink13.ui.screens.usuario.Registro
import mx.edu.utng.jtoh.mentorlink13.ui.screens.usuario.pantallaDeInicio
import mx.edu.utng.jtoh.mentorlink13.ui.theme.MentorLink13Theme
import androidx.navigation.compose.rememberNavController

import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp
import mx.edu.utng.jtoh.mentorlink13.ui.screens.aprendiz.EditarPerfilAprendiz
import mx.edu.utng.jtoh.mentorlink13.ui.screens.usuario.EspecialidadTutor
import mx.edu.utng.jtoh.mentorlink13.ui.screens.aprendiz.PaginaCategoria
import mx.edu.utng.jtoh.mentorlink13.ui.screens.instructor.PaginaInstructor
import mx.edu.utng.jtoh.mentorlink13.ui.screens.aprendiz.PaginaNotificacionesAprendiz
import mx.edu.utng.jtoh.mentorlink13.ui.screens.instructor.PaginaNotificacionesInstructor
import mx.edu.utng.jtoh.mentorlink13.ui.screens.aprendiz.PaginaPrincipalAprendiz
import mx.edu.utng.jtoh.mentorlink13.ui.screens.instructor.PantallaCalificarAprendiz
import mx.edu.utng.jtoh.mentorlink13.ui.screens.aprendiz.PantallaCalificarInstructor
import mx.edu.utng.jtoh.mentorlink13.ui.screens.aprendiz.PerfilAprendiz
import mx.edu.utng.jtoh.mentorlink13.ui.screens.instructor.PerfilAprendizVer
import mx.edu.utng.jtoh.mentorlink13.ui.screens.aprendiz.PerfilInstructor
import mx.edu.utng.jtoh.mentorlink13.ui.screens.instructor.PerfilInstructorEditable
import mx.edu.utng.jtoh.mentorlink13.ui.screens.aprendiz.SolicitarAsesoria
import mx.edu.utng.jtoh.mentorlink13.ui.screens.usuario.registroAprendiz
import mx.edu.utng.jtoh.mentorlink13.ui.screens.usuario.registroTutor
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
                    startDestination = "spalsh"
                ) {
                    composable("spalsh"){
                        SplashScreenAvanzado(navController = navController)
                    }
                    composable("pantalla_inicio") {
                        pantallaDeInicio(navController = navController)
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

                    /*composable("calificar_aprendiz/{idAprendiz/{idAsesoria}") { backStackEntry ->
                        val idAprendiz = backStackEntry.arguments?.getString("idAprendiz") ?: ""
                        val idAsesoria = backStackEntry.arguments?.getString("idAsesoria") ?: ""
                        PantallaCalificarAprendiz(navController,idAprendiz, idAsesoria)
                    }*/

                    composable("calificar_aprendiz/{idAprendiz}/{idAsesoria}"){ backStackEntry ->
                        val idAprendiz = backStackEntry.arguments?.getString("idAprendiz") ?: ""
                        val idAsesoria = backStackEntry.arguments?.getString("idAsesoria") ?: ""
                        PantallaCalificarAprendiz(navController, idAprendiz, idAsesoria)
                    }

                    composable("calificar_instructor/{idInstructor}/{id}") { backStackEntry ->
                        val idInstructor = backStackEntry.arguments?.getString("idInstructor") ?: ""
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        PantallaCalificarInstructor(navController, idInstructor,id)
                    }

                    composable("pantalla_editable_de_instructor/{idInstructor}") { backStackEntry ->
                        val idInstructor = backStackEntry.arguments?.getString("idInstructor") ?: ""
                        PerfilInstructorEditable(navController)
                    }

                    composable("perfil_aprendiz/{idUsuario}") { backStackEntry ->
                        val idUsuario = backStackEntry.arguments?.getString("idUsuario") ?: ""
                        PerfilAprendiz(idUsuario, navController)
                    }

                    composable("editarPerfilAprendiz/{idUsuario}") { backStackEntry ->
                        val idUsuario = backStackEntry.arguments?.getString("idUsuario") ?: ""
                        EditarPerfilAprendiz(idUsuario, navController)
                    }

                    composable("perfil_aprendiz_ver/{idUsuario}") { backStackEntry ->
                        val idUsuario = backStackEntry.arguments?.getString("idUsuario") ?: ""
                        PerfilAprendizVer(idUsuario, navController)
                    }

                }
            }
        }
    }
}
