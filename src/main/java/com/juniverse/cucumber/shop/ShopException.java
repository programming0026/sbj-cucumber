package com.juniverse.cucumber.shop;

import org.springframework.http.HttpStatus;

/** Error de negocio de la tienda con el código HTTP que le corresponde. */
public class ShopException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus status;

    /**
     * Crea la excepción.
     *
     * @param status código HTTP
     * @param message descripción para el cliente
     */
    public ShopException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Código HTTP.
     *
     * @return estado
     */
    public HttpStatus status() {
        return status;
    }
}
