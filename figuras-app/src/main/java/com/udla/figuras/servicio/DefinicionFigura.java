package com.udla.figuras.servicio;

import java.util.List;

/** Contrato JSON del servicio externo: {nombre, parametros, codigo} */
public record DefinicionFigura(String nombre, List<String> parametros, String codigo) {
}
