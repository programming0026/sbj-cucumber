package com.juniverse.cucumber.steps.api;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Estado del escenario en la capa de API. {@code @ScenarioScope} hace que Spring cree una instancia NUEVA por escenario y la descarte al
 * terminar: es la forma de tener estado por escenario cuando los pasos son beans de Spring.
 */
@Component
@ScenarioScope
public class ApiWorld {

    private String cartId;
    private MvcResult lastResponse;
    private String lastRequest = "";

    public String cartId() {
        return cartId;
    }

    public void cartId(String cartId) {
        this.cartId = cartId;
    }

    public MvcResult lastResponse() {
        return lastResponse;
    }

    public void lastResponse(MvcResult lastResponse, String request) {
        this.lastResponse = lastResponse;
        this.lastRequest = request;
    }

    public String lastRequest() {
        return lastRequest;
    }
}
