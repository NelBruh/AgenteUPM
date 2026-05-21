package es.upm.util;

import java.io.IOException;
import java.io.OutputStream;

public class SServer {
    public static OutputStream client;

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
