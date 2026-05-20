package es.upm;

import es.upm.behaviours.ComportamientoBinder;
import es.upm.behaviours.ComportamientoComandos;
import es.upm.util.DfHelper;
import jade.core.Agent;

/**
 * Agente multiplexador de líneas de texto, diferencia si la línea de texto es un
 * texto normal o un comando y lo reenvia al agente correspondiente.
 */
public class AgenteBinder extends Agent {

	@Override
	protected void setup() {
		System.out.println("[COMANDO] Agente activo: " + getLocalName());
		try {
			DfHelper.registrar(this, ServiciosMas.BINDER, "Multiplexador-Lineas-Chat");
			System.out.println("[COMANDO] Servicio registrado en el DF.");
		} catch (Exception e) {
			System.err.println("[COMANDO] No se pudo registrar en el DF.");
			e.printStackTrace();
		}
		addBehaviour(new ComportamientoBinder());
	}
	
	
	
	
	@Override
	protected void takeDown() {
		DfHelper.darDeBaja(this);																	// Baja del servicio en el DF
		System.out.println("[PERCEPTOR] Agente finalizado.");										// Mensaje de finalización
	}
}
