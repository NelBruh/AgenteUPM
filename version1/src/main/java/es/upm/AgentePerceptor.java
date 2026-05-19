package es.upm;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AgentePerceptor extends Agent {
    private List<String> chatSimulado = new ArrayList<>();
    private int index = 0;

    protected void setup() {
        System.out.println("Agente Perceptor (Lector de Chat) [" + getLocalName() + "] activo.");

        // Leer el archivo de texto
        try (BufferedReader br = new BufferedReader(new FileReader("chat.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                chatSimulado.add(linea);
            }
        } catch (IOException e) {
            System.err.println("[PERCEPTOR] Error al leer el archivo chat.txt. Asegúrate de que está en la raíz del proyecto.");
            e.printStackTrace();
        }

        // Lee un mensaje nuevo del chat cada 4 segundos
        addBehaviour(new TickerBehaviour(this, 4000) { 
            protected void onTick() {
                if (index < chatSimulado.size()) {
                    String mensajeChat = chatSimulado.get(index++);
                    
                    // Buscar el servicio "Moderador" en el DF
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("Moderador");
                    template.addServices(sd);

                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        if (result.length > 0) {
                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                            msg.addReceiver(result[0].getName());
                            msg.setContent(mensajeChat);
                            send(msg);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                } else {
                    System.out.println("[PERCEPTOR] Fin del chat simulado.");
                    stop();
                }
            }
        });
    }
}