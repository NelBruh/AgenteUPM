package es.upm.util;

import java.io.IOException;
import java.io.OutputStream;

/**
* Utilidad para enviar mensajes al servidor http local
*/
public class SServer {
    public static OutputStream client;
    //manda mensaje al
    public static void broadcast(String texto) {
        if (client != null) {
            try {
                client.write(("data: " + texto + "\n\n").getBytes());
                client.flush();
            } catch (IOException e) {
                client = null;
            }
        }
    }
}
