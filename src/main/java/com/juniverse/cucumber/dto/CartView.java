package com.juniverse.cucumber.dto;

import com.juniverse.cucumber.model.Quote;
import java.util.List;

/**
 * Carrito para mostrar.
 *
 * @param id identificador
 * @param customer cliente
 * @param items líneas
 * @param coupon cupón aplicado
 * @param quote desglose de precios
 */
public record CartView(String id, String customer, List<ItemView> items, String coupon, Quote quote) {
}
