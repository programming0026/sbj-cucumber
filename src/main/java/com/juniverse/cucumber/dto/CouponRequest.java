package com.juniverse.cucumber.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Cupón a aplicar.
 *
 * @param code código del cupón
 */
public record CouponRequest(@NotBlank(message = "El cupón es obligatorio") String code) {
}
