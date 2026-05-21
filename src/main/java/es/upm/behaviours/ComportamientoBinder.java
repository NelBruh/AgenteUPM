package es.upm.behaviours;

import es.upm.util.EnvioBinder;
import es.upm.util.EnvioComandos;
import es.upm.util.EnvioModerador;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ComportamientoBinder extends CyclicBehaviour {

	// Filtro para recibir solo mensajes de tipo REQUEST con el id de conversación correcto
	// Así no cogemos mensajes que no son para nosotros
	private final MessageTemplate plantilla = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
			MessageTemplate.MatchConversationId(EnvioBinder.CONVERSACION));

	public ComportamientoBinder(Agent agente) {
		super(agente);
	}

	@Override
	public void action() {
		// Esperamos bloqueados hasta que llegue un mensaje que cumpla el filtro
		ACLMessage peticion = myAgent.blockingReceive(plantilla);
		String contenido = peticion.getContent();
		
		// Codificación con if-else, se puede cambiar a futuro a switch-case para añadir multiplexación a otros agentes
		if(esComando(contenido)) {
			if(EnvioComandos.enviar(myAgent, contenido))
				System.out.println("[BINDER] Envió mensaje " + contenido + " al Agente Comandos");
		}
		else {
			if(EnvioModerador.enviar(myAgent, contenido))
				System.out.println("[BINDER] Enviado al moderador: "+ contenido);
		}
	}

	private boolean esComando(String contenido) {
		// El contenido del mensaje tiene el formato "Usuario: !discord"
		// Separamos por ":" en máximo 2 partes para obtener usuario y texto
		// El 2 evita que URLs como "http://..." se partan en más trozos
		String[] partes = contenido.split(":", 2);
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
		
		return comando.charAt(0) == '!';
	}
}
