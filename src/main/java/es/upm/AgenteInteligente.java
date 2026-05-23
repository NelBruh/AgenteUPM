package es.upm;

import es.upm.behaviours.ComportamientoModeracion;
import es.upm.model.ResultadoClasificacion;
import es.upm.util.DfHelper;
import es.upm.util.RutasProyecto;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Agente de procesamiento inteligente: entrena un clasificador Weka (J48 + bag-of-words)
 * y modera mensajes de chat en tiempo simulado.
 */
public class AgenteInteligente extends Agent {

    private static final String FICHERO_ENTRENAMIENTO = "chat_entrenamiento.arff";

    private FilteredClassifier clasificadorWeka;
    private Instances datasetEntrenamiento;

    @Override
    protected void setup() {
        System.out.println("[MODERADOR] Agente activo: " + getLocalName());

        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                entrenarModelo();
            }
        });

        try {
            DfHelper.registrar(this, ServiciosMas.MODERADOR, "Filtro-Toxicidad-Weka-J48");
            System.out.println("[MODERADOR] Servicio registrado en el DF.");
        } catch (Exception e) {
            System.err.println("[MODERADOR] No se pudo registrar en el DF.");
            e.printStackTrace();
        }

        addBehaviour(new ComportamientoModeracion(this));
    }

    private void entrenarModelo() {
        try {
            String rutaArff = RutasProyecto.resolver(FICHERO_ENTRENAMIENTO);
            DataSource origen = new DataSource(rutaArff);
            datasetEntrenamiento = origen.getDataSet();
            datasetEntrenamiento.setClassIndex(datasetEntrenamiento.numAttributes() - 1);

            StringToWordVector filtroTexto = new StringToWordVector();
            filtroTexto.setInputFormat(datasetEntrenamiento);
            filtroTexto.setAttributeNamePrefix("palabra_");

            clasificadorWeka = new FilteredClassifier();
            clasificadorWeka.setFilter(filtroTexto);
            clasificadorWeka.setClassifier(new J48());
            clasificadorWeka.buildClassifier(datasetEntrenamiento);

            System.out.println("[MODERADOR] Modelo Weka entrenado correctamente (" + rutaArff + ").");
        } catch (Exception e) {
            System.err.println("[MODERADOR] Error crítico al entrenar el modelo Weka.");
            e.printStackTrace();
        }
    }

    public ResultadoClasificacion clasificarMensaje(String texto) {
        if (clasificadorWeka == null || datasetEntrenamiento == null) {
            return new ResultadoClasificacion("Limpio", 0.0, false);
        }

        try {
            String textoLimpio = texto.toLowerCase().replaceAll("[¡!.,¿?;:\"'\\-]", "").trim();

            Instance instancia = new DenseInstance(2);
            instancia.setDataset(datasetEntrenamiento);
            instancia.setValue(datasetEntrenamiento.attribute(0), textoLimpio);

            double indiceClase = clasificadorWeka.classifyInstance(instancia);
            String clase = datasetEntrenamiento.classAttribute().value((int) indiceClase);
            double[] distribucion = clasificadorWeka.distributionForInstance(instancia);
            double confianza = distribucion[(int) indiceClase];

            boolean toxico = "Toxico".equalsIgnoreCase(clase);
            return new ResultadoClasificacion(clase, confianza, toxico);
        } catch (Exception e) {
            System.err.println("[MODERADOR] Error al clasificar: " + texto);
            return new ResultadoClasificacion("Limpio", 0.0, false);
        }
    }

    @Override
    protected void takeDown() {
        DfHelper.darDeBaja(this);
        System.out.println("[MODERADOR] Agente finalizado.");
    }
}
