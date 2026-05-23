package es.upm.util;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public final class Enviar {

    // Identificadores de conversación unificados
    public static final String CONV_BINDER = "comunicacion-binder";
    public static final String CONV_COMANDOS = "comandos-chat";
    public static final String CONV_MODERADOR = "moderacion-chat";
    public static final String CONV_VISUALIZADOR = "visualizacion-chat";

    private Enviar() {}

    /**
     * Método genérico para enviar mensajes (REQUEST o INFORM) buscando el servicio en el DF.
     */
    public static boolean mensaje(Agent emisor, String tipoServicio, int performativa, String conversacion, String contenido) {
        if (emisor == null || contenido == null || contenido.isBlank()) return false;

        try {
            var resultados = DfHelper.buscar(emisor, tipoServicio);
            if (resultados.length == 0) {
                System.out.println("[" + emisor.getLocalName() + "] Servicio " + tipoServicio + " no disponible.");
                return false;
            }

            ACLMessage msj = new ACLMessage(performativa);
            msj.addReceiver(resultados[0].getName());
            msj.setConversationId(conversacion);
            msj.setContent(contenido);
            emisor.send(msj);
            return true;
        } catch (Exception e) {
            System.err.println("[" + emisor.getLocalName() + "] Error al enviar a " + tipoServicio);
            e.printStackTrace();
            return false;
        }
    }
}
