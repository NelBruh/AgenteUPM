package es.upm;

import es.upm.util.DfHelper;
import es.upm.util.Enviar;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Agente generativo que actúa como un usuario real en el chat.
 * Hace peticiones locales a Ollama para generar comentarios aleatorios.
 */
public class AgentePerceptorBot extends Agent {
    
    // URL por defecto del servidor local de Ollama
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    
    // El modelo que tengáis descargado (puede ser "llama3", "mistral", "phi3", etc.)
    private static final String MODELO = "llama3"; 
    
    private HttpClient httpClient;

    @Override
    protected void setup() {
        System.out.println("[PERCEPTOR-BOT] Agente activo: " + getLocalName());
        
        // Inicializamos el cliente HTTP nativo de Java 11
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        try {
            DfHelper.registrar(this, "perceptor-generativo", "Bot-Ollama");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configurado para generar un mensaje cada 15 segundos (15000 ms)
        addBehaviour(new TickerBehaviour(this, 15000) {
            @Override
            protected void onTick() {
                generarYEnviarMensaje();
            }
        });
    }

    private void generarYEnviarMensaje() {
        try {
            // Este es el "System Prompt" que le dice a la IA cómo comportarse
            String prompt = "Actúa como un espectador de un canal de Twitch. Escribe un comentario corto, directo y muy informal (máximo 10 palabras) en español. A veces sé amable, otras veces sé un hater tóxico, o simplemente reacciona. No uses comillas. Solo devuelve el comentario, nada más.";
            
            // Creamos el JSON a mano para evitar dependencias externas
            String jsonBody = String.format("{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false}", MODELO, prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Enviamos la petición de forma síncrona
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String respuestaJson = response.body();
                String comentario = extraerTexto(respuestaJson);
                
                if (comentario != null && !comentario.isBlank()) {
                    // Le ponemos un nombre de usuario genérico y se lo mandamos al Binder
                    String lineaFinal = "IAGenerativa: " + comentario.trim();
                    
                    // Usamos la clase Enviar genérica que creamos para el Issue #7
                    Enviar.mensaje(this, ServiciosMas.BINDER, ACLMessage.REQUEST, Enviar.CONV_BINDER, lineaFinal);
                    System.out.println("[PERCEPTOR-BOT] Generado y enviado → " + lineaFinal);
                }
            } else {
                System.err.println("[PERCEPTOR-BOT] Error de Ollama: Código " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("[PERCEPTOR-BOT] Error al contactar con Ollama. ¿Está encendido el servidor?");
        }
    }

    /**
     * Extractor simple de JSON para no añadir librerías como Gson o Jackson.
     * Busca el campo "response" y extrae su valor.
     */
    private String extraerTexto(String json) {
        String clave = "\"response\":\"";
        int inicio = json.indexOf(clave);
        if (inicio == -1) return null;
        inicio += clave.length();
        int fin = json.indexOf("\",\"", inicio);
        if (fin == -1) fin = json.indexOf("\"}", inicio);
        if (fin == -1) return null;
        
        String texto = json.substring(inicio, fin);
        // Limpiar saltos de línea o comillas escapadas
        return texto.replace("\\\"", "\"").replace("\\n", " ");
    }

    @Override
    protected void takeDown() {
        DfHelper.darDeBaja(this);
        System.out.println("[PERCEPTOR-BOT] Agente finalizado.");
    }
}
