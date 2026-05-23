package es.upm.behaviours;

import es.upm.AgenteInteligente;
import es.upm.ServiciosMas;
import es.upm.util.Enviar;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Comportamiento cíclico con filtro de mensajes en modo bloqueante (REQUEST).
<<<<<<< HEAD
 * Clasifica el texto con Weka y reenvía el resultado al visualizador.
=======
 * Clasifica el texto con Weka y reenvía el resultado al visualizador y al perceptor.
>>>>>>> MiguelD
 */
public class ComportamientoModeracion extends CyclicBehaviour {

    private final AgenteInteligente moderador;
    
    // CORRECCIÓN ISSUE #7: Filtrar usando la constante unificada de moderación
    private final MessageTemplate plantillaPeticion =
            MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchConversationId(Enviar.CONV_MODERADOR));

    public ComportamientoModeracion(AgenteInteligente moderador) {
        super(moderador);
        this.moderador = moderador;
    }

    @Override
    public void action() {
        ACLMessage peticion = myAgent.blockingReceive(plantillaPeticion);

        String mensajeOriginal = peticion.getContent();
        String[] partes = mensajeOriginal.split(":", 2);
        String usuario = partes[0].trim();
        String texto = partes.length > 1 ? partes[1].trim() : mensajeOriginal;

        var resultado = moderador.clasificarMensaje(texto);
        String lineaVisualizador = resultado.toxico()
                ? usuario + ": [MENSAJE BLOQUEADO POR LA IA — clase " + resultado.clasePredicha() + "]"
                : usuario + ": " + texto;

        String respuesta = "Clasificación: " + resultado.clasePredicha() + " | confianza=" + resultado.confianza();

        // CORRECCIÓN ISSUE #7: Usar la clase genérica Enviar en vez de los métodos privados locales
        // Mandamos el informe técnico al Perceptor
        Enviar.mensaje(myAgent, ServiciosMas.PERCEPTOR_FICHERO, ACLMessage.INFORM, Enviar.CONV_MODERADOR, respuesta);
        
        // Mandamos el texto final (filtrado o limpio) al Visualizador
        Enviar.mensaje(myAgent, ServiciosMas.VISUALIZADOR, ACLMessage.INFORM, Enviar.CONV_VISUALIZADOR, lineaVisualizador);
    }

    // Los métodos enviarAlVisualizador() y enviarAlPerceptor() han sido eliminados para limpiar el código
}
