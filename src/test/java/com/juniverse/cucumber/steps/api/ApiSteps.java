package com.juniverse.cucumber.steps.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.jayway.jsonpath.JsonPath;
import com.juniverse.cucumber.service.CartService;
import com.juniverse.cucumber.repository.ProductCatalog;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Pasos de la capa de integración: cada frase se traduce en una petición HTTP a la aplicación real (con su contexto de Spring, validaciones
 * y manejo de errores) y comprueba la respuesta. Los pasos son beans de Spring: reciben por inyección el catálogo y el estado del escenario.
 */
public class ApiSteps {

    private final MockMvc mvc;
    private final ProductCatalog catalog;
    private final CartService carts;
    private final ApiWorld world;

    /**
     * Crea los pasos con sus dependencias inyectadas por Spring.
     *
     * @param context contexto web de la aplicación
     * @param catalog catálogo de productos
     * @param carts servicio de carritos
     * @param world estado del escenario
     */
    @Autowired
    public ApiSteps(WebApplicationContext context, ProductCatalog catalog, CartService carts, ApiWorld world) {
        this.mvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.catalog = catalog;
        this.carts = carts;
        this.world = world;
    }

    private void send(MockHttpServletRequestBuilder request, String body) throws Exception {
        MvcResult result = mvc.perform(request).andReturn();
        world.lastResponse(result, request.toString() + (body == null ? "" : "\n" + body));
    }

    private void sendJson(MockHttpServletRequestBuilder request, String json) throws Exception {
        send(request.contentType(MediaType.APPLICATION_JSON).content(json), json);
    }

    private String body() throws Exception {
        return world.lastResponse().getResponse().getContentAsString();
    }

    // ------------------------------------------------------------------ Dado
    @Dado("el siguiente inventario")
    public void elSiguienteInventario(DataTable table) {
        for (Map<String, String> row : table.asMaps()) {
            catalog.define(row.get("sku"), row.get("nombre"), new BigDecimal(row.get("precio")), Integer.parseInt(row.get("existencias")));
        }
    }

    @Dado("un carrito nuevo para el cliente {string}")
    public void unCarritoNuevo(String customer) throws Exception {
        createCart(customer, false, false);
    }

    @Dado("un carrito nuevo para el cliente {string} que es VIP")
    public void unCarritoNuevoVip(String customer) throws Exception {
        createCart(customer, true, false);
    }

    @Dado("un carrito nuevo para el cliente {string} que es su primera compra")
    public void unCarritoNuevoPrimeraCompra(String customer) throws Exception {
        createCart(customer, false, true);
    }

    private void createCart(String customer, boolean vip, boolean first) throws Exception {
        sendJson(post("/api/carts"), "{\"customer\":\"" + customer + "\",\"vip\":" + vip + ",\"firstPurchase\":" + first + "}");
        assertEquals(201, world.lastResponse().getResponse().getStatus(), "no se pudo crear el carrito: " + body());
        world.cartId(JsonPath.read(body(), "$.id"));
    }

    @Dado("que otro cliente compra {int} unidades de {string}")
    public void otroClienteCompra(int quantity, String sku) {
        // Simula una compra concurrente directamente sobre el servicio: el escenario no necesita saber cómo
        var other = carts.create(new com.juniverse.cucumber.model.Customer("otro", false, false));
        carts.addItem(other.id(), sku, quantity);
        carts.checkout(other.id());
    }

    // ------------------------------------------------------------------ Cuando
    @Cuando("agrega {int} unidad(es) de {string}")
    @Dado("que agrega {int} unidad(es) de {string}")
    public void agregaUnidades(int quantity, String sku) throws Exception {
        sendJson(post("/api/carts/" + world.cartId() + "/items"), "{\"sku\":\"" + sku + "\",\"quantity\":" + quantity + "}");
    }

    @Cuando("quita {string} del carrito")
    public void quitaDelCarrito(String sku) throws Exception {
        send(delete("/api/carts/" + world.cartId() + "/items/" + sku), null);
    }

    @Cuando("aplica el cupón {string}")
    @Dado("que aplica el cupón {string}")
    public void aplicaElCupon(String code) throws Exception {
        sendJson(put("/api/carts/" + world.cartId() + "/coupon"), "{\"code\":\"" + code + "\"}");
    }

    @Cuando("quita el cupón")
    public void quitaElCupon() throws Exception {
        send(delete("/api/carts/" + world.cartId() + "/coupon"), null);
    }

    @Cuando("consulta el carrito")
    public void consultaElCarrito() throws Exception {
        send(get("/api/carts/" + world.cartId()), null);
    }

    @Cuando("consulta el carrito {string}")
    public void consultaElCarritoPorId(String id) throws Exception {
        send(get("/api/carts/" + id), null);
    }

    @Cuando("confirma la compra")
    @Dado("que confirma la compra")
    public void confirmaLaCompra() throws Exception {
        send(post("/api/carts/" + world.cartId() + "/checkout"), null);
    }

    @Cuando("consulta el catálogo")
    public void consultaElCatalogo() throws Exception {
        send(get("/api/products"), null);
    }

    /** El cuerpo JSON viene como "DocString": el texto entre triples comillas del escenario. */
    @Cuando("envía la petición para crear un carrito con el cuerpo:")
    public void enviaLaPeticionParaCrearUnCarrito(String json) throws Exception {
        sendJson(post("/api/carts"), json);
    }

    // ------------------------------------------------------------------ Entonces
    @Entonces("la respuesta tiene el código {int}")
    public void laRespuestaTieneElCodigo(int status) throws Exception {
        assertEquals(status, world.lastResponse().getResponse().getStatus(), () -> "cuerpo de la respuesta: " + safeBody());
    }

    private String safeBody() {
        try {
            return body();
        } catch (Exception e) {
            return "(ilegible)";
        }
    }

    @Y("el mensaje de error contiene {string}")
    public void elMensajeDeErrorContiene(String text) throws Exception {
        String detail = JsonPath.read(body(), "$.detail");
        assertTrue(detail.contains(text), () -> "el mensaje era: " + detail);
    }

    @Y("la respuesta contiene el cliente {string}")
    public void laRespuestaContieneElCliente(String customer) throws Exception {
        assertEquals(customer, JsonPath.read(body(), "$.customer"));
    }

    @Y("el carrito tiene {int} productos distintos")
    public void elCarritoTieneProductosDistintos(int count) throws Exception {
        consultaElCarrito();
        List<Object> items = JsonPath.read(body(), "$.items");
        assertEquals(count, items.size());
    }

    @Entonces("el carrito contiene {int} unidades de {string}")
    public void elCarritoContieneUnidades(int quantity, String sku) throws Exception {
        consultaElCarrito();
        List<Integer> found = JsonPath.read(body(), "$.items[?(@.sku=='" + sku + "')].quantity");
        assertEquals(List.of(quantity), found);
    }

    @Y("el desglose del carrito es")
    public void elDesgloseDelCarritoEs(DataTable table) throws Exception {
        for (Map<String, String> row : table.asMaps()) {
            String field = switch (row.get("concepto")) {
                case "subtotal" -> "subtotal";
                case "importe neto" -> "net";
                case "envío" -> "shipping";
                case "IVA" -> "tax";
                case "total" -> "total";
                default -> throw new IllegalArgumentException("concepto desconocido: " + row.get("concepto"));
            };
            assertMoney(row.get("concepto"), row.get("importe"), JsonPath.read(body(), "$.quote." + field));
        }
    }

    @Y("el total del carrito es {dinero}")
    public void elTotalDelCarritoEs(BigDecimal expected) throws Exception {
        consultaElCarrito();
        assertMoney("total", expected.toPlainString(), JsonPath.read(body(), "$.quote.total"));
    }

    @Y("el carrito tiene aplicado el cupón {string}")
    public void elCarritoTieneAplicadoElCupon(String code) throws Exception {
        assertEquals(code, JsonPath.read(body(), "$.coupon"));
    }

    @Entonces("el carrito no tiene cupón")
    public void elCarritoNoTieneCupon() throws Exception {
        consultaElCarrito();
        Object coupon = JsonPath.read(body(), "$.coupon");
        assertNull(coupon, () -> "el carrito tiene el cupón " + coupon);
    }

    @Y("la compra tiene un número de pedido")
    public void laCompraTieneUnNumeroDePedido() throws Exception {
        String orderId = JsonPath.read(body(), "$.orderId");
        assertNotNull(orderId);
        assertTrue(orderId.startsWith("PED-"), orderId);
    }

    @Y("el total cobrado es {dinero}")
    public void elTotalCobradoEs(BigDecimal expected) throws Exception {
        assertMoney("total cobrado", expected.toPlainString(), JsonPath.read(body(), "$.quote.total"));
    }

    @Y("las existencias de {string} quedan en {int}")
    public void lasExistenciasQuedanEn(String sku, int stock) {
        assertEquals(stock, catalog.find(sku).orElseThrow().stock(), "existencias de " + sku);
    }

    @Entonces("el catálogo muestra {string} con {int} existencias")
    public void elCatalogoMuestra(String sku, int stock) throws Exception {
        List<Integer> found = JsonPath.read(body(), "$[?(@.sku=='" + sku + "')].stock");
        assertEquals(List.of(stock), found);
    }

    private static void assertMoney(String what, String expected, Number actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(new BigDecimal(actual.toString())), what + ": se esperaba " + expected + " pero fue " + actual);
    }
}
