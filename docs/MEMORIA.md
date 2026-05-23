# Memoria técnica — Sistema multiagente de moderación de chat

## 1. Introducción y objetivos

El objetivo del sistema es **moderar automáticamente** un flujo de mensajes de chat en directo, bloqueando contenido clasificado como tóxico (insultos, spam, enlaces fraudulentos) y mostrando al usuario final únicamente mensajes aptos o avisos de bloqueo.

Se ha elegido un dominio cercano a plataformas de streaming y redes sociales, alineado con los ejemplos del enunciado (análisis/clasificación de texto + visualización).

## 2. Diseño multiagente

### 2.1 Agentes

| Agente | Rol | Fuente / salida | Servicio en DF |
|--------|-----|-----------------|----------------|
| `AgentePerceptor` | Adquisición (fichero) | Lee `chat.txt` | `perceptor-chat-fichero` |
| `AgentePerceptorTeclado` | Adquisición (usuario) | Ventana Swing + teclado | `perceptor-chat-teclado` |
| `AgenteInteligente` | Procesamiento inteligente | Weka J48 sobre texto | `moderador-toxicidad` |
| `AgenteVisualizador` | Interfaz de usuario | Ventana Swing del chat | `visualizador-chat` |

Se combinan **dos fuentes de percepción** exigidas por el enunciado (texto en fichero + interacción con el usuario).

### 2.2 Comportamientos JADE

- **Perceptor fichero:** `ComportamientoAdquisicionChat` (`TickerBehaviour`, 4 s) + `ComportamientoRecepcionInforme`.
- **Perceptor teclado:** `ComportamientoEntradaTeclado` (`OneShotBehaviour`, abre GUI) + `ComportamientoRecepcionInforme`.
- **Moderador:** `OneShotBehaviour` (entrenamiento Weka) + `ComportamientoModeracion` (`blockingReceive`).
- **Visualizador:** `ComportamientoVisualizacion` (`blockingReceive`, actualización en EDT).

### 2.3 Directory Facilitator (DF)

Cada agente **registra** su servicio al iniciar y se **da de baja** en `takeDown()`.

Los agentes que consumen servicios **consultan** el DF:

- Ambos perceptores buscan `moderador-toxicidad` antes de cada envío (`EnvioModerador`).
- El moderador busca `visualizador-chat` tras cada clasificación.

### 2.4 Comunicación ACL

| Origen | Destino | Performative | Contenido |
|--------|---------|--------------|-----------|
| Perceptor (fichero o teclado) | Moderador | `REQUEST` | Línea del chat (`Usuario: mensaje`) |
| Moderador | Perceptor origen | `INFORM` | Clase Weka y confianza |
| Moderador | Visualizador | `INFORM` | Línea aprobada o mensaje de bloqueo |

`conversation-id`: `moderacion-chat` (coherencia del diálogo FIPA).

### 2.5 Filtros de mensajes en modo bloqueante

1. **Moderador:** `MessageTemplate` con `REQUEST` + `conversation-id`, `blockingReceive`.
2. **Visualizador:** `MessageTemplate` con `INFORM` + `conversation-id`, `blockingReceive`.

## 3. Inteligencia artificial (Weka)

### 3.1 Pipeline

1. Carga del dataset ARFF (`texto`, `clase ∈ {Limpio, Toxico}`).
2. Filtro `StringToWordVector` (bolsa de palabras).
3. Clasificador `J48` (árbol de decisión) dentro de `FilteredClassifier`.
4. Para cada mensaje entrante: limpieza léxica, instancia `DenseInstance`, `classifyInstance` + `distributionForInstance` (confianza).

### 3.2 Limitaciones conocidas

- Dataset ampliado a ~55 instancias alineadas con `chat.txt`; sigue siendo académico — en producción haría falta validación cruzada y más datos reales.
- El modelo depende del vocabulario del entrenamiento; mensajes muy diferentes pueden clasificarse de forma subóptima.

## 4. Tecnologías

- **JADE 4.6** — contenedor, DF, ACL.
- **Weka 3.8.6** — aprendizaje supervisado.
- **Java 17**, **Maven**, **Swing**.

## 5. Pruebas realizadas

1. Arranque con `mvn exec:java` desde la raíz del proyecto.
2. Verificación en consola JADE de registro de los tres agentes.
3. Observación en la GUI de mensajes limpios pasando y tóxicos bloqueados (p. ej. líneas con “idiota”, “basura”, URLs de spam).
4. Comprobación de respuestas `INFORM` del moderador al perceptor en la consola del perceptor.

## 6. Trabajo futuro

- Tercer perceptor (API web / red social).
- Reentrenamiento online del modelo.
- Persistencia de estadísticas de moderación.

## 7. Referencias

- Bellifemine, Caire, Greenwood — *Developing Multi-Agent Systems with JADE*.
- Witten, Frank, Hall — *Data Mining: Practical Machine Learning Tools and Techniques* (Weka).
- Enunciado de práctica Sistemas Inteligentes UPM 2025-26.
