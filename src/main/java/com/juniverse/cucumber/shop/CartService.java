package com.juniverse.cucumber.shop;

import com.juniverse.cucumber.pricing.Customer;
import com.juniverse.cucumber.pricing.OrderLine;
import com.juniverse.cucumber.pricing.PricingService;
import com.juniverse.cucumber.pricing.Quote;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Carritos de compra: agregar y quitar productos, aplicar un cupón y confirmar la compra. */
@Service
public class CartService {

    /** Estado de un carrito. */
    public static final class Cart {

        final String id = UUID.randomUUID().toString().substring(0, 8);
        final Customer customer;
        final Map<String, Integer> quantities = new LinkedHashMap<>();
        String coupon;
        boolean checkedOut;

        Cart(Customer customer) {
            this.customer = customer;
        }

        public String id() {
            return id;
        }

        public Customer customer() {
            return customer;
        }

        public String coupon() {
            return coupon;
        }

        public boolean checkedOut() {
            return checkedOut;
        }

        public Map<String, Integer> quantities() {
            return Map.copyOf(quantities);
        }
    }

    /**
     * Compra confirmada.
     *
     * @param orderId número de pedido
     * @param customer cliente
     * @param lines líneas compradas
     * @param quote desglose cobrado
     */
    public record Order(String orderId, String customer, List<OrderLine> lines, Quote quote) {
    }

    private final ProductCatalog catalog;
    private final PricingService pricing;
    private final Map<String, Cart> carts = new ConcurrentHashMap<>();

    /**
     * Crea el servicio.
     *
     * @param catalog catálogo
     * @param pricing reglas de precios
     */
    public CartService(ProductCatalog catalog, PricingService pricing) {
        this.catalog = catalog;
        this.pricing = pricing;
    }

    /**
     * Crea un carrito.
     *
     * @param customer cliente
     * @return el carrito
     */
    public Cart create(Customer customer) {
        Cart cart = new Cart(customer);
        carts.put(cart.id, cart);
        return cart;
    }

    /**
     * Busca un carrito.
     *
     * @param id identificador
     * @return el carrito
     * @throws ShopException 404 si no existe
     */
    public Cart get(String id) {
        Cart cart = carts.get(id);
        if (cart == null) {
            throw new ShopException(HttpStatus.NOT_FOUND, "No existe el carrito " + id);
        }
        return cart;
    }

    private Cart open(String id) {
        Cart cart = get(id);
        if (cart.checkedOut) {
            throw new ShopException(HttpStatus.CONFLICT, "El carrito " + id + " ya fue confirmado");
        }
        return cart;
    }

    /**
     * Agrega unidades de un producto, comprobando que alcancen las existencias.
     *
     * @param cartId carrito
     * @param sku producto
     * @param quantity unidades a agregar
     * @return el carrito actualizado
     * @throws ShopException 404 si no existe el producto, 409 si no hay existencias suficientes o el carrito ya se confirmó
     */
    public synchronized Cart addItem(String cartId, String sku, int quantity) {
        Cart cart = open(cartId);
        Product product = catalog.find(sku).orElseThrow(() -> new ShopException(HttpStatus.NOT_FOUND, "No existe el producto " + sku));
        int wanted = cart.quantities.getOrDefault(sku, 0) + quantity;
        if (wanted > product.stock()) {
            throw new ShopException(HttpStatus.CONFLICT, "No hay existencias suficientes de " + product.name() + ": quedan " + product.stock());
        }
        cart.quantities.put(sku, wanted);
        return cart;
    }

    /**
     * Quita un producto del carrito.
     *
     * @param cartId carrito
     * @param sku producto
     * @return el carrito actualizado
     */
    public synchronized Cart removeItem(String cartId, String sku) {
        Cart cart = open(cartId);
        if (cart.quantities.remove(sku) == null) {
            throw new ShopException(HttpStatus.NOT_FOUND, "El producto " + sku + " no está en el carrito");
        }
        return cart;
    }

    /**
     * Aplica un cupón (se valida en el momento).
     *
     * @param cartId carrito
     * @param code cupón, o {@code null} para quitarlo
     * @return el carrito actualizado
     * @throws ShopException 422 si el cupón no se puede aplicar
     */
    public synchronized Cart applyCoupon(String cartId, String code) {
        Cart cart = open(cartId);
        if (code != null) {
            try {
                pricing.quote(cart.customer, lines(cart), code);
            } catch (com.juniverse.cucumber.pricing.CouponException e) {
                throw new ShopException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
            }
        }
        cart.coupon = code == null ? null : code.trim().toUpperCase();
        return cart;
    }

    /**
     * Líneas del carrito con el precio actual de cada producto.
     *
     * @param cart carrito
     * @return líneas
     */
    public List<OrderLine> lines(Cart cart) {
        List<OrderLine> lines = new ArrayList<>();
        cart.quantities.forEach((sku, quantity) -> lines.add(new OrderLine(sku, quantity, catalog.find(sku).orElseThrow().price())));
        return lines;
    }

    /**
     * Calcula el desglose del carrito.
     *
     * @param cart carrito
     * @return desglose
     */
    public Quote quote(Cart cart) {
        return pricing.quote(cart.customer, lines(cart), cart.coupon);
    }

    /**
     * Confirma la compra: descuenta las existencias de TODOS los productos o de ninguno.
     *
     * @param cartId carrito
     * @return el pedido
     * @throws ShopException 422 si el carrito está vacío, 409 si ya se confirmó o las existencias cambiaron
     */
    public synchronized Order checkout(String cartId) {
        Cart cart = open(cartId);
        if (cart.quantities.isEmpty()) {
            throw new ShopException(HttpStatus.UNPROCESSABLE_ENTITY, "No se puede confirmar un carrito vacío");
        }
        List<String> reserved = new ArrayList<>();
        for (Map.Entry<String, Integer> line : cart.quantities.entrySet()) {
            if (!catalog.reserve(line.getKey(), line.getValue())) {
                // Algo se agotó desde que se agregó al carrito: se devuelve lo ya descontado para no dejar la compra a medias
                reserved.forEach(sku -> catalog.find(sku).ifPresent(p -> catalog.define(sku, p.name(), p.price(), p.stock() + cart.quantities.get(sku))));
                throw new ShopException(HttpStatus.CONFLICT, "Ya no hay existencias suficientes de " + line.getKey());
            }
            reserved.add(line.getKey());
        }
        cart.checkedOut = true;
        return new Order("PED-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(), cart.customer.name(), lines(cart), quote(cart));
    }
}
