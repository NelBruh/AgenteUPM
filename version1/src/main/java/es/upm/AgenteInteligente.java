package es.upm;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class AgenteInteligente extends Agent {
    private FilteredClassifier clasificadorWeka;
    private Instances datasetEntrenamiento;

    protected void setup() {
        System.out.println("Agente Inteligente (Moderador IA) [" + getLocalName() + "] iniciando...");

        // 1. CARGAR Y ENTRENAR EL MODELO WEKA
        try {
            // Cargar el dataset de entrenamiento .arff
            DataSource source = new DataSource("chat_entrenamiento.arff");
            datasetEntrenamiento = source.getDataSet();
            // Indicar a Weka que la clase a predecir es la última columna (Limpio o Toxico)
            datasetEntrenamiento.setClassIndex(datasetEntrenamiento.numAttributes() - 1);

            // Configurar el filtro para procesar texto (Pasa de frases a matriz de palabras)
            StringToWordVector filtroTexto = new StringToWordVector();
            filtroTexto.setInputFormat(datasetEntrenamiento);
            filtroTexto.setAttributeNamePrefix("palabra_");
            // Crear el clasificador ensamblando el filtro de texto y el algoritmo J48
            clasificadorWeka = new FilteredClassifier();
            clasificadorWeka.setFilter(filtroTexto);
            clasificadorWeka.setClassifier(new J48()); // Algoritmo de Árbol de Decisión

            // Entrenar el modelo con los datos
            clasificadorWeka.buildClassifier(datasetEntrenamiento);
            System.out.println("[WEKA] ¡Modelo de Machine Learning entrenado con éxito!");

        } catch (Exception e) {
            System.err.println("[WEKA] ERROR crítico al cargar o entrenar WEKA.");
            e.printStackTrace();
        }

        // 2. REGISTRO EN EL DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Moderador");
        sd.setName("Filtro-Toxicidad-Weka");
        dfd.addServices(sd);
        try { DFService.register(this, dfd); } catch (Exception e) { e.printStackTrace(); }

        // 3. COMPORTAMIENTO: RECIBIR Y CLASIFICAR
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                // Filtro bloqueante
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage msg = myAgent.blockingReceive(mt);

                if (msg != null) {
                    String mensajeOriginal = msg.getContent();
                    
                    // Separar usuario de texto para que Weka solo analice el contenido
                    String[] partes = mensajeOriginal.split(":", 2);
                    String usuario = partes[0];
                    String texto = partes.length > 1 ? partes[1] : mensajeOriginal;

                    // PREDICCIÓN CON WEKA
                    boolean esToxico = analizarConWeka(texto);

                    String resultadoFinal;
                    if (esToxico) {
                        resultadoFinal = usuario + ": [MENSAJE BLOQUEADO POR LA IA (J48)]";
                    } else {
                        resultadoFinal = usuario + ":" + texto;
                    }
                    
                    enviarAVisualizador(resultadoFinal);
                }
            }
        });
    }

    // Lógica pura de Machine Learning
    private boolean analizarConWeka(String texto) {
        if (clasificadorWeka == null || datasetEntrenamiento == null) return false;

        try {
            // Limpieza básica para facilitar el análisis a Weka
            String textoLimpio = texto.toLowerCase().replaceAll("[¡!.,¿?;:\"'-]", "");

            // Crear una nueva instancia (fila de datos) para WEKA
            Instance instancia = new DenseInstance(2);
            instancia.setDataset(datasetEntrenamiento);
            instancia.setValue(datasetEntrenamiento.attribute(0), textoLimpio);

            // Pedir a Weka que clasifique la nueva frase
            double resultado = clasificadorWeka.classifyInstance(instancia);
            String clasePredicha = datasetEntrenamiento.classAttribute().value((int) resultado);

            return clasePredicha.equals("Toxico");
        } catch (Exception e) {
            System.err.println("[WEKA] Error al clasificar la frase: " + texto);
            return false;
        }
    }

    private void enviarAVisualizador(String res) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Visualizador");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                ACLMessage info = new ACLMessage(ACLMessage.INFORM);
                info.addReceiver(result[0].getName());
                info.setContent(res);
                send(info);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    protected void takeDown() {
        try { DFService.deregister(this); } catch (Exception e) {}
    }
}