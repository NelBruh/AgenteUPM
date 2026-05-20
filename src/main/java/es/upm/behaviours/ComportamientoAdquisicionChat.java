package es.upm.behaviours;

import es.upm.util.EnvioModerador;
import es.upm.util.RutasProyecto;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Comportamiento periódico: envía líneas del chat simulado al moderador vía ACL REQUEST.
 * El destinatario se descubre consultando el DF (servicio de moderación).
 */
public class ComportamientoAdquisicionChat extends TickerBehaviour {
	private BufferedReader lectorChat;
	private String linea;

	public ComportamientoAdquisicionChat(Agent agente, long periodoMs, String fichero) {
		super(agente, periodoMs);
		lectorChat = obtenerLector(fichero);
	}


	/**
	 * Envia un mensaje con la siguiente línea del chat cada periodoMs.
	 */
	@Override
	protected void onTick() {
		linea = cogerLinea();																// Coge la siguiente línea del fichero
		if (linea == null) {																// Si es null, es el final del fichero
			System.out.println("[PERCEPTOR] Fin del chat simulado.");
			stop();
			return;
		}
		if (EnvioModerador.enviar(myAgent, linea)) {										// Si no es el final, se envía la línea al agente
			System.out.println("[PERCEPTOR-FICHERO] Enviado al moderador: " + linea);		// inteligente
		}
	}

	
	
	
	/**
	 * 
	 * @return una línea del fichero de chat, devuelve null si ha llegado al final del fichero.
	 */
	private String cogerLinea() {
		String linea;
		try {
			linea = lectorChat.readLine();
			while (linea != null && linea.isBlank()) { linea = lectorChat.readLine(); };	// Ciclado de líneas que estén en vacías, si es null sale y se devuelve null
			return linea != null ? linea.trim() : null;										// TODO: puede dar null pointer exception?
		} catch (IOException e) {
			System.err.println("[PERCEPTOR] Error de lectura del fichero de chat.");
			e.printStackTrace();
			stop();
			return null;
		}
	}

	/**
	 * @param fichero
	 * @return un buffer de lectura para el fichero pasado como parámetro,
	 * si hay algún error se devuelve null.
	 */
	private BufferedReader obtenerLector(String fichero) {
		Path ruta = Path.of(RutasProyecto.resolver(fichero));
		if (!Files.isRegularFile(ruta)) {												// Obtiene el path al fichero, null si error.
			System.err.println("[PERCEPTOR] No se encuentra " + ruta);
			return null;
		}
		try {					
			return Files.newBufferedReader(ruta);										// Obtiene el buffer de lectura para dicho fichero.
		} catch (IOException e) {														// Si falla para y devuelve null.
			System.err.println("[PERCEPTOR] Error de lectura del fichero de chat.");
			e.printStackTrace();
			stop();
			return null;
		}
	}
	
	
	@Override
	public int onEnd() {
	    if (lectorChat != null) {
	        try {
	            lectorChat.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return super.onEnd();															// Envío de un entero para saber el estado de finalización
	}
}
