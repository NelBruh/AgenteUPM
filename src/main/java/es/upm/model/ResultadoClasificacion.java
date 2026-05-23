package es.upm.model;

import java.util.Objects;

/**
 * Resultado de la clasificación Weka para un mensaje de chat.
 */
public class ResultadoClasificacion {
    
    private final String clasePredicha;
    private final double confianza;
    private final boolean toxico;

    // Constructor
    public ResultadoClasificacion(String clasePredicha, double confianza, boolean toxico) {
        this.clasePredicha = clasePredicha;
        this.confianza = confianza;
        this.toxico = toxico;
    }

    // Getters equivalentes a los que genera el record
    public String clasePredicha() {
        return clasePredicha;
    }

    public double confianza() {
        return confianza;
    }

    public boolean toxico() {
        return toxico;
    }
    
    // Si en el código original se usaban getClasePredicha(), getConfianza()... 
    // añade esos métodos también. Los records usan nombreMetodo() en lugar de getNombreMetodo().

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultadoClasificacion that = (ResultadoClasificacion) o;
        return Double.compare(that.confianza, confianza) == 0 &&
               toxico == that.toxico &&
               Objects.equals(clasePredicha, that.clasePredicha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clasePredicha, confianza, toxico);
    }

    @Override
    public String toString() {
        return "ResultadoClasificacion{" +
                "clasePredicha='" + clasePredicha + '\'' +
                ", confianza=" + confianza +
                ", toxico=" + toxico +
                '}';
    }
}
