package es.upm;

/**
 * Tipos de servicio registrados y consultados en el Directory Facilitator (DF) de JADE.
 */
public final class ServiciosMas {

    /** Percepción desde fichero de texto (chat simulado). */
    public static final String PERCEPTOR_FICHERO = "perceptor-chat-fichero";
    /** Percepción mediante entrada manual del usuario. */
    public static final String PERCEPTOR_TECLADO = "perceptor-chat-teclado";
    public static final String MODERADOR = "moderador-toxicidad";
    public static final String VISUALIZADOR = "visualizador-chat";
    /* Gestión mediante agente multiplexador de líneas */
    public static final String BINDER = "multiplexador-chat-lineas";
    /* Ejecuciín de comandos mediante agente comandos*/
    public static final String COMANDOS = "ejecutador-comandos-chat-";

    private ServiciosMas() {
    }
}
