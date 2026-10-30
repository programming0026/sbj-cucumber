package com.juniverse.cucumber.steps.api;

import com.juniverse.cucumber.CucumberApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Le dice a Cucumber que levante el contexto de Spring Boot de la aplicación y que inyecte sus beans en los pasos. Hay UNA sola clase
 * con esta anotación; el contexto se crea una vez y se reutiliza en todos los escenarios (rápido), y el estado de cada escenario se
 * limpia con ganchos y con beans de ámbito {@code @ScenarioScope}.
 */
@CucumberContextConfiguration
@SpringBootTest(classes = CucumberApplication.class)
public class CucumberSpringConfiguration {
}
