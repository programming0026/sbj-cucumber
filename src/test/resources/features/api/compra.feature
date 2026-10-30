# language: es
@api @compra
Característica: Confirmar la compra
  Al confirmar, el inventario se descuenta y el carrito deja de poder modificarse.

  Antecedentes:
    Dado el siguiente inventario
      | sku     | nombre  | precio | existencias |
      | LAPTOP  | Laptop  | 800.00 | 10          |
      | MOUSE   | Mouse   | 20.00  | 100         |
      | MONITOR | Monitor | 300.00 | 5           |

  Escenario: Una compra correcta descuenta las existencias
    Dado un carrito nuevo para el cliente "ana"
    Y que agrega 2 unidades de "LAPTOP"
    Y que agrega 10 unidades de "MOUSE"
    Cuando confirma la compra
    Entonces la respuesta tiene el código 201
    Y la compra tiene un número de pedido
    Y el total cobrado es 2076.40
    Y las existencias de "LAPTOP" quedan en 8
    Y las existencias de "MOUSE" quedan en 90

  Escenario: No se puede confirmar un carrito vacío
    Dado un carrito nuevo para el cliente "ana"
    Cuando confirma la compra
    Entonces la respuesta tiene el código 422
    Y el mensaje de error contiene "carrito vacío"

  Escenario: Un carrito ya confirmado no admite cambios ni una segunda confirmación
    Dado un carrito nuevo para el cliente "ana"
    Y que agrega 1 unidad de "MONITOR"
    Y que confirma la compra
    Cuando agrega 1 unidad de "MOUSE"
    Entonces la respuesta tiene el código 409
    Y el mensaje de error contiene "ya fue confirmado"
    Cuando confirma la compra
    Entonces la respuesta tiene el código 409
    Y las existencias de "MONITOR" quedan en 4

  Escenario: Si otro cliente agota el producto, la compra falla sin descontar nada
    Dado un carrito nuevo para el cliente "ana"
    Y que agrega 5 unidades de "MOUSE"
    Y que agrega 3 unidades de "MONITOR"
    Y que otro cliente compra 4 unidades de "MONITOR"
    Cuando confirma la compra
    Entonces la respuesta tiene el código 409
    Y el mensaje de error contiene "Ya no hay existencias suficientes"
    # MOUSE se había descontado antes de fallar MONITOR: la compra debe deshacerse por completo
    Y las existencias de "MOUSE" quedan en 100
    Y las existencias de "MONITOR" quedan en 1

  @catalogo
  Escenario: El catálogo refleja las existencias después de una compra
    Dado un carrito nuevo para el cliente "ana"
    Y que agrega 5 unidades de "MONITOR"
    Y que confirma la compra
    Cuando consulta el catálogo
    Entonces el catálogo muestra "MONITOR" con 0 existencias
