package es.upm;

import es.upm.behaviours.ComportamientoEntradaTeclado;
import es.upm.behaviours.ComportamientoRecepcionInforme;
import es.upm.util.DfHelper;
import jade.core.Agent;

import javax.swing.*;

/**
 * Segundo agente de percepción: adquiere información externa mediante interacción
 * con el usuario (entrada manual por teclado en una ventana Swing).
 */
public class AgentePerceptorTeclado extends Agent {

    private JFrame ventanaEntrada;

    public void registrarVentana(JFrame ventana) {
        this.ventanaEntrada = ventana;
    }

    @Override
    protected void setup() {
        System.out.println("[PERCEPTOR-TECLADO] Agente activo: " + getLocalName());

        try {
            DfHelper.registrar(this, ServiciosMas.PERCEPTOR_TECLADO, "Entrada-Teclado-Manual");
            System.out.println("[PERCEPTOR-TECLADO] Servicio registrado en el DF.");
        } catch (Exception e) {
            System.err.println("[PERCEPTOR-TECLADO] No se pudo registrar en el DF.");
            e.printStackTrace();
        }

        addBehaviour(new ComportamientoEntradaTeclado(this));
        addBehaviour(new ComportamientoRecepcionInforme(this));
    }

    @Override
    protected void takeDown() {
        DfHelper.darDeBaja(this);
        if (ventanaEntrada != null) {
            SwingUtilities.invokeLater(() -> ventanaEntrada.dispose());
        }
        System.out.println("[PERCEPTOR-TECLADO] Agente finalizado.");
    }
}
