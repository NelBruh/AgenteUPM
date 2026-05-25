# Sistema multiagente de moderación de chat (JADE + Weka)

**Asignatura:** Sistemas Inteligentes — ETSI Informáticos, UPM  
**Curso:** 2025-2026  
**Plataforma:** [JADE](https://jade.tilab.com/) 4.6 + [Weka](https://www.cs.waikato.ac.nz/ml/weka/) 3.8

Sistema multiagente que simula un chat en directo: **tres agentes de percepción** (fichero, teclado y bot generativo con Ollama) adquieren mensajes, un agente **binder** los enruta, un agente **inteligente** los clasifica como limpios o tóxicos con Weka, un agente **de comandos** responde a comandos de chat (`!discord`, `!horario`…), y un agente **visualiza** el resultado en Swing. Todos se coordinan mediante el **Directory Facilitator (DF)** y mensajes **ACL**.

---

## Miembros del grupo

| Nombre completo | NIA | Correo UPM |
|-----------------|-----|------------|
| Jiaxu He | X8587956D | jiaxu.he@alumnos.upm.es |
| Isaak Rojas Castaño |  55385263K | isaak.rojas@alumnos.upm.es |
| Miguel Martín Trilla | 05950903K | miguel.martin.trilla@alumnos.upm.es |
| Gufeng Wu | X8464449N | gufeng.wu@alumnos.upm.es |
| Miguel Díaz Martín | 11874375G | miguel.diaz.martin@alumnos.upm.es |

Ficha completa: [`docs/GRUPO.md`](docs/GRUPO.md)

---

## Requisitos cumplidos (enunciado de práctica)

| Requisito | Implementación |
|-----------|----------------|
| Agente de percepción / adquisición externa | `AgentePerceptor` (fichero) + `AgentePerceptorTeclado` (usuario) + `AgentePerceptorBot` (LLM Ollama) — **tres fuentes combinadas** |
| Agente multiplexador / enrutador | `AgenteBinder` — distingue mensajes normales de comandos `!` y los redirige al moderador o al agente de comandos |
| Agente con procesamiento inteligente | `AgenteInteligente` — Weka J48 + `StringToWordVector` |
| Agente de comandos de chat | `AgenteComando` — responde a `!discord`, `!horario`, `!redessociales`, `!ayuda` con respuestas predefinidas |
| Agente con interfaz de usuario | `AgenteVisualizador` — Swing |
| Comportamientos JADE en cada agente | `ComportamientoAdquisicionChat`, `ComportamientoBinder`, `ComportamientoComandos`, `ComportamientoModeracion`, `ComportamientoVisualizacion` + `OneShotBehaviour` de entrenamiento |
| Mensajes ACL | `REQUEST` / `INFORM` entre agentes |
| Filtro bloqueante | `blockingReceive` + `MessageTemplate` en binder, moderador, agente de comandos y visualizador |
| Directory Facilitator | Registro y consulta en los **siete** agentes |

---

## Arquitectura

En [`docs/Diagrama_Arquitectura.pdf`](docs/Diagrama_Arquitectura.pdf)

**Flujo:** los tres perceptores envían mensajes al **Binder**: el de fichero cada 4 s, el de teclado cuando se pulsa *Enviar*, y el bot generativo cada 15 s (consulta a Ollama con el prompt de espectador de Twitch). El Binder examina cada línea: si empieza por `!` la redirige al **AgenteComando**, que devuelve la respuesta al visualizador; en caso contrario la redirige al **moderador**, que clasifica con Weka (dataset ampliado ~55 ejemplos) y el visualizador muestra el chat moderado.

### Prueba en vivo durante la defensa

1. Arrancar el sistema y esperar mensajes automáticos de `chat.txt`.
2. En la ventana **Entrada manual — Perceptor teclado**, escribir:
   - Limpio: `Profesor: Buen trabajo con el sistema multiagente`
   - Tóxico: `Troll: Eres un idiota esto es basura`
3. Abrir en el navegador http://localhost:8080/ para visualizar los mensajes


Documentación ampliada: [`docs/MEMORIA.md`](docs/MEMORIA.md).

---

## Instalación

### Requisitos previos

- **JDK 11+** ([Adoptium](https://adoptium.net/) o Oracle JDK)
- **Apache Maven 3.8+** — [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
- **Eclipse IDE** (opcional, recomendado en el enunciado) con plugin M2Eclipse

### Dependencias del proyecto

Las dependencias se gestionan con Maven (`pom.xml`):

| Dependencia | Versión | Uso |
|-------------|---------|-----|
| `jade/lib/jade.jar` (JADE local en el repo) | 4.6.0 | Plataforma multiagente — **no está en Maven Central** |
| `nz.ac.waikato.cms.weka:weka-stable` | 3.8.6 | Clasificador J48 (Maven) |

La carpeta **`jade/`** es la distribución Tilab (`jade/lib/jade.jar`). La carpeta **`lib/`** puede contener otros jars de Eclipse si los usáis.

**Captura para la memoria:** ejecutar en la raíz del proyecto:

```bash
mvn dependency:tree
```

Incluir una captura de pantalla del resultado en el documento de entrega del Aula Virtual.

### Clonar e importar

```bash
git clone https://github.com/NelBruh/AgenteUPM.git
cd AgenteUPM-master
mvn clean compile
```

**Eclipse:** `File → Import → Existing Maven Projects` y seleccionar la carpeta del repositorio.

---

## Ejecución

### Opción A — Maven (recomendada)

Desde la **raíz del proyecto** (donde están `chat.txt` y `chat_entrenamiento.arff`):

```bash
mvn clean compile exec:java
```

Se abrirá la consola gráfica de JADE y la ventana del chat. Los tres agentes arrancan automáticamente.

### Opción B — Eclipse

1. Importar como proyecto Maven.
2. Ejecutar la clase `es.upm.MainContainer` como *Java Application*.
3. Asegurarse de que el *working directory* del lanzador es la raíz del proyecto (`Run → Run Configurations → Arguments → Working directory: ${project_loc}`).

### Opción C — Agentes manuales en JADE

Si preferís cargar agentes uno a uno desde la GUI de JADE:

| Agente | Clase |
|--------|--------|
| perceptor | `es.upm.AgentePerceptor` |
| teclado | `es.upm.AgentePerceptorTeclado` |
| bot | `es.upm.AgentePerceptorBot` |
| binder | `es.upm.AgenteBinder` |
| moderador | `es.upm.AgenteInteligente` |
| comandos | `es.upm.AgenteComando` |
| visualizador | `es.upm.AgenteVisualizador` |

---

## Datos de ejemplo

| Fichero | Descripción |
|---------|-------------|
| `chat.txt` | Mensajes simulados de un chat en directo (limpios, tóxicos, spam) |
| `chat_entrenamiento.arff` | Dataset Weka ampliado (~55 instancias, `Limpio` / `Toxico`) |

Podéis editar `chat.txt` para nuevos casos automáticos. Para el teclado, usad el formato `Usuario: mensaje` (si omitís el usuario, se prefija `Manual:`). Tras cambiar el `.arff`, reiniciad el sistema para reentrenar.

---

## Estructura del repositorio

```
├── chat.txt                      # Entrada simulada del chat
├── chat_entrenamiento.arff       # Entrenamiento Weka
├── pom.xml
├── README.md
├── docs/
│   ├── GRUPO.md                  # Identificación del grupo
│   ├── MEMORIA.md                # Memoria técnica
│   └── DECLARACION_IA.md         # Uso de herramientas de IA
└── src/main/java/es/upm/
    ├── MainContainer.java        # Arranque del sistema
    ├── AgentePerceptor.java
    ├── AgentePerceptorTeclado.java
    ├── AgentePerceptorBot.java   # Perceptor generativo via Ollama (LLM local)
    ├── AgenteBinder.java         # Multiplexador: enruta mensajes o comandos
    ├── AgenteInteligente.java
    ├── AgenteComando.java        # Responde a comandos !discord, !horario, etc.
    ├── AgenteVisualizador.java
    ├── ServiciosMas.java
    ├── behaviours/               # Comportamientos JADE
    ├── model/
    └── util/
```

---

## Defensa oral — puntos clave

1. **Siete agentes:** tres perceptores (fichero + teclado + bot Ollama), binder, moderador Weka, agente de comandos, visualizador.
2. **DF:** cada agente registra un servicio; perceptores, binder, moderador y agente de comandos consultan servicios consumidos.
3. **ACL:** protocolo request-inform; `conversation-id` diferenciado por canal (`CONV_BINDER`, `CONV_MODERADOR`, `CONV_COMANDOS`, `CONV_VISUALIZADOR`).
4. **Filtros bloqueantes:** binder, moderador (REQUEST), agente de comandos (REQUEST) y visualizador (INFORM).
5. **Weka:** pipeline `StringToWordVector` + árbol **J48**; salida con clase y confianza.
6. **AgenteBinder:** detecta si el contenido comienza con `!` y enruta al agente de comandos; en caso contrario lo pasa al moderador.
7. **AgentePerceptorBot:** usa la API REST de Ollama (`/api/generate`) con un prompt de espectador de Twitch; genera un mensaje cada 15 s sin dependencias externas de JSON.
8. **AgenteComando:** mapa estático de comandos (`!discord`, `!horario`, `!redessociales`, `!ayuda`); envía al visualizador la línea del usuario y la respuesta del bot como dos INFORM consecutivos.

---

## Declaración de uso de IA

Ver [`docs/DECLARACION_IA.md`](docs/DECLARACION_IA.md).

---

## Licencia y contacto

Proyecto académico para la asignatura Sistemas Inteligentes. Dudas: daniel.garijo@upm.es
