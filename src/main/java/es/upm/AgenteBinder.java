package es.upm;

import es.upm.behaviours.ComportamientoBinder;
import es.upm.util.DfHelper;
import jade.core.Agent;

/**
 * Agente multiplexador de líneas de texto, diferencia si la línea de texto es un
 * texto normal o un comando y lo reenvia al agente correspondiente.
 */
public class AgenteBinder extends Agent {

	@Override
	protected void setup() {
		System.out.println("[BINDER] Agente activo: " + getLocalName());
		try {
			DfHelper.registrar(this, ServiciosMas.BINDER, "Multiplexador-Lineas-Chat");
			System.out.println("[BINDER] Servicio registrado en el DF.");
		} catch (Exception e) {
			System.err.println("[BINDER] No se pudo registrar en el DF.");
			e.printStackTrace();
		}
		addBehaviour(new ComportamientoBinder(this));
	}
	
	
	
	
	@Override
	protected void takeDown() {
		DfHelper.darDeBaja(this);																	// Baja del servicio en el DF
		System.out.println("[BINDER] Agente finalizado.");										// Mensaje de finalización
	}
}
