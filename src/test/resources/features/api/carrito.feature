# language: es
@api @carrito
Característica: Carrito de compra a través de la API
  Los mismos precios y cupones, pero ahora de extremo a extremo: peticiones HTTP a la aplicación real
  (con su contexto de Spring), validaciones y códigos de respuesta.

  Antecedentes:
    Dado el siguiente inventario
      | sku     | nombre  | precio | existencias |
      | LAPTOP  | Laptop  | 800.00 | 10          |
      | MOUSE   | Mouse   | 20.00  | 100         |
      | MONITOR | Monitor | 300.00 | 5           |

  Escenario: Agregar productos y ver el total con su desglose
    Dado un carrito nuevo para el cliente "ana"
    Cuando agrega 2 unidades de "LAPTOP"
    Y agrega 1 unidad de "MOUSE"
    Y consulta el carrito
    Entonces la respuesta tiene el código 200
    Y el carrito tiene 2 productos distintos
    Y el desglose del carrito es
      | concepto     | importe |
      | subtotal     | 1620.00 |
      | importe neto | 1620.00 |
      | envío        | 0.00    |
      | IVA          | 259.20  |
      | total        | 1879.20 |

  Escenario: Al agregar el mismo producto dos veces se suman las unidades
    Dado un carrito nuevo para el cliente "ana"
    Cuando agrega 3 unidades de "MOUSE"
    Y agrega 4 unidades de "MOUSE"
    Entonces el carrito contiene 7 unidades de "MOUSE"

  Escenario: Quitar un producto del carrito
    Dado un carrito nuevo para el cliente "ana"
    Y que agrega 1 unidad de "LAPTOP"
    Y que agrega 1 unidad de "MOUSE"
    Cuando quita "LAPTOP" del carrito
    Entonces la respuesta tiene el código 200
    Y el carrito tiene 1 productos distintos

  Escenario: Crear un carrito enviando el cuerpo JSON
    Cuando envía la petición para crear un carrito con el cuerpo:
      """json
      {
        "customer": "luis",
        "vip": true,
        "firstPurchase": false
      }
      """
    Entonces la respuesta tiene el código 201
    Y la respuesta contiene el cliente "luis"

  Regla: Las peticiones inválidas se rechazan con un mensaje claro

    Escenario: No se puede crear un carrito sin cliente
      Cuando envía la petición para crear un carrito con el cuerpo:
        """json
        { "customer": "", "vip": false, "firstPurchase": true }
        """
      Entonces la respuesta tiene el código 400

    Esquema del escenario: Casos de error al agregar productos
      Dado un carrito nuevo para el cliente "ana"
      Cuando agrega <cantidad> unidades de "<producto>"
      Entonces la respuesta tiene el código <codigo>
      Y el mensaje de error contiene "<mensaje>"

      Ejemplos:
        | producto | cantidad | codigo | mensaje                           |
        | FANTASMA | 1        | 404    | No existe el producto FANTASMA    |
        | MONITOR  | 6        | 409    | quedan 5                          |
        | MOUSE    | 101      | 409    | quedan 100                        |

    Escenario: Un carrito que no existe devuelve 404
      Cuando consulta el carrito "inexistente"
      Entonces la respuesta tiene el código 404
      Y el mensaje de error contiene "No existe el carrito"

  Regla: Los cupones se validan al aplicarlos

    Escenario: Aplicar un cupón válido actualiza el total
      Dado un carrito nuevo para el cliente "ana" que es su primera compra
      Y que agrega 1 unidad de "MONITOR"
      Cuando aplica el cupón "BIENVENIDO10"
      Entonces la respuesta tiene el código 200
      Y el carrito tiene aplicado el cupón "BIENVENIDO10"
      Y el total del carrito es 313.20

    Escenario: Un cupón que no corresponde al cliente responde 422
      Dado un carrito nuevo para el cliente "ana"
      Y que agrega 1 unidad de "MONITOR"
      Cuando aplica el cupón "VIP20"
      Entonces la respuesta tiene el código 422
      Y el mensaje de error contiene "solo para clientes VIP"
      Y el carrito no tiene cupón

    Escenario: Quitar el cupón vuelve al precio normal
      Dado un carrito nuevo para el cliente "eva" que es VIP
      Y que agrega 1 unidad de "MONITOR"
      Y que aplica el cupón "VIP20"
      Cuando quita el cupón
      Entonces el carrito no tiene cupón
      Y el total del carrito es 348.00
