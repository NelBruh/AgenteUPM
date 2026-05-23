package es.upm;

import com.sun.net.httpserver.HttpServer;
import es.upm.behaviours.ComportamientoVisualizacion;
import es.upm.util.DfHelper;
import es.upm.util.SServer;
import jade.core.Agent;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Agente de visualización: Levanta un servidor local en localhost:8080 para
 * visualizar los mensajes
 */
public class AgenteVisualizador extends Agent {
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
}
