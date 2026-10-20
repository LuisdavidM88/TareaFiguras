package com.udla.figuras.controlador;

import com.udla.figuras.servicio.FiguraException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(FiguraException.class)
    public ResponseEntity<Map<String, String>> manejar(FiguraException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}
