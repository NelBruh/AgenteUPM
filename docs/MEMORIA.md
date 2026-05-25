# Memoria técnica — Sistema multiagente de moderación de chat

## 1. Introducción y objetivos

El objetivo del sistema es **moderar automáticamente** un flujo de mensajes de chat en directo, bloqueando contenido clasificado como tóxico (insultos, spam, enlaces fraudulentos) y mostrando al usuario final únicamente mensajes aptos o avisos de bloqueo.

Se ha elegido un dominio cercano a plataformas de streaming y redes sociales, alineado con los ejemplos del enunciado (análisis/clasificación de texto + visualización). El sistema incluye además un **bot generativo** basado en LLM local (Ollama) que simula espectadores reales, y un **agente de comandos** que responde a órdenes de chat tipo `!discord`.

## 2. Diseño multiagente

### 2.1 Agentes

| Agente | Rol | Fuente / salida | Servicio en DF |
|--------|-----|-----------------|----------------|
| `AgentePerceptor` | Adquisición (fichero) | Lee `chat.txt` línea a línea cada 4 s | `perceptor-chat-fichero` |
| `AgentePerceptorTeclado` | Adquisición (usuario) | Ventana Swing + teclado | `perceptor-chat-teclado` |
| `AgentePerceptorBot` | Adquisición (LLM generativo) | Consulta a Ollama cada 15 s | `perceptor-generativo` |
| `AgenteBinder` | Multiplexador / enrutador | Recibe de los tres perceptores y redirige | `multiplexador-chat-lineas` |
| `AgenteInteligente` | Procesamiento inteligente | Weka J48 sobre texto | `moderador-toxicidad` |
| `AgenteComando` | Respuesta a comandos de chat | Mapa estático de comandos → respuestas | `ejecutador-comandos-chat-` |
| `AgenteVisualizador` | Interfaz de usuario | Ventana Swing del chat moderado | `visualizador-chat` |

Se combinan **tres fuentes de percepción**: texto en fichero, interacción directa con el usuario y generación automática mediante LLM local.

### 2.2 Comportamientos JADE

- **Perceptor fichero:** `ComportamientoAdquisicionChat` (`TickerBehaviour`, 4 s) + `ComportamientoRecepcionInforme`.
- **Perceptor teclado:** `ComportamientoEntradaTeclado` (`OneShotBehaviour`, abre GUI) + `ComportamientoRecepcionInforme`.
- **Perceptor bot:** `TickerBehaviour` (15 s) — construye prompt, llama a Ollama vía `java.net.http.HttpClient`, extrae el texto de la respuesta JSON y lo envía al Binder.
- **Binder:** `ComportamientoBinder` (`CyclicBehaviour`) — `blockingReceive` con filtro `REQUEST + CONV_BINDER`; detecta si el contenido empieza por `!` y enruta al agente de comandos o al moderador.
- **Moderador:** `OneShotBehaviour` (entrenamiento Weka) + `ComportamientoModeracion` (`blockingReceive`).
- **Comandos:** `ComportamientoComandos` (`CyclicBehaviour`) — `blockingReceive` con filtro `REQUEST + CONV_COMANDOS`; consulta mapa de respuestas y envía dos `INFORM` al visualizador (línea original + respuesta del bot).
- **Visualizador:** `ComportamientoVisualizacion` (`blockingReceive`, actualización en EDT).

### 2.3 Directory Facilitator (DF)

Cada agente **registra** su servicio al iniciar (`DfHelper.registrar`) y se **da de baja** en `takeDown()` (`DfHelper.darDeBaja`).

Los agentes que consumen servicios **consultan** el DF antes de cada envío:

- Los tres perceptores buscan `multiplexador-chat-lineas` (Binder).
- El Binder busca `moderador-toxicidad` o `ejecutador-comandos-chat-` según el tipo de mensaje.
- El moderador busca `visualizador-chat` tras cada clasificación.
- El agente de comandos busca `visualizador-chat` para enviar la respuesta.

### 2.4 Comunicación ACL

| Origen | Destino | Performative | `conversation-id` | Contenido |
|--------|---------|--------------|-------------------|-----------|
| Perceptor (cualquiera) | Binder | `REQUEST` | `comunicacion-binder` | `Usuario: mensaje` |
| Binder | Moderador | `REQUEST` | `moderacion-chat` | `Usuario: mensaje` |
| Binder | AgenteComando | `REQUEST` | `comandos-chat` | `Usuario: !comando` |
| Moderador | Visualizador | `INFORM` | `visualizacion-chat` | Línea aprobada o aviso de bloqueo |
| AgenteComando | Visualizador | `INFORM` | `visualizacion-chat` | Línea original + respuesta del bot |

Las constantes de `conversation-id` se centralizan en `util/Enviar.java` (`CONV_BINDER`, `CONV_MODERADOR`, `CONV_COMANDOS`, `CONV_VISUALIZADOR`).

### 2.5 Filtros de mensajes en modo bloqueante

1. **Binder:** `MessageTemplate` con `REQUEST + CONV_BINDER`, `blockingReceive`.
2. **Moderador:** `MessageTemplate` con `REQUEST + CONV_MODERADOR`, `blockingReceive`.
3. **AgenteComando:** `MessageTemplate` con `REQUEST + CONV_COMANDOS`, `blockingReceive`.
4. **Visualizador:** `MessageTemplate` con `INFORM + CONV_VISUALIZADOR`, `blockingReceive`.

## 3. Agente generativo (AgentePerceptorBot)

El bot usa la API REST local de **Ollama** (`http://localhost:11434/api/generate`) para generar comentarios cortos que simulan espectadores de Twitch. La petición HTTP se hace con el cliente nativo de Java 11 (`java.net.http.HttpClient`) sin dependencias externas adicionales.

- **Modelo configurable:** constante `MODELO` en la clase (por defecto `llama3`; válido con `mistral`, `phi3`, etc.).
- **Prompt de sistema:** instruye al LLM para que genere comentarios informales de máximo 10 palabras, alternando entre amables y tóxicos.
- **Tolerancia a fallos:** si Ollama no está disponible, el agente registra el error en consola y continúa sin enviar mensajes; el resto del sistema no se ve afectado.

## 4. Agente de comandos (AgenteComando)

Responde a órdenes predefinidas del chat sin necesidad de clasificación Weka:

| Comando | Respuesta |
|---------|-----------|
| `!discord` | Enlace de invitación al servidor Discord |
| `!redessociales` | Perfiles de Twitter, Instagram y YouTube |
| `!horario` | Días y hora de los streams en directo |
| `!ayuda` | Lista de comandos disponibles |

Cualquier comando no reconocido devuelve un mensaje de error con sugerencia de `!ayuda`.

## 5. Inteligencia artificial (Weka)

### 5.1 Pipeline

1. Carga del dataset ARFF (`texto`, `clase ∈ {Limpio, Toxico}`).
2. Filtro `StringToWordVector` (bolsa de palabras).
3. Clasificador `J48` (árbol de decisión) dentro de `FilteredClassifier`.
4. Para cada mensaje entrante: limpieza léxica, instancia `DenseInstance`, `classifyInstance` + `distributionForInstance` (confianza).

### 5.2 Limitaciones conocidas

- Dataset ampliado a ~55 instancias alineadas con `chat.txt`; sigue siendo académico — en producción haría falta validación cruzada y más datos reales.
- El modelo depende del vocabulario del entrenamiento; mensajes muy diferentes pueden clasificarse de forma subóptima.

## 6. Tecnologías

- **JADE 4.6** — contenedor, DF, ACL.
- **Weka 3.8.6** — aprendizaje supervisado.
- **Ollama** (LLM local) — generación de texto para el perceptor bot.
- **Java 17**, **Maven**, **Swing**, `java.net.http` (Java 11+).

## 7. Pruebas realizadas

1. Arranque con `mvn exec:java` desde la raíz del proyecto.
2. Verificación en consola JADE de registro de los siete agentes en el DF.
3. Mensajes automáticos de `chat.txt` clasificados como limpios o tóxicos en la GUI del visualizador.
4. Envío manual desde el perceptor teclado: mensajes limpios pasan, tóxicos se bloquean.
5. Comandos `!discord`, `!horario`, etc. enviados desde teclado: respuesta del bot aparece en el visualizador sin pasar por Weka.
6. Bot generativo con Ollama activo: comentarios generados por el LLM clasificados automáticamente por el moderador.
7. Bot generativo con Ollama **inactivo**: el sistema sigue funcionando con los otros dos perceptores sin errores.

## 8. Referencias

- Bellifemine, Caire, Greenwood — *Developing Multi-Agent Systems with JADE*.
- Witten, Frank, Hall — *Data Mining: Practical Machine Learning Tools and Techniques* (Weka).
- Enunciado de práctica Sistemas Inteligentes UPM 2025-26.
