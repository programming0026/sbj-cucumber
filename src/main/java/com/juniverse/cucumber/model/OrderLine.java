package com.juniverse.cucumber.model;

import java.math.BigDecimal;

/**
 * Línea de un pedido.
 *
 * @param sku código del producto
 * @param quantity unidades
 * @param unitPrice precio unitario
 */
public record OrderLine(String sku, int quantity, BigDecimal unitPrice) {

    /**
     * Importe de la línea antes de descuentos.
     *
     * @return cantidad por precio unitario
     */
    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
