package com.juniverse.cucumber;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.OBJECT_FACTORY_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Ejecuta los escenarios de {@code features/api} contra la aplicación real: Cucumber levanta el contexto de Spring Boot
 * ({@code @CucumberContextConfiguration}) y los pasos lo usan por inyección. Es la capa de integración de la pirámide de pruebas.
 */
@Suite
@SuiteDisplayName("Pruebas de integración con Cucumber (API REST)")
@IncludeEngines("cucumber")
@SelectClasspathResource("features/api")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.juniverse.cucumber.steps.common,com.juniverse.cucumber.steps.api")
@ConfigurationParameter(key = OBJECT_FACTORY_PROPERTY_NAME, value = "io.cucumber.spring.SpringFactory")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/api.html, json:target/cucumber-reports/api.json, junit:target/cucumber-reports/api.xml")
public class ApiFeaturesTest {
}
