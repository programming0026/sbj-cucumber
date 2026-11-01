package com.juniverse.cucumber.steps.api;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import com.juniverse.cucumber.repository.ProductCatalog;

/** Ganchos de la capa de API: restablecen el estado compartido y dejan evidencia en el informe. */
public class ApiHooks {

    private final ProductCatalog catalog;
    private final ApiWorld world;

    /**
     * Crea los ganchos.
     *
     * @param catalog catálogo (singleton del contexto de Spring: hay que restablecerlo entre escenarios)
     * @param world estado del escenario
     */
    @Autowired
    public ApiHooks(ProductCatalog catalog, ApiWorld world) {
        this.catalog = catalog;
        this.world = world;
    }

    /** Antes de cada escenario de API el catálogo vuelve a su estado inicial; así el orden de ejecución no importa. */
    @Before(value = "@api", order = 0)
    public void resetCatalog() {
        catalog.reset();
    }

    /**
     * Después de cada escenario adjunta la última petición y respuesta al informe HTML: al investigar un fallo se ve exactamente qué se
     * envió y qué se recibió.
     *
     * @param scenario escenario en curso
     * @throws Exception si no se puede leer la respuesta
     */
    @After("@api")
    public void attachEvidence(Scenario scenario) throws Exception {
        if (world.lastResponse() != null) {
            scenario.attach(world.lastRequest(), "text/plain", "Última petición");
            scenario.attach("HTTP " + world.lastResponse().getResponse().getStatus() + "\n" + world.lastResponse().getResponse().getContentAsString(),
                    "text/plain", "Última respuesta");
        }
    }
}
