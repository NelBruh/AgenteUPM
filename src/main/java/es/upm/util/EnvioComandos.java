package es.upm.util;

import es.upm.ServiciosMas;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

/**
 * Envía mensajes ACL REQUEST al ejecutador de comandos descubierto en el DF.
 */
public final class EnvioComandos {

	public static final String CONVERSACION = "comandos-chat";

	private EnvioComandos() {}

	public static boolean enviar(Agent agente, String contenido) {
		if (agente == null) {
			System.err.println("[ENVIO] Agente nulo: no se puede enviar el mensaje.");
			return false;
		}
		if (contenido == null || contenido.isBlank()) {
			return false;
		}

		try {
			var resultados = DfHelper.buscar(agente, ServiciosMas.COMANDOS);
			if (resultados.length == 0) {
				System.out.println("[" + agente.getLocalName() + "] Ejecutador de comandos no disponible en el DF.");
				return false;
			}

			ACLMessage peticion = new ACLMessage(ACLMessage.REQUEST);
			peticion.addReceiver(resultados[0].getName());
			peticion.setConversationId(CONVERSACION);
			peticion.setContent(contenido);
			agente.send(peticion);
			return true;
		} catch (Exception e) {
			System.err.println(
					"[" + agente.getLocalName() + "] Error al enviar mensaje al ejecutador de comandos: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
}
