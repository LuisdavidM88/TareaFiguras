package com.udla.figuras.servicio;

import figuras.Figura;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

/**
 * "Máquina de interpretación": compila código Groovy en tiempo de ejecución.
 * Las clases que compila quedan registradas en este ClassLoader, así que
 * Class.forName("figuras.Pentagono", true, cargador) las encuentra igual que a una clase nativa.
 */
@Component
public class CargadorGroovy {

    private final GroovyClassLoader cargador;
    private final Map<String, String> cargadas = new TreeMap<>(); // nombre -> origen

    public CargadorGroovy() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setParameters(true); // conserva los nombres de los parámetros (lado, apotema...)
        // El padre es el ClassLoader de la app: así Groovy "ve" la interfaz figuras.Figura
        this.cargador = new GroovyClassLoader(Figura.class.getClassLoader(), config);
    }

    public ClassLoader getClassLoader() {
        return cargador;
    }

    /** Compila el código y devuelve el nombre simple de la clase (ej. "Pentagono"). */
    public synchronized String compilar(String codigo, String origen) {
        Class<?> clase;
        try {
            clase = cargador.parseClass(codigo);
        } catch (Exception e) {
            throw new FiguraException("Error al compilar el código Groovy: " + e.getMessage());
        }
        if (!Figura.class.isAssignableFrom(clase)) {
            throw new FiguraException("La clase " + clase.getName() + " no implementa figuras.Figura");
        }
        if (!"figuras".equals(clase.getPackageName())) {
            throw new FiguraException("La clase debe declararse en el paquete 'figuras' (package figuras)");
        }
        cargadas.put(clase.getSimpleName(), origen);
        return clase.getSimpleName();
    }

    public Map<String, String> getCargadas() {
        return cargadas;
    }
}
