package es.upm.behaviours;

import es.upm.util.EnvioModerador;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.List;

/**
 * Comportamiento periódico: envía líneas del chat simulado al moderador vía ACL REQUEST.
 * El destinatario se descubre consultando el DF (servicio de moderación).
 */
public class ComportamientoAdquisicionChat extends TickerBehaviour {

    private final List<String> lineasChat;
    private int indice = 0;

    public ComportamientoAdquisicionChat(Agent agente, long periodoMs, List<String> lineasChat) {
        super(agente, periodoMs);
        this.lineasChat = lineasChat;
    }

    @Override
    protected void onTick() {
        if (indice >= lineasChat.size()) {
            System.out.println("[PERCEPTOR] Fin del chat simulado.");
            stop();
            return;
        }

        String mensaje = lineasChat.get(indice++);

        if (EnvioModerador.enviar(myAgent, mensaje)) {
            System.out.println("[PERCEPTOR-FICHERO] Enviado al moderador: " + mensaje);
        }
    }
}
