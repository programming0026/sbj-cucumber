package com.juniverse.cucumber.dto;

import java.math.BigDecimal;

/**
 * Línea del carrito para mostrar.
 *
 * @param sku código
 * @param name nombre
 * @param quantity unidades
 * @param unitPrice precio unitario
 * @param lineTotal importe de la línea
 */
public record ItemView(String sku, String name, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
}
