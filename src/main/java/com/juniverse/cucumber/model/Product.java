package com.juniverse.cucumber.model;

import java.math.BigDecimal;

/**
 * Producto del catálogo.
 *
 * @param sku código
 * @param name nombre
 * @param price precio unitario
 * @param stock existencias disponibles
 */
public record Product(String sku, String name, BigDecimal price, int stock) {
}
