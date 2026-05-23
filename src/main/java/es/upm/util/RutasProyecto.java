package es.upm.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resuelve rutas de ficheros de datos respecto al directorio del proyecto.
 */
public final class RutasProyecto {

    private static final String PROP_DATADIR = "agentupm.datadir";

    private RutasProyecto() {
    }

    public static void configurarDirectorioDatos(String directorio) {
        if (directorio != null && !directorio.isBlank()) {
            System.setProperty(PROP_DATADIR, directorio);
        }
    }

    public static String resolver(String nombreFichero) {
        Path base = Paths.get(System.getProperty(PROP_DATADIR, System.getProperty("user.dir")));
        Path candidato = base.resolve(nombreFichero);
        if (Files.isRegularFile(candidato)) {
            return candidato.toAbsolutePath().toString();
        }
        Path padre = base.getParent();
        if (padre != null) {
            Path alternativo = padre.resolve(nombreFichero);
            if (Files.isRegularFile(alternativo)) {
                return alternativo.toAbsolutePath().toString();
            }
        }
        return candidato.toAbsolutePath().toString();
    }
}
