package com.udla.servicio;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** API REST externa: entrega la definición de nuevas figuras como {nombre, parametros, codigo}. */
@RestController
@RequestMapping("/figuras")
public class DefinicionController {

    // Figuras que ofrece este servicio y los parámetros de su constructor
    private static final Map<String, List<String>> CATALOGO = Map.of(
            "Hexagono", List.of("lado"),
            "Octagono", List.of("lado")
    );

    @GetMapping
    public Map<String, List<String>> listar() {
        return CATALOGO;
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<Map<String, Object>> obtener(@PathVariable("nombre") String nombre) throws IOException {
        ClassPathResource archivo = new ClassPathResource("definiciones/" + nombre + ".groovy");
        if (!CATALOGO.containsKey(nombre) || !archivo.exists()) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> definicion = new LinkedHashMap<>();
        definicion.put("nombre", nombre);
        definicion.put("parametros", CATALOGO.get(nombre));
        definicion.put("codigo", archivo.getContentAsString(StandardCharsets.UTF_8));
        return ResponseEntity.ok(definicion);
    }
}
