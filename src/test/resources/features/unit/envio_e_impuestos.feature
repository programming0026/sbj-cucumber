# language: es
@unitario @envio @impuestos
Característica: Envío e impuestos
  El envío cuesta 9.90 y es gratis desde 100.00 de importe neto. El IVA es el 16 % del importe neto.

  Antecedentes:
    Dado el catálogo de productos
      | sku     | nombre  | precio |
      | LAPTOP  | Laptop  | 800.00 |
      | MOUSE   | Mouse   | 20.00  |
      | TECLADO | Teclado | 50.00  |
    Y un cliente "ana"

  Esquema del escenario: El envío es gratis desde 100.00 de importe neto
    Cuando compra <cantidad> unidades de "TECLADO"
    Entonces el envío cuesta <envio>

    Ejemplos:
      | cantidad | envio |
      | 1        | 9.90  |
      | 2        | 0.00  |
      | 3        | 0.00  |

  Escenario: El umbral se mide después de los descuentos
    Dado que es su primera compra
    Y que el carrito contiene
      | producto | cantidad |
      | TECLADO  | 2        |
    Cuando aplica el cupón "BIENVENIDO10"
    Y calcula el total
    Entonces el importe neto es 90.00
    Y el envío cuesta 9.90

  Escenario: El cupón ENVIOGRATIS elimina el costo de envío
    Dado que el carrito contiene
      | producto | cantidad |
      | MOUSE    | 1        |
    Cuando aplica el cupón "ENVIOGRATIS"
    Y calcula el total
    Entonces el envío cuesta 0.00
    Y el descuento por cupón es 0.00
    Y el total a pagar es 23.20

  Escenario: Un carrito vacío no paga envío ni impuestos
    Cuando calcula el total
    Entonces el desglose es
      | concepto     | importe |
      | subtotal     | 0.00    |
      | importe neto | 0.00    |
      | envío        | 0.00    |
      | IVA          | 0.00    |
      | total        | 0.00    |

  Escenario: Desglose completo de una compra con todos los conceptos
    Dado un cliente "eva"
    Y que el cliente es VIP
    Y que el carrito contiene
      | producto | cantidad |
      | LAPTOP   | 2        |
      | MOUSE    | 10       |
    Cuando aplica el cupón "VIP20"
    Y calcula el total
    Entonces el desglose es
      | concepto            | importe |
      | subtotal            | 1800.00 |
      | descuento volumen   | 10.00   |
      | descuento cupón     | 358.00  |
      | importe neto        | 1432.00 |
      | envío               | 0.00    |
      | IVA                 | 229.12  |
      | total               | 1661.12 |
