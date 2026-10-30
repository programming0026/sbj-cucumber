package com.juniverse.cucumber.steps.unit;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Ganchos (<i>hooks</i>) de las pruebas unitarias: código que Cucumber ejecuta antes y después de cada escenario. Sirven para preparar y
 * limpiar, y para añadir información al informe.
 */
public class UnitHooks {

    private final PricingWorld world;

    /**
     * Crea los ganchos.
     *
     * @param world estado del escenario
     */
    public UnitHooks(PricingWorld world) {
        this.world = world;
    }

    /**
     * Se ejecuta antes de cada escenario con la etiqueta {@code @unitario}.
     *
     * @param scenario escenario en curso
     */
    @Before("@unitario")
    public void prepare(Scenario scenario) {
        scenario.log("Escenario: " + scenario.getName());   // aparece en el informe HTML
    }

    /**
     * Se ejecuta después de cada escenario: adjunta al informe el desglose calculado para entender qué pasó.
     *
     * @param scenario escenario en curso
     */
    @After("@unitario")
    public void attachResult(Scenario scenario) {
        if (world.quote != null) {
            scenario.attach(world.quote.toString(), "text/plain", "Desglose calculado");
        }
        if (world.couponError != null) {
            scenario.attach(world.couponError.getMessage(), "text/plain", "Cupón rechazado");
        }
    }
}
