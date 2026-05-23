# Módulo 2 -- Running App [WIP] 

Aplicación Android nativa diseñada para el seguimiento y registro de actividades de running, integrada con servicios de Firebase y capacidades de hardware del dispositivo. Este proyecto corresponde al Trabajo Integrador del Módulo 2 de la materia Aplicaciones Móviles.

---

## Integrantes y Grupo
* **Integrantes:**
  * Mariano Alejo Carrizo - mariano.carrizo4280@gmail.com
  * Claudio D' Amico - mail

---

## Descripción del Proyecto
La aplicación está orientada a corredores que desean mantener un historial detallado de sus sesiones de entrenamiento. Permite registrar rutas mediante geolocalización, medir métricas clave de rendimiento y asociar elementos multimedia o datos de sensores específicos para enriquecer la experiencia del atleta. Cada usuario accede a un entorno personalizado y seguro donde sus datos están completamente aislados del resto.

---

## Requerimientos Funcionales Implementados

### RF1 -- Autenticación
* **Descripción:** Acceso seguro mediante **Firebase Authentication**.
* **Implementación:** Flujo completo de registro e inicio de sesión utilizando Email/Contraseña (o Google Sign-In, según hayan elegido). Protege las vistas principales evitando el acceso de usuarios no autenticados.

### RF2 -- Listado de Datos
* **Descripción:** Pantalla principal con el historial de carreras del usuario.
* **Implementación:** Muestra una lista resumida de los entrenamientos (fecha, distancia, tiempo). Mediante consultas estructuradas, el listado se filtra para mostrar **únicamente los datos del usuario actualmente autenticado**.

### RF3 -- Detalle
* **Descripción:** Vista extendida de cada actividad.
* **Implementación:** Al seleccionar una carrera del listado, se navega a una pantalla de detalle que expone la información completa: mapa con la ruta/punto de inicio, notas adicionales y recursos multimedia vinculados.

### RF4 -- Creación y Edición
* **Descripción:** Gestión de entrenamientos en tiempo real.
* **Implementación:** Formularios dedicados para añadir nuevas carreras o modificar registros existentes. Los datos se persisten de forma remota en **Cloud Firestore** bajo el ID único del usuario (`uid`).

### RF5 -- Geolocalización
* **Descripción:** Integración con mapas y ubicación en tiempo real.
* **Implementación:** Uso de la API de ubicación de Google Play Services para obtener las coordenadas actuales del corredor. Se visualizan en un mapa interactivo para registrar el punto de partida o el recorrido de la actividad.

### RF6 -- Sensor o Cámara
**Cámara / Galería:** Permite capturar o seleccionar una fotografía al finalizar la carrera (por ejemplo, del paisaje o del calzado usado) guardándola de forma eficiente.
**Acelerómetro:** Detecta el movimiento o ritmo de zancada durante la actividad para aportar valor al caso de uso del running.

### RF7 -- Notificaciones
* **Descripción:** Avisos e interacciones con el usuario en contextos clave.
* **Implementación:** Emisión de notificaciones (locales o push mediante FCM) al cumplir objetivos de distancia, recordar entrenamientos o confirmar la sincronización exitosa de los datos.

---

## Requerimientos Opcionales Incorporados
* **Caché Offline con Room:** Soporte completo sin conexión. Los entrenamientos se guardan localmente en la base de datos Room y se sincronizan automáticamente al recuperar la conectividad.
* **Notificaciones Push (FCM):** Integración con Firebase Cloud Messaging para la recepción de alertas remotas.
* **Retrofit + API Externa:** Conexión con una API de clima (ej. OpenWeather) para mostrar las condiciones meteorológicas actuales antes de iniciar la carrera.

---

## Arquitectura y Consideraciones Técnicas
* **Interfaz de Usuario:** Implementada de forma consistente utilizando `[Jetpack Compose / Views tradicionales en XML]`.
* **Seguridad:** Configuración estricta de reglas en Firestore para garantizar que los usuarios solo puedan leer y escribir sus propios documentos.

---

