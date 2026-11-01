package com.juniverse.cucumber.model;

/**
 * Cliente que compra.
 *
 * @param name nombre
 * @param vip si es cliente VIP
 * @param firstPurchase si es su primera compra
 */
public record Customer(String name, boolean vip, boolean firstPurchase) {
}
