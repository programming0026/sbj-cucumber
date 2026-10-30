package com.juniverse.cucumber.steps.common;

import io.cucumber.java.ParameterType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tipos de parámetro propios: permiten escribir {@code {dinero}} o {@code {fecha}} en la frase de un paso y recibir ya un
 * {@link BigDecimal} o un {@link LocalDate}. Así los escenarios dicen "el total es 23.20" y el código no repite conversiones.
 */
public class ParameterTypes {

    /**
     * Importe: número con o sin decimales (con punto), por ejemplo {@code 20}, {@code 20.5} o {@code 1661.12}.
     *
     * @param text texto del importe
     * @return el importe
     */
    @ParameterType("\\d+(?:\\.\\d+)?")
    public BigDecimal dinero(String text) {
        return new BigDecimal(text);
    }

    /**
     * Fecha en formato ISO {@code aaaa-mm-dd}.
     *
     * @param text texto de la fecha
     * @return la fecha
     */
    @ParameterType("\\d{4}-\\d{2}-\\d{2}")
    public LocalDate fecha(String text) {
        return LocalDate.parse(text);
    }
}
