package es.upm;

import es.upm.behaviours.ComportamientoAdquisicionChat;
import es.upm.behaviours.ComportamientoRecepcionInforme;
import es.upm.util.DfHelper;
//import es.upm.util.RutasProyecto;
import jade.core.Agent;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.List;

/**
 * Agente de percepción: adquiere mensajes externos desde un fichero de chat simulado
 * y los envía al moderador descubierto mediante el DF.
 */
public class AgentePerceptor extends Agent {

	private static final String FICHERO_CHAT = "chat.txt";
	private static final long PERIODO_MS = 4000L;

	@Override
	protected void setup() {
		System.out.println("[PERCEPTOR] Agente activo: " + getLocalName());					// Mensaje de bienvenida
		try {																				// Registro de servicio en DF
			DfHelper.registrar(this, ServiciosMas.PERCEPTOR_FICHERO, "Lector-Chat-TXT");
			System.out.println("[PERCEPTOR] Servicio registrado en el DF.");
		} catch (Exception e) {
			System.err.println("[PERCEPTOR] No se pudo registrar en el DF.");
			e.printStackTrace();
		}
//		List<String> lineas = cargarChat();
		addBehaviour(new ComportamientoAdquisicionChat(this, PERIODO_MS, FICHERO_CHAT));
		addBehaviour(new ComportamientoRecepcionInforme(this));
	}

//	private List<String> cargarChat() {
//		List<String> lineas = new ArrayList<>();
//		Path ruta = Path.of(RutasProyecto.resolver(FICHERO_CHAT));
//
//		if (!Files.isRegularFile(ruta)) {
//			System.err.println("[PERCEPTOR] No se encuentra " + ruta);
//			return null;
//		}
//
//		try (BufferedReader lector = Files.newBufferedReader(ruta)) {
//			String linea;
//			while ((linea = lector.readLine()) != null) {
//				if (!linea.isBlank()) {
//					lineas.add(linea.trim());
//				}
//			}
//			System.out.println("[PERCEPTOR] Cargadas " + lineas.size() + " líneas desde " + ruta);
//		} catch (IOException e) {
//			System.err.println("[PERCEPTOR] Error de lectura del fichero de chat.");
//			e.printStackTrace();
//		}
//		return lineas;
//	}

	@Override
	protected void takeDown() {
		DfHelper.darDeBaja(this);																	// Baja del servicio en el DF
		System.out.println("[PERCEPTOR] Agente finalizado.");										// Mensaje de finalización
	}
}
