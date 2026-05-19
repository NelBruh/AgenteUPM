package es.upm;

import es.upm.behaviours.ComportamientoVisualizacion;
import es.upm.util.DfHelper;
import jade.core.Agent;

import javax.swing.*;
import java.awt.*;

/**
 * Agente de visualización: muestra en una interfaz Swing el chat moderado
 * recibido mediante mensajes ACL INFORM.
 */
public class AgenteVisualizador extends Agent {

    private JFrame ventana;

    @Override
    protected void setup() {
        System.out.println("[VISUALIZADOR] Agente activo: " + getLocalName());

        JTextArea areaChat = crearInterfaz();

        try {
            DfHelper.registrar(this, ServiciosMas.VISUALIZADOR, "Interfaz-Chat-Swing");
            System.out.println("[VISUALIZADOR] Servicio registrado en el DF.");
        } catch (Exception e) {
            System.err.println("[VISUALIZADOR] No se pudo registrar en el DF.");
            e.printStackTrace();
        }

        addBehaviour(new ComportamientoVisualizacion(this, areaChat));
    }

    private JTextArea crearInterfaz() {
        JTextArea areaChat = new JTextArea(18, 55);
        areaChat.setEditable(false);
        areaChat.setFont(new Font("SansSerif", Font.PLAIN, 14));
        areaChat.setBackground(Color.BLACK);
        areaChat.setForeground(Color.WHITE);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);

        ventana = new JFrame("Chat en directo — Moderación multiagente (JADE + Weka)");
        ventana.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        ventana.add(new JScrollPane(areaChat));
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);

        areaChat.append("=== Sistema multiagente UPM — esperando mensajes moderados ===\n\n");
        return areaChat;
    }

    @Override
    protected void takeDown() {
        DfHelper.darDeBaja(this);
        if (ventana != null) {
            SwingUtilities.invokeLater(() -> ventana.dispose());
        }
        System.out.println("[VISUALIZADOR] Agente finalizado.");
    }
}
