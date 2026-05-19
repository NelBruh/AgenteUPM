package es.upm;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import javax.swing.*;
import java.awt.*;

public class AgenteVisualizador extends Agent {
    private JTextArea areaChat;

    protected void setup() {
        // Interfaz gráfica simulando un Chat
        JFrame frame = new JFrame("🔴 Chat en Directo - Modo Seguro Activado");
        areaChat = new JTextArea(15, 50);
        areaChat.setEditable(false);
        areaChat.setFont(new Font("SansSerif", Font.BOLD, 14));
        areaChat.setBackground(Color.BLACK);
        areaChat.setForeground(Color.WHITE);
        
        frame.add(new JScrollPane(areaChat));
        frame.pack();
        frame.setVisible(true);

        // Registro en DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Visualizador");
        sd.setName("Interfaz-Chat");
        dfd.addServices(sd);
        try { DFService.register(this, dfd); } catch (Exception e) { e.printStackTrace(); }

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    areaChat.append(" " + msg.getContent() + "\n");
                    areaChat.append(" ---------------------------------------------------\n");
                    // Hacer scroll automático hacia abajo
                    areaChat.setCaretPosition(areaChat.getDocument().getLength());
                } else {
                    block();
                }
            }
        });
    }
}