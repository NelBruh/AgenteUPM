package es.upm.behaviours;

import es.upm.AgentePerceptorTeclado;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import es.upm.util.Enviar;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Crea la ventana Swing de entrada manual. Guarda una referencia propia al agente
 * porque, al ser {@link OneShotBehaviour}, JADE anula {@code myAgent} al terminar
 * y los listeners Swing seguirían activos.
 */
public class ComportamientoEntradaTeclado extends OneShotBehaviour {

  /** Referencia estable al agente (no usar {@code myAgent} tras finalizar el behaviour). */
  private final Agent agente;

  private JFrame ventana;

  public ComportamientoEntradaTeclado(Agent agente) {
    super(agente);
    this.agente = agente;
  }

  @Override
  public void action() {
    SwingUtilities.invokeLater(this::crearVentana);
  }

  private void crearVentana() {
    ventana = new JFrame("Entrada manual — Perceptor teclado");
    ventana.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    ventana.setLayout(new BorderLayout(8, 8));

    JLabel instrucciones = new JLabel(
        "<html>Formato: <b>Usuario: mensaje</b> — Ej: <i>Usuario1: Hola a todos</i></html>");
    instrucciones.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

    JTextField campo = new JTextField();
    campo.setFont(new Font("SansSerif", Font.PLAIN, 14));

    JButton botonEnviar = new JButton("Enviar al moderador");
    botonEnviar.addActionListener(e -> enviarLinea(campo.getText(), campo));

    campo.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          enviarLinea(campo.getText(), campo);
        }
      }
    });

    JPanel sur = new JPanel(new BorderLayout(6, 0));
    sur.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    sur.add(campo, BorderLayout.CENTER);
    sur.add(botonEnviar, BorderLayout.EAST);

    ventana.add(instrucciones, BorderLayout.NORTH);
    ventana.add(sur, BorderLayout.CENTER);
    ventana.pack();
    ventana.setLocationByPlatform(true);
    ventana.setVisible(true);

    if (agente instanceof AgentePerceptorTeclado) {
      ((AgentePerceptorTeclado) agente).registrarVentana(ventana);
    }

    System.out.println("[PERCEPTOR-TECLADO] Ventana de entrada manual lista.");
  }

  private void enviarLinea(String texto, JTextField campo) {
    String linea = texto == null ? "" : texto.trim();
    if (linea.isEmpty()) return;
    if (!linea.contains(":")) linea = "Manual:" + linea;

    // ISSUE #7: Envío genérico al Binder mediante REQUEST
    boolean enviado = es.upm.util.Enviar.mensaje(agente, es.upm.ServiciosMas.BINDER, jade.lang.acl.ACLMessage.REQUEST, es.upm.util.Enviar.CONV_BINDER, linea);
    
    if (enviado) {
      System.out.println("[PERCEPTOR-TECLADO] Enviado → " + linea);
      campo.setText("");
    } else {
      JOptionPane.showMessageDialog(ventana, "No se encontró el Binder en el DF.", "Error", JOptionPane.WARNING_MESSAGE);
    }
  }
}
