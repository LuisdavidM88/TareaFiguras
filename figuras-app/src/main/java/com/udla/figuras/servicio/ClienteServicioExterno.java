package com.udla.figuras.servicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Extra: consume la API REST externa que entrega la definición (código Groovy) de nuevas figuras. */
@Component
public class ClienteServicioExterno {

    private final RestClient restClient;
    private final String urlBase;

    public ClienteServicioExterno(@Value("${servicio.figuras.url}") String urlBase) {
        this.urlBase = urlBase;
        this.restClient = RestClient.create(urlBase);
    }

    public String obtenerCodigo(String nombre) {
        DefinicionFigura definicion;
        try {
            definicion = restClient.get()
                    .uri("/figuras/{nombre}", nombre)
                    .retrieve()
                    .body(DefinicionFigura.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new FiguraException("La figura '" + nombre + "' no existe ni localmente ni en el servicio externo");
        } catch (RestClientException e) {
            throw new FiguraException("No se pudo conectar con el servicio externo (" + urlBase + "): " + e.getMessage());
        }
        if (definicion == null || definicion.codigo() == null || definicion.codigo().isBlank()) {
            throw new FiguraException("El servicio externo devolvió una definición vacía para " + nombre);
        }
        return definicion.codigo();
    }
}
