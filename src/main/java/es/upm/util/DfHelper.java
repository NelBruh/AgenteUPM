package es.upm.util;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * Utilidades para registrar, consultar y dar de baja servicios en el DF de JADE.
 */
public final class DfHelper {

    private DfHelper() {
    }

    public static void registrar(Agent agente, String tipo, String nombre) throws FIPAException {
        DFAgentDescription descripcion = new DFAgentDescription();
        descripcion.setName(agente.getAID());
        ServiceDescription servicio = new ServiceDescription();
        servicio.setType(tipo);
        servicio.setName(nombre);
        descripcion.addServices(servicio);
        DFService.register(agente, descripcion);
    }

    public static void darDeBaja(Agent agente) {
        try {
            DFService.deregister(agente);
        } catch (Exception ignored) {
            // El contenedor puede estar cerrándose.
        }
    }

    public static DFAgentDescription[] buscar(Agent agente, String tipo) throws FIPAException {
        DFAgentDescription plantilla = new DFAgentDescription();
        ServiceDescription servicio = new ServiceDescription();
        servicio.setType(tipo);
        plantilla.addServices(servicio);
        return DFService.search(agente, plantilla);
    }
}
