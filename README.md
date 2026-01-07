# Examen_Moviles

📍 GPS Tracker - ESCOM IPN
Esta aplicación es un Rastreador GPS en segundo plano desarrollado como parte de la evaluación de Programación de Dispositivos Móviles en la ESCOM - IPN. La aplicación registra la ubicación del usuario de forma persistente utilizando servicios de primer plano (Foreground Services).

🚀 Características
🛰️ Rastreo en Tiempo Real: Obtención de coordenadas exactas mediante FusedLocationProviderClient.

🔄 Persistencia de Datos: Almacenamiento local de coordenadas.

🔔 Foreground Service: El rastreo continúa incluso si la aplicación está cerrada o el teléfono bloqueado.

⚙️ Intervalos Configurables: Ajuste de la frecuencia de actualización de GPS.

📱 Notificaciones Interactivas: Control del servicio directamente desde la barra de notificaciones.

🛠️ Tecnologías Utilizadas
Kotlin: Lenguaje principal de desarrollo.

Corrutinas y Flow: Para el manejo de operaciones asíncronas y flujo de datos.

Google Play Services Location: Para una detección de ubicación eficiente y precisa.

DataStore / Preferences: Para la gestión de configuraciones del usuario.

NotificationManager: Para el canal de comunicación constante con el usuario.

🏗️ Arquitectura
La aplicación sigue los principios de una arquitectura limpia, separando las responsabilidades en:

Service: LocationService gestiona el ciclo de vida del GPS.

Data: Repositorios y almacenamiento local (LocationStorage).

UI: Actividad principal para visualización y control.

📲 Instalación y Uso
Clonar el repositorio:

Bash

git clone https://github.com/gael-marquez/Examen_Moviles.git
Abrir en Android Studio: Importa el proyecto y espera a que Gradle sincronice las dependencias.

Permisos: Asegúrate de conceder permisos de Ubicación (Todo el tiempo) y Notificaciones al iniciar la app.

🧪 Pruebas y Evidencias
En esta sección se detallan las pruebas realizadas para validar el funcionamiento del rastreo y la persistencia.

Nota: Aquí puedes adjuntar capturas de pantalla de la app funcionando, logs de Logcat mostrando las coordenadas guardadas o videos del servicio en ejecución.

Prueba 1: Inicio del servicio y creación de notificación.

Prueba 2: Cambio de intervalo de actualización.

Prueba 3: Persistencia de datos tras cerrar la aplicación.

👤 Autor
Gael Márquez - gael-marquez

Institución: Escuela Superior de Cómputo (ESCOM - IPN)

Este proyecto fue realizado con fines académicos para la unidad de aprendizaje de Dispositivos Móviles.
