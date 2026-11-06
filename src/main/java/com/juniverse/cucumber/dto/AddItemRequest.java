package com.juniverse.cucumber.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Producto a agregar.
 *
 * @param sku código
 * @param quantity unidades
 */
public record AddItemRequest(@NotBlank(message = "El producto es obligatorio") String sku, @Min(value = 1, message = "La cantidad mínima es 1") int quantity) {
}
