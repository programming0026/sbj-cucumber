package com.juniverse.cucumber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Tienda en línea de ejemplo. Lo importante de este proyecto NO es la tienda sino cómo se prueba: con <b>Cucumber</b> (BDD), donde las
 * pruebas se escriben como escenarios en lenguaje natural que entiende todo el equipo, no solo quien programa.
 */
@SpringBootApplication
public class CucumberApplication {

    /**
     * Arranca la aplicación.
     *
     * @param args argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(CucumberApplication.class, args);
    }
}
