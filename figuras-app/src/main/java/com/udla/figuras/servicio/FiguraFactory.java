package com.udla.figuras.servicio;

import figuras.Figura;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sigue los 5 pasos de "Reflection paso a paso":
 *  1. El dato          -> String nombreClase
 *  2. Class.forName()  -> Class<?> clase
 *  3. getDeclaredConstructor(tipos)
 *  4. newInstance(valores)
 *  5. Cast a Figura    -> polimorfismo normal
 */
@Service
public class FiguraFactory {

    private static final String PAQUETE = "figuras.";

    private final CargadorGroovy cargadorGroovy;
    private final ClienteServicioExterno servicioExterno;

    public FiguraFactory(CargadorGroovy cargadorGroovy, ClienteServicioExterno servicioExterno) {
        this.cargadorGroovy = cargadorGroovy;
        this.servicioExterno = servicioExterno;
    }

    public Map<String, Object> crear(String nombre, Map<String, Object> parametros) {
        // Paso 1 · El dato: el nombre llega como texto
        String nombreClase = nombre;

        // Paso 2 · Class.forName(): busca la clase (nativa, Groovy o del servicio externo)
        Class<?> clase = buscarClase(nombreClase);

        // Paso 3 · getDeclaredConstructor(): el constructor con esos tipos de parámetro
        Constructor<?> elegido = elegirConstructor(clase, parametros.keySet());
        Class<?>[] tipos = elegido.getParameterTypes();
        Constructor<?> ctor;
        try {
            ctor = clase.getDeclaredConstructor(tipos);
        } catch (NoSuchMethodException e) {
            throw new FiguraException("No existe el constructor " + Arrays.toString(tipos));
        }

        // Paso 4 · newInstance(): se crea el objeto con los valores reales
        Object[] valores = ordenarValores(ctor, parametros);
        Object obj;
        try {
            obj = ctor.newInstance(valores);
        } catch (InvocationTargetException e) {
            throw new FiguraException("La figura rechazó los parámetros: " + e.getCause().getMessage());
        } catch (ReflectiveOperationException e) {
            throw new FiguraException("No se pudo instanciar " + nombreClase + ": " + e.getMessage());
        }

        // Paso 5 · De vuelta a lo conocido: cast a Figura y polimorfismo normal
        Figura f = (Figura) obj;

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("figura", nombreClase);
        respuesta.put("origen", origenDe(nombreClase));
        respuesta.put("parametros", parametros);
        respuesta.put("area", redondear(f.calcularArea()));
        respuesta.put("perimetro", redondear(f.calcularPerimetro()));
        return respuesta;
    }

    private Class<?> buscarClase(String nombreClase) {
        try {
            return cargarYValidar(nombreClase);
        } catch (ClassNotFoundException e) {
            // Extra: si no existe, se pide la definición al servicio externo y se compila
            String codigo = servicioExterno.obtenerCodigo(nombreClase);
            cargadorGroovy.compilar(codigo, "servicio externo");
            try {
                return cargarYValidar(nombreClase);
            } catch (ClassNotFoundException otra) {
                throw new FiguraException("El servicio externo no entregó la clase " + nombreClase);
            }
        }
    }

    private Class<?> cargarYValidar(String nombreClase) throws ClassNotFoundException {
        Class<?> clase = Class.forName(PAQUETE + nombreClase, true, cargadorGroovy.getClassLoader());
        if (!Figura.class.isAssignableFrom(clase) || clase.isInterface()) {
            throw new FiguraException(nombreClase + " no es una figura");
        }
        return clase;
    }

    /** Elige el constructor cuyos nombres de parámetros coinciden con los enviados (radio, base...). */
    private Constructor<?> elegirConstructor(Class<?> clase, Set<String> nombres) {
        for (Constructor<?> c : clase.getDeclaredConstructors()) {
            List<String> nombresCtor = nombresDe(c);
            boolean mismaCantidad = nombresCtor.size() == nombres.size();
            // Si el compilador no guardó los nombres (arg0, arg1...), se acepta por cantidad y posición
            if (mismaCantidad && (!tieneNombres(c) || nombres.containsAll(nombresCtor))) {
                return c;
            }
        }
        String esperados = Arrays.stream(clase.getDeclaredConstructors())
                .map(c -> String.join(", ", nombresDe(c)))
                .collect(Collectors.joining(" | "));
        throw new FiguraException(clase.getSimpleName() + " espera los parámetros: " + esperados);
    }

    private List<String> nombresDe(Constructor<?> c) {
        List<String> nombres = new ArrayList<>();
        for (Parameter p : c.getParameters()) {
            nombres.add(p.getName());
        }
        return nombres;
    }

    private Object[] ordenarValores(Constructor<?> ctor, Map<String, Object> parametros) {
        Parameter[] ps = ctor.getParameters();
        List<Object> enOrden = new ArrayList<>(parametros.values());
        Object[] valores = new Object[ps.length];
        for (int i = 0; i < ps.length; i++) {
            Object valor = tieneNombres(ctor) ? parametros.get(ps[i].getName()) : enOrden.get(i);
            valores[i] = convertir(valor, ps[i].getType(), ps[i].getName());
        }
        return valores;
    }

    private boolean tieneNombres(Constructor<?> c) {
        Parameter[] ps = c.getParameters();
        return ps.length == 0 || ps[0].isNamePresent();
    }

    private Object convertir(Object valor, Class<?> tipo, String nombre) {
        try {
            String texto = String.valueOf(valor).trim();
            if (tipo == double.class || tipo == Double.class) return Double.parseDouble(texto);
            if (tipo == int.class || tipo == Integer.class) return Integer.parseInt(texto);
            if (tipo == long.class || tipo == Long.class) return Long.parseLong(texto);
            if (tipo == String.class) return texto;
            if (tipo == Object.class) return Double.parseDouble(texto); // parámetro sin tipo en Groovy (def)
        } catch (NumberFormatException e) {
            throw new FiguraException("El parámetro '" + nombre + "' debe ser numérico, llegó: " + valor);
        }
        throw new FiguraException("Tipo de parámetro no soportado: " + tipo.getSimpleName());
    }

    private String origenDe(String nombreClase) {
        return cargadorGroovy.getCargadas().getOrDefault(nombreClase, "nativa (Java)");
    }

    private double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
