package es.upm;

import es.upm.util.RutasProyecto;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Punto de entrada: arranca el contenedor principal de JADE y los agentes del sistema.
 */
public class MainContainer {

    public static void main(String[] args) {
        Path raiz = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        RutasProyecto.configurarDirectorioDatos(raiz.toString());

        String agentes = String.join(";",
                "perceptor:es.upm.AgentePerceptor",
                "teclado:es.upm.AgentePerceptorTeclado",
                "moderador:es.upm.AgenteInteligente",
                "visualizador:es.upm.AgenteVisualizador");

        Profile perfil = new ProfileImpl();
        perfil.setParameter(Profile.GUI, "true");
        perfil.setParameter(Profile.MAIN, "true");
        perfil.setParameter(Profile.MAIN_HOST, "localhost");
        perfil.setParameter(Profile.MAIN_PORT, "1099");
        perfil.setParameter(Profile.AGENTS, agentes);

        Runtime.instance().createMainContainer(perfil);
        System.out.println("[SISTEMA] Contenedor JADE iniciado. Directorio de datos: " + raiz);
    }
}
