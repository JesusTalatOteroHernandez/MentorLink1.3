## Descripción del Proyecto
MentorLink es una aplicación móvil innovadora desarrollada en Kotlin con Jetpack Compose que conecta estudiantes con mentores experimentados para facilitar el aprendizaje colaborativo y el desarrollo profesional. La aplicación fue creada como proyecto final de la asignatura Desarrollo de Aplicaciones Móviles en la Universidad Tecnológica del Norte de Guanajuato (UTNG).
La plataforma permite a los usuarios buscar mentores especializados en diferentes áreas de conocimiento, agendar sesiones de mentoría, gestionar su perfil académico y profesional, y mantener un seguimiento del progreso en su aprendizaje. MentorLink ofrece una experiencia intuitiva y moderna siguiendo las mejores prácticas de desarrollo Android con arquitectura MVVM y Material Design 3.
Desarrollado por:

Jesús Talat Otero Hernández
<br>
Susana Cardenas Vazquez

Periodo: Septiembre - Diciembre 2025

## Características Principales
<li>Búsqueda de Mentores: Encuentra mentores por área de especialización, disponibilidad y calificación</li>
<li>Gestión de Perfil: Crea y edita tu perfil como mentor o estudiante con información detallada</li>
<li>Agendamiento de Sesiones: Programa sesiones de mentoría</li>
<li>Evaluación y Reseñas: Califica las sesiones y deja comentarios constructivos</li>
<li>Dashboard Personalizado: Visualiza tu progreso, próximas sesiones y notificaciones</li>
<li>Autenticación Segura: Login y registro con Firebase Authentication</li>
<li>Interfaz Moderna: Diseño intuitivo basado en Material Design 3</li>
<li>Navegación Fluida: Implementación con Jetpack Navigation Component</li>

### Capturas de pantalla
[Capturas de pantalla](https://github.com/JesusTalatOteroHernandez/MentorLink1.3/blob/780eae55fc05b7e1d9a0a6908ee4e542cd5c4df6/screenshots/ScreenShot1.jpeg)

## Tecnologías Utilizadas
### Lenguaje y Framework

<li>Kotlin 2.0.21 - Lenguaje principal de desarrollo</li>
<li>Jetpack Compose 1.12.0 - UI moderna y declarativa</li>
<li>Material Design 3 - Sistema de diseño de Google</li>

### Arquitectura y Patrones

<li>Clean Architecture - Organización en capas</li>
<li>Repository Pattern - Abstracción de fuentes de datos</li>
<li>Dependency Injection - Hilt para inyección de dependencias</li>

### Librerías Principales
dependencies {

    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.appcompat:appcompat-resources:1.6.1")

    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.core:core-splashscreen:1.0.1")
}

### Backend y Base de Datos

<li>Firebase Authentication - Gestión de usuarios</li>
<li>Firebase Firestore - Base de datos NoSQL en tiempo real</li>

### Herramientas de Desarrollo

<li>Android Studio Narval | 2025.1.2</li>
<li>Gradle 8.12.3</li>
<li>Git/GitHub - Control de versiones</li>
<li>Figma - Diseño de interfaces (prototipado)</li>
