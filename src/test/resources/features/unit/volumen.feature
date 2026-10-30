# language: es
@unitario @volumen
Característica: Descuento por volumen
  Quien compra muchas unidades de un mismo producto recibe un descuento sobre esa línea:
  5 % desde 10 unidades y 10 % desde 50 unidades.

  Antecedentes:
    Dado el catálogo de productos
      | sku     | nombre  | precio |
      | LAPTOP  | Laptop  | 800.00 |
      | MOUSE   | Mouse   | 20.00  |
      | TECLADO | Teclado | 50.00  |
    Y un cliente "ana"

  Escenario: Comprar pocas unidades no tiene descuento
    Cuando compra 9 unidades de "MOUSE"
    Entonces el subtotal es 180.00
    Y el descuento por volumen es 0.00

  Esquema del escenario: El descuento depende de la cantidad comprada
    Cuando compra <cantidad> unidades de "MOUSE"
    Entonces el subtotal es <subtotal>
    Y el descuento por volumen es <descuento>
    Y el importe neto es <neto>

    Ejemplos: Sin descuento
      | cantidad | subtotal | descuento | neto    |
      | 1        | 20.00    | 0.00      | 20.00   |
      | 9        | 180.00   | 0.00      | 180.00  |

    Ejemplos: Descuento del 5 %
      | cantidad | subtotal | descuento | neto    |
      | 10       | 200.00   | 10.00     | 190.00  |
      | 49       | 980.00   | 49.00     | 931.00  |

    Ejemplos: Descuento del 10 %
      | cantidad | subtotal | descuento | neto    |
      | 50       | 1000.00  | 100.00    | 900.00  |
      | 100      | 2000.00  | 200.00    | 1800.00 |

  Escenario: El descuento se calcula por producto, no sobre el total del pedido
    Dado que el carrito contiene
      | producto | cantidad |
      | MOUSE    | 10       |
      | TECLADO  | 4        |
    Cuando calcula el total
    Entonces el desglose es
      | concepto            | importe |
      | subtotal            | 400.00  |
      | descuento volumen   | 10.00   |
      | importe neto        | 390.00  |

  Escenario: El descuento se redondea a dos decimales
    Dado el producto "CHICLE" a 0.33
    Cuando compra 10 unidades de "CHICLE"
    Entonces el subtotal es 3.30
    Y el descuento por volumen es 0.17
