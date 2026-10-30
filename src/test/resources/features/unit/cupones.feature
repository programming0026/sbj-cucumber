# language: es
@unitario @cupones
Característica: Cupones de descuento
  Cada cupón tiene sus propias reglas. Si no se cumplen, el cliente recibe una explicación clara.

  Antecedentes:
    Dado el catálogo de productos
      | sku     | nombre  | precio |
      | LAPTOP  | Laptop  | 800.00 |
      | MOUSE   | Mouse   | 20.00  |
      | TECLADO | Teclado | 50.00  |

  Regla: BIENVENIDO10 da 10 % solo en la primera compra

    Escenario: Un cliente nuevo aprovecha el cupón
      Dado un cliente "ana"
      Y que es su primera compra
      Y que el carrito contiene
        | producto | cantidad |
        | TECLADO  | 4        |
      Cuando aplica el cupón "BIENVENIDO10"
      Y calcula el total
      Entonces se aplica el cupón "BIENVENIDO10"
      Y el descuento por cupón es 20.00
      Y el importe neto es 180.00

    Escenario: Un cliente que ya compró antes no puede usarlo
      Dado un cliente "luis"
      Y que el carrito contiene
        | producto | cantidad |
        | TECLADO  | 4        |
      Cuando aplica el cupón "BIENVENIDO10"
      Y calcula el total
      Entonces el cupón es rechazado con el motivo "solo para la primera compra"

  Regla: VIP20 da 20 % solo a clientes VIP

    Escenario: Un cliente VIP recibe el 20 %
      Dado un cliente "eva"
      Y que el cliente es VIP
      Y que el carrito contiene
        | producto | cantidad |
        | LAPTOP   | 1        |
      Cuando aplica el cupón "VIP20"
      Y calcula el total
      Entonces el descuento por cupón es 160.00
      Y el importe neto es 640.00

    Escenario: Un cliente que no es VIP es rechazado
      Dado un cliente "ana"
      Y que el carrito contiene
        | producto | cantidad |
        | LAPTOP   | 1        |
      Cuando aplica el cupón "VIP20"
      Y calcula el total
      Entonces el cupón es rechazado con el motivo "solo para clientes VIP"

  Regla: NAVIDAD25 da 25 % solo en diciembre y con compra mínima de 200

    Esquema del escenario: La validez depende de la fecha y del importe
      Dado un cliente "ana"
      Y que hoy es <fecha>
      Y que el carrito contiene
        | producto | cantidad |
        | TECLADO  | <cantidad> |
      Cuando aplica el cupón "NAVIDAD25"
      Y calcula el total
      Entonces el resultado del cupón es "<resultado>"

      Ejemplos:
        | fecha      | cantidad | resultado                          |
        | 2026-12-01 | 4        | aplicado                           |
        | 2026-12-31 | 8        | aplicado                           |
        | 2026-11-30 | 8        | solo es válido en diciembre        |
        | 2027-01-01 | 8        | solo es válido en diciembre        |
        | 2026-12-15 | 3        | requiere una compra mínima de 200  |

    Escenario: El mínimo se mide después del descuento por volumen
      Dado un cliente "ana"
      Y que hoy es 2026-12-10
      Y que el carrito contiene
        | producto | cantidad |
        | MOUSE    | 10       |
      Cuando aplica el cupón "NAVIDAD25"
      Y calcula el total
      Entonces el cupón es rechazado con el motivo "compra mínima de 200"

  Regla: Los cupones desconocidos se rechazan

    Esquema del escenario: Cupones inexistentes o mal escritos
      Dado un cliente "ana"
      Y que el carrito contiene
        | producto | cantidad |
        | MOUSE    | 1        |
      Cuando aplica el cupón "<codigo>"
      Y calcula el total
      Entonces el cupón es rechazado con el motivo "no existe"

      Ejemplos:
        | codigo       |
        | GRATIS       |
        | VERANO       |
        | BIENVENIDO11 |

    Escenario: El código del cupón no distingue mayúsculas de minúsculas
      Dado un cliente "ana"
      Y que es su primera compra
      Y que el carrito contiene
        | producto | cantidad |
        | TECLADO  | 2        |
      Cuando aplica el cupón "bienvenido10"
      Y calcula el total
      Entonces se aplica el cupón "BIENVENIDO10"
