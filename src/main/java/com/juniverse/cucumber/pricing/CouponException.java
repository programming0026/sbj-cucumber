package com.juniverse.cucumber.pricing;

/** Un cupón no se puede aplicar. El mensaje explica el motivo al cliente. */
public class CouponException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción.
     *
     * @param message motivo, redactado para mostrarlo al cliente
     */
    public CouponException(String message) {
        super(message);
    }
}
