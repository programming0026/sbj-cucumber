package com.juniverse.cucumber.steps.unit;

import com.juniverse.cucumber.exception.CouponException;
import com.juniverse.cucumber.model.Customer;
import com.juniverse.cucumber.model.OrderLine;
import com.juniverse.cucumber.service.PricingService;
import com.juniverse.cucumber.model.Quote;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * El "mundo" del escenario: el estado que comparten los pasos. PicoContainer crea UNA instancia por escenario y se la entrega a todas las
 * clases de pasos que la pidan en su constructor, así cada escenario empieza limpio y los escenarios no se afectan entre sí.
 */
public class PricingWorld {

    final Map<String, BigDecimal> prices = new HashMap<>();
    final List<OrderLine> lines = new ArrayList<>();
    String customerName = "cliente";
    boolean vip;
    boolean firstPurchase;
    String coupon;
    LocalDate today = LocalDate.of(2026, 6, 15);

    Quote quote;
    CouponException couponError;

    Customer customer() {
        return new Customer(customerName, vip, firstPurchase);
    }

    /** Calcula con las reglas reales de negocio y guarda el resultado o el error de cupón. */
    void calculate() {
        PricingService service = new PricingService(Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
        quote = null;
        couponError = null;
        try {
            quote = service.quote(customer(), lines, coupon);
        } catch (CouponException e) {
            couponError = e;
        }
    }

    /** Calcula si aún no se ha hecho, para los pasos que piden un resultado directamente (Cuando compra N unidades...). */
    Quote quoteOrCalculate() {
        if (quote == null && couponError == null) {
            calculate();
        }
        return quote;
    }
}
