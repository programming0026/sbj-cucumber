package com.juniverse.cucumber.shop;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Catálogo con existencias, en memoria. Las pruebas lo restablecen antes de cada escenario con {@link #reset()}. */
@Component
public class ProductCatalog {

    private record Entry(String name, BigDecimal price, int stock) {
    }

    private final Map<String, Entry> products = new ConcurrentHashMap<>();

    /** Crea el catálogo con los datos iniciales. */
    public ProductCatalog() {
        reset();
    }

    /** Restablece el catálogo: LAPTOP 800.00 (10), MOUSE 20.00 (100), TECLADO 50.00 (25) y MONITOR 300.00 (5). */
    public void reset() {
        products.clear();
        define("LAPTOP", "Laptop", new BigDecimal("800.00"), 10);
        define("MOUSE", "Mouse", new BigDecimal("20.00"), 100);
        define("TECLADO", "Teclado", new BigDecimal("50.00"), 25);
        define("MONITOR", "Monitor", new BigDecimal("300.00"), 5);
    }

    /**
     * Define o reemplaza un producto.
     *
     * @param sku código
     * @param name nombre
     * @param price precio
     * @param stock existencias
     */
    public void define(String sku, String name, BigDecimal price, int stock) {
        products.put(sku, new Entry(name, price, stock));
    }

    /**
     * Busca un producto.
     *
     * @param sku código
     * @return el producto, si existe
     */
    public Optional<Product> find(String sku) {
        Entry entry = products.get(sku);
        return entry == null ? Optional.empty() : Optional.of(new Product(sku, entry.name(), entry.price(), entry.stock()));
    }

    /**
     * Todos los productos.
     *
     * @return productos ordenados por código
     */
    public List<Product> findAll() {
        return products.keySet().stream().sorted().map(sku -> find(sku).orElseThrow()).sorted(Comparator.comparing(Product::sku)).toList();
    }

    /**
     * Descuenta existencias de forma atómica.
     *
     * @param sku código
     * @param quantity unidades
     * @return {@code true} si había existencias suficientes y se descontaron
     */
    public boolean reserve(String sku, int quantity) {
        boolean[] reserved = {false};
        products.computeIfPresent(sku, (key, entry) -> {
            if (entry.stock() < quantity) {
                return entry;
            }
            reserved[0] = true;
            return new Entry(entry.name(), entry.price(), entry.stock() - quantity);
        });
        return reserved[0];
    }
}
