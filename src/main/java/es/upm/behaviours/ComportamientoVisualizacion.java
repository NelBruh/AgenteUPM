package es.upm.behaviours;


import es.upm.util.EnvioModerador;
import es.upm.util.SServer;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import javax.swing.*;

/**
 * Comportamiento cíclico que recibe INFORM del moderador mediante filtro bloqueante
 * y actualiza la interfaz Swing en el hilo de eventos (EDT).
 */
public class ComportamientoVisualizacion extends CyclicBehaviour {

    private final MessageTemplate plantillaInforme =
            MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId(EnvioModerador.CONVERSACION));

    public ComportamientoVisualizacion(jade.core.Agent agente) {
        super(agente);
    }

    @Override
    public void action(){
        ACLMessage mensaje = myAgent.blockingReceive(plantillaInforme);
        String linea = mensaje.getContent();
        SServer.broadcast(linea);
    }
}
