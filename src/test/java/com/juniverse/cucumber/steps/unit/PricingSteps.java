package com.juniverse.cucumber.steps.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.juniverse.cucumber.pricing.OrderLine;
import com.juniverse.cucumber.pricing.Quote;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Pasos de las pruebas "unitarias" con Cucumber: ejercitan {@code PricingService} directamente, SIN Spring ni HTTP. Son tan rápidas como
 * cualquier prueba JUnit (milisegundos), pero se leen como una especificación del negocio.
 *
 * <p>Cada método está enlazado a una frase del archivo {@code .feature} mediante una <i>expresión de Cucumber</i>:
 * {@code {int}}, {@code {string}} y los tipos propios {@code {dinero}} y {@code {fecha}} capturan los datos de la frase.</p>
 */
public class PricingSteps {

    private final PricingWorld world;

    /**
     * Crea los pasos; PicoContainer inyecta el mundo del escenario.
     *
     * @param world estado compartido del escenario
     */
    public PricingSteps(PricingWorld world) {
        this.world = world;
    }

    // ------------------------------------------------------------------ Dado
    @Dado("el catálogo de productos")
    public void elCatalogoDeProductos(DataTable table) {
        for (Map<String, String> row : table.asMaps()) {
            world.prices.put(row.get("sku"), new BigDecimal(row.get("precio")));
        }
    }

    @Dado("el producto {string} a {dinero}")
    public void elProductoAPrecio(String sku, BigDecimal price) {
        world.prices.put(sku, price);
    }

    @Dado("un cliente {string}")
    public void unCliente(String name) {
        world.customerName = name;
    }

    @Y("que el cliente es VIP")
    public void queElClienteEsVip() {
        world.vip = true;
    }

    @Y("que es su primera compra")
    public void queEsSuPrimeraCompra() {
        world.firstPurchase = true;
    }

    @Y("que hoy es {fecha}")
    public void queHoyEs(LocalDate date) {
        world.today = date;
    }

    @Dado("que el carrito contiene")
    public void queElCarritoContiene(DataTable table) {
        for (Map<String, String> row : table.asMaps()) {
            addLine(row.get("producto"), Integer.parseInt(row.get("cantidad")));
        }
    }

    private void addLine(String sku, int quantity) {
        BigDecimal price = world.prices.get(sku);
        assertNotNull(price, "el producto " + sku + " no está en el catálogo del escenario");
        world.lines.add(new OrderLine(sku, quantity, price));
    }

    // ------------------------------------------------------------------ Cuando
    @Cuando("compra {int} unidades de {string}")
    public void compraUnidades(int quantity, String sku) {
        addLine(sku, quantity);
        world.calculate();
    }

    @Cuando("aplica el cupón {string}")
    public void aplicaElCupon(String code) {
        world.coupon = code;
    }

    @Cuando("calcula el total")
    public void calculaElTotal() {
        world.calculate();
    }

    // ------------------------------------------------------------------ Entonces
    private Quote quote() {
        Quote quote = world.quoteOrCalculate();
        assertNotNull(quote, "no hay resultado: " + (world.couponError == null ? "no se calculó" : "el cupón fue rechazado: " + world.couponError.getMessage()));
        return quote;
    }

    private static void assertMoney(String what, BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual), what + ": se esperaba " + expected + " pero fue " + actual);
    }

    @Entonces("el subtotal es {dinero}")
    public void elSubtotalEs(BigDecimal expected) {
        assertMoney("subtotal", expected, quote().subtotal());
    }

    @Y("el descuento por volumen es {dinero}")
    public void elDescuentoPorVolumenEs(BigDecimal expected) {
        assertMoney("descuento por volumen", expected, quote().volumeDiscount());
    }

    @Y("el descuento por cupón es {dinero}")
    public void elDescuentoPorCuponEs(BigDecimal expected) {
        assertMoney("descuento por cupón", expected, quote().couponDiscount());
    }

    @Y("el importe neto es {dinero}")
    public void elImporteNetoEs(BigDecimal expected) {
        assertMoney("importe neto", expected, quote().net());
    }

    @Entonces("el envío cuesta {dinero}")
    public void elEnvioCuesta(BigDecimal expected) {
        assertMoney("envío", expected, quote().shipping());
    }

    @Y("el total a pagar es {dinero}")
    public void elTotalAPagarEs(BigDecimal expected) {
        assertMoney("total", expected, quote().total());
    }

    @Entonces("se aplica el cupón {string}")
    public void seAplicaElCupon(String code) {
        assertNull(world.couponError, () -> "el cupón fue rechazado: " + world.couponError.getMessage());
        assertEquals(code, quote().coupon());
    }

    @Entonces("el cupón es rechazado con el motivo {string}")
    public void elCuponEsRechazadoConElMotivo(String reason) {
        assertNotNull(world.couponError, "el cupón debía rechazarse pero se aceptó");
        assertTrue(world.couponError.getMessage().contains(reason), () -> "el motivo era \"" + world.couponError.getMessage() + "\"");
    }

    @Entonces("el resultado del cupón es {string}")
    public void elResultadoDelCuponEs(String expected) {
        if (expected.equals("aplicado")) {
            assertNull(world.couponError, () -> "el cupón fue rechazado: " + world.couponError.getMessage());
            assertNotNull(world.quote);
        } else {
            assertNotNull(world.couponError, "el cupón debía rechazarse pero se aceptó");
            assertTrue(world.couponError.getMessage().contains(expected), () -> "el motivo era \"" + world.couponError.getMessage() + "\"");
        }
    }

    /** Compara una tabla concepto/importe con el desglose completo: una sola frase verifica varios valores a la vez. */
    @Entonces("el desglose es")
    public void elDesgloseEs(DataTable table) {
        Quote quote = quote();
        Map<String, BigDecimal> actual = Map.of(
                "subtotal", quote.subtotal(), "descuento volumen", quote.volumeDiscount(), "descuento cupón", quote.couponDiscount(),
                "importe neto", quote.net(), "envío", quote.shipping(), "IVA", quote.tax(), "total", quote.total());
        List<Map<String, String>> rows = table.asMaps();
        for (Map<String, String> row : rows) {
            BigDecimal value = actual.get(row.get("concepto"));
            assertNotNull(value, "concepto desconocido: " + row.get("concepto"));
            assertMoney(row.get("concepto"), new BigDecimal(row.get("importe")), value);
        }
    }
}
