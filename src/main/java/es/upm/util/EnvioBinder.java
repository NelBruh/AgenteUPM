package es.upm.util;

import es.upm.ServiciosMas;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

/**
 * Envía mensajes ACL REQUEST al binder multiplexador descubierto en el DF.
 */
public final class EnvioBinder {
	
	public static final String CONVERSACION = "comunicacion-binder";

	private EnvioBinder() {}

	public static boolean enviar(Agent agente, String contenido) {
		if (agente == null) {
			System.err.println("[ENVIO] Agente nulo: no se puede enviar el mensaje.");
			return false;
		}
		if (contenido == null || contenido.isBlank()) {
			return false;
		}

		try {
			var resultados = DfHelper.buscar(agente, ServiciosMas.BINDER);
			if (resultados.length == 0) {
				System.out.println("[" + agente.getLocalName() + "] Binder no disponible en el DF.");
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
					"[" + agente.getLocalName() + "] Error al enviar mensaje al binder: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
}
