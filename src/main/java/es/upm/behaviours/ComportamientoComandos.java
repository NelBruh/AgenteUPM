package es.upm.behaviours;

import es.upm.ServiciosMas;
import es.upm.util.Enviar;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Map;

/**
 * Comportamiento cíclico del agente de comandos: recibe REQUEST con un comando tipo !discord,
 * busca la respuesta en el mapa y envía dos INFORM al visualizador:
 * la línea original del usuario y la respuesta del bot.
 */
public class ComportamientoComandos extends CyclicBehaviour {

    // Mapa que asocia cada comando con su respuesta
    private final Map<String, String> comandos;

    // Filtro para recibir solo mensajes de tipo REQUEST con el id de conversación correcto
    // Así no cogemos mensajes que no son para nosotros
    private final MessageTemplate plantilla = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            // CORRECCIÓN ISSUE #7: Usar la constante unificada de comandos
            MessageTemplate.MatchConversationId(Enviar.CONV_COMANDOS));

    public ComportamientoComandos(Agent agente, Map<String, String> comandos) {
        super(agente);
        this.comandos = comandos;
    }

    @Override
    public void action() {
        // Esperamos bloqueados hasta que llegue un mensaje que cumpla el filtro
        ACLMessage peticion = myAgent.blockingReceive(plantilla);

        // El contenido del mensaje tiene el formato "Usuario: !discord"
        String contenido = peticion.getContent();

        // Separamos por ":" en máximo 2 partes para obtener usuario y texto
        // El 2 evita que URLs como "http://..." se partan en más trozos
        String[] partes = contenido.split(":", 2);
        String usuario = partes[0].trim();
        String texto;
        if (partes.length > 1) {
            // Caso normal: había ":" en el mensaje → cogemos la parte del texto
            texto = partes[1].trim();
        } else {
            // Caso raro: no había ":" → usamos el contenido entero como texto
            texto = contenido;
        }

        // Cogemos solo la primera palabra del texto como comando
        String comando = texto.split("\\s+")[0].toLowerCase();

        // Buscamos el comando en el mapa, si no existe devolvemos el mensaje de error
        String respuesta = comandos.getOrDefault(comando,
                "Bot: Comando no reconocido. Usa !ayuda para ver los comandos disponibles.");

        // CORRECCIÓN ISSUE #7: Usar la clase genérica en lugar del método local
        // Mandamos al visualizador primero la linea del usuario y luego la respuesta del bot
        Enviar.mensaje(myAgent, ServiciosMas.VISUALIZADOR, ACLMessage.INFORM, Enviar.CONV_VISUALIZADOR, usuario + ": " + texto);
        Enviar.mensaje(myAgent, ServiciosMas.VISUALIZADOR, ACLMessage.INFORM, Enviar.CONV_VISUALIZADOR, respuesta);

        System.out.println("[COMANDO] " + usuario + " usó " + comando + " → " + respuesta);
    }
    
    // El método enviarAlVisualizador ha sido eliminado según los requisitos del Issue #7
}
