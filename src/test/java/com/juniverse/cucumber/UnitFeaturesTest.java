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
 * Ejecuta los escenarios de {@code features/unit} SIN levantar Spring: solo la lógica de precios, con PicoContainer para compartir el
 * estado entre las clases de pasos. Es la capa rápida de la pirámide de pruebas.
 *
 * <p>Con JUnit Platform, Cucumber es un "motor" más: este {@code @Suite} le indica qué carpeta de escenarios leer y dónde buscar los pasos
 * (el "glue"). Se ejecuta con {@code mvn test} como cualquier otra prueba.</p>
 */
@Suite
@SuiteDisplayName("Pruebas unitarias con Cucumber (reglas de precios)")
@IncludeEngines("cucumber")
@SelectClasspathResource("features/unit")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.juniverse.cucumber.steps.common,com.juniverse.cucumber.steps.unit")
@ConfigurationParameter(key = OBJECT_FACTORY_PROPERTY_NAME, value = "io.cucumber.picocontainer.PicoFactory")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/unit.html, json:target/cucumber-reports/unit.json, junit:target/cucumber-reports/unit.xml")
public class UnitFeaturesTest {
}
