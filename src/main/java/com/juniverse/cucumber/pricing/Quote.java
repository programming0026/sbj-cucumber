package com.juniverse.cucumber.pricing;

import java.math.BigDecimal;

/**
 * Desglose de lo que cuesta una compra.
 *
 * @param subtotal suma de las líneas antes de descuentos
 * @param volumeDiscount descuento por comprar muchas unidades de un mismo producto
 * @param couponDiscount descuento del cupón
 * @param net importe tras los descuentos
 * @param shipping costo de envío
 * @param tax IVA sobre el importe neto
 * @param total total a pagar
 * @param coupon cupón aplicado, o {@code null}
 */
public record Quote(BigDecimal subtotal, BigDecimal volumeDiscount, BigDecimal couponDiscount, BigDecimal net, BigDecimal shipping,
        BigDecimal tax, BigDecimal total, String coupon) {
}
