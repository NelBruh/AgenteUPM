package es.upm;

import com.sun.net.httpserver.HttpServer;
import es.upm.behaviours.ComportamientoVisualizacion;
import es.upm.util.DfHelper;
import es.upm.util.SServer;
import jade.core.Agent;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Agente de visualización: muestra en una interfaz Swing el chat moderado
 * recibido mediante mensajes ACL INFORM.
 */
public class AgenteVisualizador extends Agent {
    static OutputStream client;
    @Override
    protected void setup(){

        //crea servidor http
        try {
            DfHelper.registrar(this, ServiciosMas.VISUALIZADOR, "Interfaz-Chat-Swing");
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext("/", exchange -> {
                byte[] html = java.nio.file.Files.readAllBytes(new File("src/main/java/es/upm/index.html").toPath());
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, html.length);
                exchange.getResponseBody().write(html);
                exchange.getResponseBody().close();
            });

            server.createContext("/events", exchange -> {
                exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
                exchange.getResponseHeaders().set("Cache-Control", "no-cache");
                exchange.sendResponseHeaders(200, 0);
                SServer.client = exchange.getResponseBody();
                // mantiene la conexion abierta
                while (SServer.client != null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("SSE en http://localhost:8080");
        } catch (Exception e) {
            e.printStackTrace();
        }
        addBehaviour(new ComportamientoVisualizacion(this));
    }

    static void broadcast(String texto) {
        if (client != null) {
            try {
                client.write(("data: " + texto + "\n\n").getBytes());
                client.flush();
            } catch (IOException e) {
                client = null;
            }
        }
    }
/*
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
 */
}
