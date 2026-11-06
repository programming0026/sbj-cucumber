package com.juniverse.cucumber.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datos para crear un carrito.
 *
 * @param customer cliente
 * @param vip si es VIP
 * @param firstPurchase si es su primera compra
 */
public record CreateCartRequest(@NotBlank(message = "El cliente es obligatorio") String customer, boolean vip, boolean firstPurchase) {
}
