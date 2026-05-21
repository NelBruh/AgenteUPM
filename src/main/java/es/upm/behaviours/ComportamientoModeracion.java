package es.upm.behaviours;

import es.upm.AgenteInteligente;
import es.upm.ServiciosMas;
import es.upm.util.DfHelper;
import es.upm.util.EnvioModerador;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Comportamiento cíclico con filtro de mensajes en modo bloqueante (REQUEST).
 * Clasifica el texto con Weka y reenvía el resultado al visualizador.
 */
public class ComportamientoModeracion extends CyclicBehaviour {

    private final AgenteInteligente moderador;
    private final MessageTemplate plantillaPeticion =
            MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchConversationId(EnvioModerador.CONVERSACION));

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
//        ACLMessage respuesta = peticion.createReply();
//        respuesta.setPerformative(ACLMessage.INFORM);
//        respuesta.setContent("Clasificación: " + resultado.clasePredicha() + " | confianza=" + resultado.confianza());
//        myAgent.send(respuesta);

        enviarAlPerceptor(respuesta);
        enviarAlVisualizador(lineaVisualizador);
    }

    private void enviarAlVisualizador(String contenido) {
        try {
            var resultados = DfHelper.buscar(myAgent, ServiciosMas.VISUALIZADOR);
            if (resultados.length == 0) {
                System.err.println("[MODERADOR] No hay visualizador registrado en el DF.");
                return;
            }
            ACLMessage informe = new ACLMessage(ACLMessage.INFORM);
            informe.addReceiver(resultados[0].getName());
            informe.setConversationId(EnvioModerador.CONVERSACION);
            informe.setContent(contenido);
            myAgent.send(informe);
        } catch (Exception e) {
            System.err.println("[MODERADOR] Error al localizar el visualizador en el DF.");
            e.printStackTrace();
        }
    }
    
    private void enviarAlPerceptor(String contenido) {
        try {
            var resultados = DfHelper.buscar(myAgent, ServiciosMas.PERCEPTOR_FICHERO);
            if (resultados.length == 0) {
                System.err.println("[MODERADOR] No hay perceptor registrado en el DF.");
                return;
            }
            ACLMessage informe = new ACLMessage(ACLMessage.INFORM);
            informe.addReceiver(resultados[0].getName());
            informe.setConversationId(EnvioModerador.CONVERSACION);
            informe.setContent(contenido);
            myAgent.send(informe);
        } catch (Exception e) {
            System.err.println("[MODERADOR] Error al localizar el perceptor en el DF.");
            e.printStackTrace();
        }
    }
}
