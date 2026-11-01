package com.juniverse.cucumber.controller;

import com.juniverse.cucumber.model.Customer;
import com.juniverse.cucumber.model.OrderLine;
import com.juniverse.cucumber.model.Quote;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.juniverse.cucumber.model.Product;
import com.juniverse.cucumber.repository.ProductCatalog;
import com.juniverse.cucumber.service.CartService;

/** API REST de la tienda, la que ejercitan los escenarios Cucumber de la capa de integración. */
@RestController
@RequestMapping("/api")
public class ShopController {

    /**
     * Datos para crear un carrito.
     *
     * @param customer cliente
     * @param vip si es VIP
     * @param firstPurchase si es su primera compra
     */
    public record CreateCartRequest(@NotBlank(message = "El cliente es obligatorio") String customer, boolean vip, boolean firstPurchase) {
    }

    /**
     * Producto a agregar.
     *
     * @param sku código
     * @param quantity unidades
     */
    public record AddItemRequest(@NotBlank(message = "El producto es obligatorio") String sku, @Min(value = 1, message = "La cantidad mínima es 1") int quantity) {
    }

    /**
     * Cupón a aplicar.
     *
     * @param code código del cupón
     */
    public record CouponRequest(@NotBlank(message = "El cupón es obligatorio") String code) {
    }

    /**
     * Línea del carrito para mostrar.
     *
     * @param sku código
     * @param name nombre
     * @param quantity unidades
     * @param unitPrice precio unitario
     * @param lineTotal importe de la línea
     */
    public record ItemView(String sku, String name, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }

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

    private final CartService carts;
    private final ProductCatalog catalog;

    /**
     * Crea el controlador.
     *
     * @param carts servicio de carritos
     * @param catalog catálogo
     */
    public ShopController(CartService carts, ProductCatalog catalog) {
        this.carts = carts;
        this.catalog = catalog;
    }

    private CartView view(CartService.Cart cart) {
        List<ItemView> items = carts.lines(cart).stream()
                .map(line -> new ItemView(line.sku(), catalog.find(line.sku()).orElseThrow().name(), line.quantity(), line.unitPrice(), line.total())).toList();
        return new CartView(cart.id(), cart.customer().name(), items, cart.coupon(), carts.quote(cart));
    }

    /**
     * Catálogo con existencias.
     *
     * @return productos
     */
    @GetMapping("/products")
    public List<Product> products() {
        return catalog.findAll();
    }

    /**
     * Crea un carrito.
     *
     * @param request datos validados
     * @return 201 con el carrito
     */
    @PostMapping("/carts")
    public ResponseEntity<CartView> create(@Valid @RequestBody CreateCartRequest request) {
        CartService.Cart cart = carts.create(new Customer(request.customer(), request.vip(), request.firstPurchase()));
        return ResponseEntity.created(URI.create("/api/carts/" + cart.id())).body(view(cart));
    }

    /**
     * Consulta un carrito con su desglose de precios.
     *
     * @param id carrito
     * @return carrito
     */
    @GetMapping("/carts/{id}")
    public CartView get(@PathVariable String id) {
        return view(carts.get(id));
    }

    /**
     * Agrega un producto.
     *
     * @param id carrito
     * @param request producto y cantidad
     * @return carrito actualizado
     */
    @PostMapping("/carts/{id}/items")
    public CartView addItem(@PathVariable String id, @Valid @RequestBody AddItemRequest request) {
        return view(carts.addItem(id, request.sku(), request.quantity()));
    }

    /**
     * Quita un producto.
     *
     * @param id carrito
     * @param sku producto
     * @return carrito actualizado
     */
    @DeleteMapping("/carts/{id}/items/{sku}")
    public CartView removeItem(@PathVariable String id, @PathVariable String sku) {
        return view(carts.removeItem(id, sku));
    }

    /**
     * Aplica un cupón.
     *
     * @param id carrito
     * @param request cupón
     * @return carrito actualizado
     */
    @PutMapping("/carts/{id}/coupon")
    public CartView applyCoupon(@PathVariable String id, @Valid @RequestBody CouponRequest request) {
        return view(carts.applyCoupon(id, request.code()));
    }

    /**
     * Quita el cupón.
     *
     * @param id carrito
     * @return carrito actualizado
     */
    @DeleteMapping("/carts/{id}/coupon")
    public CartView removeCoupon(@PathVariable String id) {
        return view(carts.applyCoupon(id, null));
    }

    /**
     * Confirma la compra.
     *
     * @param id carrito
     * @return 201 con el pedido
     */
    @PostMapping("/carts/{id}/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@PathVariable String id) {
        CartService.Order order = carts.checkout(id);
        List<OrderLine> lines = order.lines();
        return ResponseEntity.status(201).body(Map.of("orderId", order.orderId(), "customer", order.customer(), "items", lines, "quote", order.quote()));
    }
}
