package es.upm.behaviours;

import es.upm.util.EnvioModerador;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Comportamiento auxiliar del perceptor: muestra en consola las respuestas INFORM del moderador.
 */
public class ComportamientoRecepcionInforme extends CyclicBehaviour {

    private final MessageTemplate plantillaRespuesta =
            MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId(EnvioModerador.CONVERSACION));

    public ComportamientoRecepcionInforme(Agent agente) {
        super(agente);
    }

    @Override
    public void action() {
        ACLMessage respuesta = myAgent.receive(plantillaRespuesta);
        if (respuesta != null) {
            System.out.println("[" + myAgent.getLocalName().toUpperCase() + "] Respuesta ACL del moderador → "
                    + respuesta.getContent());
        } else {
            block();
        }
    }
}
