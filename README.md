# Pruebas BDD con Cucumber en Spring Boot 4

Ejemplo con **Spring Boot 4**, **Java 21** y **Cucumber 8** (sobre JUnit 6) donde las pruebas se escriben como **escenarios en español** que entiende cualquier persona del equipo —analista, QA, producto— y no solo quien programa. Las mismas especificaciones sirven como documentación viva y como pruebas automáticas.

## ¿Qué es BDD y qué aporta Cucumber?

*Behaviour-Driven Development*: primero se describe el **comportamiento** del sistema con ejemplos concretos, de forma que negocio y desarrollo hablen el mismo idioma. Cucumber ejecuta esos ejemplos.

```gherkin
# language: es
Esquema del escenario: El descuento depende de la cantidad comprada
  Cuando compra <cantidad> unidades de "MOUSE"
  Entonces el subtotal es <subtotal>
  Y el descuento por volumen es <descuento>

  Ejemplos: Descuento del 5 %
    | cantidad | subtotal | descuento |
    | 10       | 200.00   | 10.00     |
    | 49       | 980.00   | 49.00     |
```

```java
@Cuando("compra {int} unidades de {string}")     // la frase del escenario ↔ un método de Java
public void compraUnidades(int quantity, String sku) { ... }
```

| Pieza | Qué es | Dónde está |
|-------|--------|------------|
| **Feature** (`.feature`) | La especificación en lenguaje natural (Gherkin) | `src/test/resources/features/` |
| **Step definitions** | El código Java que ejecuta cada frase | `src/test/java/.../steps/` |
| **Runner** | Una clase `@Suite` que le dice a JUnit qué carpeta de escenarios ejecutar | `UnitFeaturesTest`, `ApiFeaturesTest` |
| **Informe** | HTML/JSON/XML con cada escenario, sus pasos y la evidencia | `target/cucumber-reports/` |

## El dominio: una tienda en línea

Las reglas de negocio (`PricingService`) que describen las features:

| Regla | Detalle |
|-------|---------|
| Descuento por volumen | Por línea: 5 % desde 10 unidades, 10 % desde 50 |
| Cupones | `BIENVENIDO10` (10 %, solo primera compra), `VIP20` (20 %, solo VIP), `NAVIDAD25` (25 %, solo en diciembre y compra mínima de 200 **después** del descuento por volumen), `ENVIOGRATIS` |
| Envío | 9,90; gratis desde 100 de importe neto o con `ENVIOGRATIS`; un carrito vacío no paga envío |
| IVA | 16 % del importe neto |
| Compra | Descuenta el stock de **todos** los productos o de ninguno; un carrito confirmado no se modifica |

## Dos capas de pruebas, una pirámide

| | `features/unit` | `features/api` |
|---|---|---|
| Qué ejercita | `PricingService` directamente | La aplicación completa: HTTP, validación, errores, estado |
| Spring | **No se levanta** | `@CucumberContextConfiguration` + `@SpringBootTest` (un solo contexto para toda la suite) |
| Estado compartido entre pasos | **PicoContainer** (`PricingWorld`, una instancia por escenario) | Beans `@ScenarioScope` (`ApiWorld`) |
| Velocidad | Milisegundos (30 escenarios en ~0,6 s) | Segundos (el contexto se arranca una vez) |
| Cuándo | Reglas de negocio con muchas combinaciones | Flujos de extremo a extremo y contratos de la API |

Cada capa tiene su propio runner con su propio *glue* (el paquete donde Cucumber busca los pasos) y su propia fábrica de objetos (`OBJECT_FACTORY_PROPERTY_NAME`): así la capa rápida nunca paga el costo de arrancar Spring.

## Lo que muestra el ejemplo

| Característica de Cucumber | Dónde verla |
|----------------------------|-------------|
| **Gherkin en español** (`# language: es`, `Característica`, `Escenario`, `Dado/Cuando/Entonces/Y`) | todas las features |
| **`Antecedentes`** (Background) para el contexto común | todas las features |
| **`Esquema del escenario` + `Ejemplos`** (varios casos con una sola plantilla; hasta con ejemplos nombrados) | `volumen.feature`, `cupones.feature` |
| **`Regla`** (agrupa escenarios por regla de negocio) | `cupones.feature`, `carrito.feature` |
| **DataTable** como entrada (`| producto | cantidad |`) y como verificación de un desglose completo | `PricingSteps.elDesgloseEs` |
| **DocString** (`"""json … """`) para un cuerpo de petición | `carrito.feature` |
| **Tipos de parámetro propios**: `{dinero}` → `BigDecimal`, `{fecha}` → `LocalDate` | `ParameterTypes` |
| **Etiquetas** (`@unitario`, `@api`, `@cupones`…) para filtrar y para enganchar *hooks* | features y hooks |
| **Hooks** `@Before` / `@After` por etiqueta, y adjuntar evidencia al informe (`scenario.attach`) | `UnitHooks`, `ApiHooks` |
| **Estado por escenario** sin que se contaminen entre sí | `PricingWorld`, `ApiWorld` |
| **Inyección de beans de Spring** en los pasos | `ApiSteps` |
| **Reloj fijo** para probar un cupón de temporada sin esperar a diciembre | `PricingWorld` |

## Estructura

```text
src/main/java/com/juniverse/cucumber/
├── controller/     # ShopController (API REST)
├── service/        # PricingService: LAS REGLAS que describen las features; CartService (carritos)
├── repository/     # ProductCatalog (catálogo con stock)
├── model/          # Product, Customer, OrderLine, Quote
├── exception/      # CouponException, ShopException, ApiExceptionHandler
└── config/         # ClockConfig

src/test/resources/
├── junit-platform.properties        # Cucumber: sin publicación en la nube, nombres largos, sin paralelismo
└── features/
    ├── unit/   volumen.feature · cupones.feature · envio_e_impuestos.feature
    └── api/    carrito.feature · compra.feature

src/test/java/com/juniverse/cucumber/
├── UnitFeaturesTest.java · ApiFeaturesTest.java      # los dos runners (@Suite)
└── steps/
    ├── common/  ParameterTypes
    ├── unit/    PricingSteps · PricingWorld · UnitHooks
    └── api/     ApiSteps · ApiWorld · ApiHooks · CucumberSpringConfiguration
```

## Requisitos

- JDK 21 y Maven 3.9 o superior. No necesita Docker ni bases de datos.

## Ejecutar

```bash
mvn test                                              # las dos suites: 47 escenarios
mvn test -Dcucumber.filter.tags="@cupones"            # solo los de cupones
mvn test -Dcucumber.filter.tags="@api and @compra"    # combinaciones de etiquetas
mvn test -Dcucumber.filter.tags="@unitario and not @volumen"
mvn test -Dcucumber.filter.name="NAVIDAD25|ENVIOGRATIS"   # por nombre del escenario (expresión regular)
```

Los escenarios que no coinciden aparecen como *Skipped*. Si el filtro no incluye ningún escenario de API, **el contexto de Spring ni siquiera se arranca**.

### Informes

Tras ejecutar, abre en el navegador:

- `target/cucumber-reports/unit.html` y `target/cucumber-reports/api.html`: cada característica, regla y escenario con sus pasos en verde o rojo, las tablas, y —en los de API— la **última petición y respuesta** adjuntas como evidencia.
- Los mismos datos en `*.json` (para herramientas) y `*.xml` (formato JUnit, para integración continua).

## Cómo escribir buenos escenarios

- **Describe el comportamiento, no la implementación.** "Cuando aplica el cupón VIP20", no "cuando se hace PUT a /coupon". El escenario debe seguir siendo cierto aunque cambie la tecnología.
- **Un escenario, una regla.** Si necesitas la palabra "y además" en el título, son dos escenarios.
- **Usa ejemplos concretos** y una tabla (`Esquema del escenario`) cuando solo cambian los datos.
- **Escenarios independientes:** cada uno prepara lo que necesita y el orden de ejecución no importa. Por eso hay `@Before` que restablece el catálogo y mundos por escenario.
- **Reutiliza pasos**, pero no los hagas tan genéricos que la frase deje de significar algo ("Cuando hago X con Y").
- **Los datos importantes, visibles:** si el resultado depende de 800.00 y 10 unidades, que se vea en el escenario, no escondido en el código.

## Trampas con las que se topó este ejemplo

| Problema | Causa |
|----------|-------|
| `DuplicateStepDefinitionException` | Cucumber no distingue las palabras clave: `@Cuando("calcula")` y `@Y("calcula")` son **el mismo paso**. Cada frase se define una sola vez, con cualquier anotación |
| Ambigüedad entre `PicoFactory` y `SpringFactory` al tener ambos en el classpath | Se elige la fábrica explícitamente en cada runner (`OBJECT_FACTORY_PROPERTY_NAME`) |
| El estado de un escenario "se filtraba" al siguiente | Los beans de Spring son *singleton*; el estado por escenario va en un bean `@ScenarioScope` y los singletons compartidos se restablecen con un `@Before` |
| El `Esquema del escenario` no sustituía `<columna>` | Los marcadores se escriben igual en las frases y dentro de las celdas de una tabla del paso; el nombre de la columna debe coincidir exactamente |
| Comparar importes con `equals` fallaba (`20.0` ≠ `20.00`) | `BigDecimal.equals` compara la escala; se usa `compareTo` |

### ¿Las pruebas realmente protegen?

Una suite verde no basta. Se verificó rompiendo a propósito tres reglas del negocio: cambiar el IVA a 15 % hizo fallar 6 escenarios, subir el umbral del descuento por volumen a 11 unidades hizo fallar 6, y quitar la reversión de una compra fallida hizo fallar 1 (el de "otro cliente agota el producto"). Con el código restaurado, todo vuelve a verde. Haz tú mismo la prueba: cambia una constante de `PricingService` y ejecuta `mvn test`.

## Cucumber frente a JUnit "normal"

| | JUnit + AssertJ | Cucumber |
|---|---|---|
| Quién lo lee | Programadores | Todo el equipo |
| Ideal para | Detalles técnicos, casos límite de código | Reglas de negocio y flujos de usuario |
| Costo | Bajo | Mantener el *glue* y las frases; si nadie de negocio lee las features, pierde su sentido |
| Combinación | Úsalos juntos: pruebas unitarias técnicas con JUnit (ver `sbj-testing`) y las reglas que el negocio debe poder leer y validar con Cucumber |

## Licencia

Código de ejemplo de uso libre con fines educativos.
