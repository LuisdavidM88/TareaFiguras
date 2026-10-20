package com.udla.figuras.controlador;

import com.udla.figuras.servicio.CargadorGroovy;
import com.udla.figuras.servicio.FiguraException;
import com.udla.figuras.servicio.FiguraFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/figuras")
public class FiguraController {

    private final FiguraFactory factory;
    private final CargadorGroovy cargadorGroovy;

    public FiguraController(FiguraFactory factory, CargadorGroovy cargadorGroovy) {
        this.factory = factory;
        this.cargadorGroovy = cargadorGroovy;
    }

    /** Navegador: /api/figuras/crear?nombre=Circulo&radio=5 */
    @GetMapping("/crear")
    public Map<String, Object> crearGet(@RequestParam Map<String, String> todos) {
        Map<String, Object> parametros = new LinkedHashMap<>(todos);
        Object nombre = parametros.remove("nombre");
        if (nombre == null) {
            throw new FiguraException("Falta el parámetro 'nombre'");
        }
        return factory.crear(nombre.toString(), parametros);
    }

    /** JSON: {"nombre":"Circulo","parametros":{"radio":5}} */
    @PostMapping("/crear")
    public Map<String, Object> crearPost(@RequestBody SolicitudFigura solicitud) {
        Map<String, Object> parametros = solicitud.parametros() == null ? Map.of() : solicitud.parametros();
        return factory.crear(solicitud.nombre(), parametros);
    }

    /** Carga y compila una clase Groovy en tiempo de ejecución (texto plano en el cuerpo). */
    @PostMapping(value = "/groovy", consumes = "text/plain")
    public Map<String, Object> cargarGroovy(@RequestBody String codigo) {
        String nombre = cargadorGroovy.compilar(codigo, "groovy (cargada en ejecución)");
        return Map.of("mensaje", "Clase compilada y registrada", "figura", nombre);
    }

    /** Lista las figuras cargadas en ejecución (Groovy y servicio externo). */
    @GetMapping("/cargadas")
    public Map<String, String> cargadas() {
        return cargadorGroovy.getCargadas();
    }

    public record SolicitudFigura(String nombre, Map<String, Object> parametros) {
    }
}
