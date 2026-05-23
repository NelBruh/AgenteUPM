# Sistema multiagente de moderación de chat (JADE + Weka)

**Asignatura:** Sistemas Inteligentes — ETSI Informáticos, UPM  
**Curso:** 2025-2026  
**Plataforma:** [JADE](https://jade.tilab.com/) 4.6 + [Weka](https://www.cs.waikato.ac.nz/ml/weka/) 3.8

Sistema multiagente que simula un chat en directo: **dos agentes de percepción** (fichero + teclado) adquieren mensajes, un agente **inteligente** los clasifica como limpios o tóxicos con Weka, y un agente **visualiza** el resultado en Swing. Todos se coordinan mediante el **Directory Facilitator (DF)** y mensajes **ACL**.

---

## Miembros del grupo

Completar en [`docs/GRUPO.md`](docs/GRUPO.md) antes de la entrega en el Aula Virtual.

---

## Requisitos cumplidos (enunciado de práctica)

| Requisito | Implementación |
|-----------|----------------|
| Agente de percepción / adquisición externa | `AgentePerceptor` (fichero) + `AgentePerceptorTeclado` (usuario) — **dos fuentes combinadas** |
| Agente con procesamiento inteligente | `AgenteInteligente` — Weka J48 + `StringToWordVector` |
| Agente con interfaz de usuario | `AgenteVisualizador` — Swing |
| Comportamientos JADE en cada agente | `ComportamientoAdquisicionChat`, `ComportamientoModeracion`, `ComportamientoVisualizacion` + `OneShotBehaviour` de entrenamiento |
| Mensajes ACL | `REQUEST` / `INFORM` entre agentes |
| Filtro bloqueante | `blockingReceive` + `MessageTemplate` en moderador y visualizador |
| Directory Facilitator | Registro y consulta en los **cuatro** agentes |

---

## Arquitectura

En [`docs/Diagrama_Arquitectura.pdf`](docs/Diagrama_Arquitectura.pdf)

**Flujo:** el perceptor de fichero envía una línea cada 4 s; el de teclado envía cuando pulsáis *Enviar*. El moderador clasifica con Weka (dataset ampliado ~55 ejemplos) y el visualizador muestra el chat moderado.

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
git clone <URL-de-vuestro-repositorio>
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
| moderador | `es.upm.AgenteInteligente` |
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
    ├── AgenteInteligente.java
    ├── AgenteVisualizador.java
    ├── ServiciosMas.java
    ├── behaviours/               # Comportamientos JADE
    ├── model/
    └── util/
```

---

## Defensa oral — puntos clave

1. **Cuatro agentes:** dos perceptores (fichero + teclado), moderador Weka, visualizador.
2. **DF:** cada agente registra un servicio; perceptor y moderador consultan servicios consumidos.
3. **ACL:** protocolo request-inform; `conversation-id` común `moderacion-chat`.
4. **Filtros bloqueantes:** moderador (REQUEST) y visualizador (INFORM).
5. **Weka:** pipeline `StringToWordVector` + árbol **J48**; salida con clase y confianza.

---

## Declaración de uso de IA

Ver [`docs/DECLARACION_IA.md`](docs/DECLARACION_IA.md).

---

## Licencia y contacto

Proyecto académico para la asignatura Sistemas Inteligentes. Dudas: daniel.garijo@upm.es
