# 📍 GPS Tracker - ESCOM IPN

Esta aplicación es un **Rastreador GPS en segundo plano** desarrollado como parte de la evaluación de Programación de Dispositivos Móviles en la **ESCOM - IPN**. La aplicación registra la ubicación del usuario de forma persistente utilizando servicios de primer plano (*Foreground Services*).

## 🚀 Características

* 🛰️ **Rastreo en Tiempo Real:** Obtención de coordenadas exactas mediante `FusedLocationProviderClient`.
* 🔄 **Persistencia de Datos:** Almacenamiento local de coordenadas.
* 🔔 **Foreground Service:** El rastreo continúa incluso si la aplicación está cerrada o el teléfono bloqueado.
* ⚙️ **Intervalos Configurables:** Ajuste de la frecuencia de actualización de GPS.
* 📱 **Notificaciones Interactivas:** Control del servicio directamente desde la barra de notificaciones.

---

## 🛠️ Tecnologías Utilizadas

* **Kotlin:** Lenguaje principal de desarrollo.
* **Corrutinas y Flow:** Para el manejo de operaciones asíncronas y flujo de datos.
* **Google Play Services Location:** Para una detección de ubicación eficiente y precisa.
* **DataStore / Preferences:** Para la gestión de configuraciones del usuario.
* **NotificationManager:** Para el canal de comunicación constante con el usuario.

---

## 🏗️ Arquitectura

La aplicación sigue los principios de una arquitectura limpia, separando las responsabilidades en:
1. **Service:** `LocationService` gestiona el ciclo de vida del GPS.
2. **Data:** Repositorios y almacenamiento local (`LocationStorage`).
3. **UI:** Actividad principal para visualización y control.

---

## 📲 Instalación y Uso

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/gael-marquez/Examen_Moviles.git](https://github.com/gael-marquez/Examen_Moviles.git)
2. **Abrir en Android Studio: Importa el proyecto y espera a que Gradle sincronice las dependencias.**

3. **Permisos: Asegúrate de conceder permisos de Ubicación (Todo el tiempo) y Notificaciones al iniciar la app.**

##👤 Autor
Gael Márquez - gael-marquez

##Escuela Superior de Cómputo (ESCOM - IPN)
