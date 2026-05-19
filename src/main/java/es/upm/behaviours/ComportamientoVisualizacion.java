package es.upm.behaviours;

import es.upm.util.EnvioModerador;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import javax.swing.*;

/**
 * Comportamiento cíclico que recibe INFORM del moderador mediante filtro bloqueante
 * y actualiza la interfaz Swing en el hilo de eventos (EDT).
 */
public class ComportamientoVisualizacion extends CyclicBehaviour {

    private final JTextArea areaChat;
    private final MessageTemplate plantillaInforme =
            MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId(EnvioModerador.CONVERSACION));

    public ComportamientoVisualizacion(jade.core.Agent agente, JTextArea areaChat) {
        super(agente);
        this.areaChat = areaChat;
    }

    @Override
    public void action() {
        ACLMessage mensaje = myAgent.blockingReceive(plantillaInforme);
        String linea = mensaje.getContent();

        SwingUtilities.invokeLater(() -> {
            areaChat.append(linea + "\n");
            areaChat.append("---------------------------------------------------\n");
            areaChat.setCaretPosition(areaChat.getDocument().getLength());
        });
    }
}
