package es.upm;

import es.upm.behaviours.ComportamientoComandos;
import es.upm.util.DfHelper;
import jade.core.Agent;

import java.util.HashMap;
import java.util.Map;

/**
 * Agente de comandos de chat: responde a comandos tipo !discord, !horario, etc.
 * Registra sus respuestas en un mapa y las envía al visualizador vía ACL INFORM.
 */
public class AgenteComando extends Agent {

    static final String SERVICIO = "chatbot-comandos";

    private Map<String, String> comandos;

    @Override
    protected void setup() {
        System.out.println("[COMANDO] Agente activo: " + getLocalName());

        comandos = new HashMap<>();
        comandos.put("!discord",       "Bot: Únete al Discord del canal → https://discord.gg/ejemplo");
        comandos.put("!redessociales", "Bot: Twitter @canal | Instagram @canal | YouTube @canal");
        comandos.put("!horario",       "Bot: Streams en directo → Lunes, Miércoles y Viernes a las 19:00h");
        comandos.put("!ayuda",         "Bot: Comandos disponibles → !discord | !redessociales | !horario");

        try {
            DfHelper.registrar(this, SERVICIO, "Bot-Comandos-Chat");
            System.out.println("[COMANDO] Servicio registrado en el DF.");
        } catch (Exception e) {
            System.err.println("[COMANDO] No se pudo registrar en el DF.");
            e.printStackTrace();
        }

        addBehaviour(new ComportamientoComandos(this, comandos));
    }

    @Override
    protected void takeDown() {
        DfHelper.darDeBaja(this);
        System.out.println("[COMANDO] Agente finalizado.");
    }
}
