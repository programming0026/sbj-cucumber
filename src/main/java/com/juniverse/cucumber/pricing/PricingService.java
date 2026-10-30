package com.juniverse.cucumber.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Reglas de precios de la tienda: la lógica de negocio que las pruebas Cucumber describen.
 *
 * <ol>
 *   <li><b>Descuento por volumen</b> (por línea): 5 % desde 10 unidades y 10 % desde 50 unidades de un mismo producto.</li>
 *   <li><b>Cupón</b> (sobre el subtotal ya con descuento por volumen): {@code BIENVENIDO10} (10 %, solo en la primera compra),
 *       {@code VIP20} (20 %, solo clientes VIP), {@code NAVIDAD25} (25 %, solo en diciembre y con compra mínima de 200) y
 *       {@code ENVIOGRATIS} (sin descuento, pero el envío no se cobra).</li>
 *   <li><b>Envío</b>: 9,90, gratis si el importe neto llega a 100 o con el cupón {@code ENVIOGRATIS}. Un carrito vacío no paga envío.</li>
 *   <li><b>IVA</b>: 16 % sobre el importe neto.</li>
 *   <li>Todos los importes se redondean a 2 decimales (mitad hacia arriba).</li>
 * </ol>
 */
@Service
public class PricingService {

    private static final BigDecimal SHIPPING_FEE = new BigDecimal("9.90");
    private static final BigDecimal FREE_SHIPPING_FROM = new BigDecimal("100.00");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.16");
    private static final BigDecimal CHRISTMAS_MINIMUM = new BigDecimal("200.00");

    private final Clock clock;

    /**
     * Crea el servicio.
     *
     * @param clock reloj (las pruebas lo fijan en una fecha concreta para probar cupones de temporada)
     */
    public PricingService(Clock clock) {
        this.clock = clock;
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Descuento por volumen de una línea.
     *
     * @param line línea del pedido
     * @return importe del descuento (cero si no llega al mínimo)
     */
    public BigDecimal volumeDiscount(OrderLine line) {
        BigDecimal rate = line.quantity() >= 50 ? new BigDecimal("0.10") : line.quantity() >= 10 ? new BigDecimal("0.05") : BigDecimal.ZERO;
        return money(line.total().multiply(rate));
    }

    /**
     * Calcula el desglose de una compra.
     *
     * @param customer cliente
     * @param lines líneas del pedido
     * @param couponCode cupón, o {@code null} si no usa ninguno
     * @return el desglose completo
     * @throws CouponException si el cupón no existe o no se puede aplicar a esta compra
     */
    public Quote quote(Customer customer, List<OrderLine> lines, String couponCode) {
        BigDecimal subtotal = money(lines.stream().map(OrderLine::total).reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal volume = lines.stream().map(this::volumeDiscount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal afterVolume = subtotal.subtract(volume);

        BigDecimal couponRate = BigDecimal.ZERO;
        boolean freeShippingCoupon = false;
        String applied = null;
        if (couponCode != null && !couponCode.isBlank()) {
            applied = couponCode.trim().toUpperCase();
            switch (applied) {
                case "BIENVENIDO10" -> {
                    if (!customer.firstPurchase()) {
                        throw new CouponException("El cupón BIENVENIDO10 es solo para la primera compra");
                    }
                    couponRate = new BigDecimal("0.10");
                }
                case "VIP20" -> {
                    if (!customer.vip()) {
                        throw new CouponException("El cupón VIP20 es solo para clientes VIP");
                    }
                    couponRate = new BigDecimal("0.20");
                }
                case "NAVIDAD25" -> {
                    if (LocalDate.now(clock).getMonth() != Month.DECEMBER) {
                        throw new CouponException("El cupón NAVIDAD25 solo es válido en diciembre");
                    }
                    if (afterVolume.compareTo(CHRISTMAS_MINIMUM) < 0) {
                        throw new CouponException("El cupón NAVIDAD25 requiere una compra mínima de 200.00");
                    }
                    couponRate = new BigDecimal("0.25");
                }
                case "ENVIOGRATIS" -> freeShippingCoupon = true;
                default -> throw new CouponException("El cupón " + applied + " no existe");
            }
        }
        BigDecimal couponDiscount = money(afterVolume.multiply(couponRate));
        BigDecimal net = afterVolume.subtract(couponDiscount);
        BigDecimal shipping = lines.isEmpty() || freeShippingCoupon || net.compareTo(FREE_SHIPPING_FROM) >= 0 ? money(BigDecimal.ZERO) : SHIPPING_FEE;
        BigDecimal tax = money(net.multiply(TAX_RATE));
        return new Quote(subtotal, volume, couponDiscount, net, shipping, tax, net.add(tax).add(shipping), applied);
    }
}
