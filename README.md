# Despacho de mensajería urbana en moto

Aplicación de escritorio en Java 17 para armar la ruta de un mensajero en moto, cargar el maletero y verificar que la carga sea exactamente el inverso de la ruta antes de salir. Usa una lista doblemente enlazada para la ruta, colas para los envíos pendientes y de reintento, y una pila para el maletero.

La interfaz gráfica está hecha con Swing y muestra en todo momento el estado de las tres estructuras. Toda la lógica de las estructuras vive en las clases del dominio; la interfaz solo invoca métodos públicos y dibuja lo que estos devuelven.

## Requisitos

- Java Development Kit (JDK) 17 o superior.
- No se requieren dependencias externas, Maven ni Gradle.
- Para la interfaz se necesita un entorno gráfico con Swing disponible.

## Instalación

No hay dependencias que instalar. Basta con tener `java` y `javac` en el `PATH`:

```bash
java -version
javac -version
```

El proyecto se compila directamente con el compilador oficial y los archivos se generan en `out/`.

## Ejecución

Desde la raíz del proyecto:

```bash
javac -d out $(find src -name "*.java")
java -cp out courier.Main
```

La aplicación abre una ventana de 1366×768 con cuatro secciones navegables desde la barra superior:

1. **Bandeja de envíos**: colas de pendientes y reintentos, formulario de recepción, carga de datos de ejemplo y armado de ruta.
2. **Editor de ruta**: lista enlazada de paradas, parada actual, mover, insertar, eliminar y recorrido en ambos sentidos.
3. **Maletero**: pila vertical con el tope arriba, ruta al lado, validación en vivo y operaciones de carga, descarga, entrega y entrega fallida.
4. **Cierre y reportes**: cierre de ruta, métricas actualizadas y bitácora cronológica.

## Uso

1. Reciba envíos con el formulario de la bandeja o use **Cargar datos de ejemplo** (RFE-10).
2. Pulse **Armar ruta**: se toman hasta N envíos del frente de pendientes, en el mismo orden.
3. En el editor puede mover, insertar o eliminar paradas. Si el maletero estaba cargado, cualquier modificación invalida la carga (R5) y lo descarga para obligar a recargarlo.
4. En la pantalla del maletero pulse **Cargar maletero**: se apila desde la última parada hasta la primera, de modo que el tope sea la primera parada.
5. Pulse **Validar carga** para comparar la pila con la ruta sin vaciarla. El botón **Salir** solo se habilita con una carga válida.
6. **Entregar** desapila el tope, marca la parada como entregada y avanza el apuntador actual. **Registrar entrega fallida** mueve el paquete al fondo con un maletero auxiliar, marca la parada como fallida y la reubica al final de la ruta.
7. Al **Cerrar ruta**, lo no entregado vuelve a pendientes y lo que llegó a dos intentos fallidos pasa a la cola de reintentos del día siguiente.

Los errores se muestran en la barra de estado y citan la regla incumplida (R1, R2, R3, R4, R5, R6, R7, R8, R9 o RF-01). Todas las operaciones quedan en la bitácora.

## Decisiones documentadas

- **RF-05 / carga del maletero**: si el maletero no está vacío, la carga se rechaza. Debe descargarse primero con el botón **Descargar maletero**.
- **R5 / invalidación**: al mover, insertar o eliminar una parada con el maletero cargado, la carga se marca como inválida y el maletero se descarga automáticamente. Se incrementa el contador de invalidaciones y se exige volver a cargar antes de salir.
- **R7 / movimientos**: se cuenta cada operación de desapilado y apilado ejecutada durante la reorganización con el maletero auxiliar, incluyendo el paquete fallido y los paquetes que se desplazan para conservar el orden relativo.
- **Espera en pendientes**: el reloj de la oficina se toma como el mayor minuto de recepción conocido. La espera de un envío es la diferencia entre ese minuto y el momento en que entró a la cola, hasta que se asigna a una ruta. El reporte muestra cada espera, el promedio y el máximo.
- **Capacidad**: `trunk.capacity` define N, el máximo de paradas y de paquetes del maletero. `pending.capacity` define la capacidad de la cola de pendientes para la recepción.
- **Datos de ejemplo**: el juego de prueba de RFE-10 está aislado en `src/courier/service/SeedData.java` y solo se carga cuando el usuario pulsa el botón correspondiente. La aplicación arranca vacía.

## Configuración

El archivo `config.properties` de la raíz permite cambiar las capacidades:

```properties
trunk.capacity=6
pending.capacity=20
```

Si el archivo no existe, se usan esos mismos valores por defecto.

## Estructura del proyecto

```text
src/courier/
  Main.java                 Punto de entrada y configuración visual de Swing
  shipment/                 Shipment, ShipmentType
  route/                    Route, Stop, StopStatus (lista doblemente enlazada)
  waiting/                  ShipmentWaitingLine, PendingShipments, RetryShipments (colas O(1))
  trunk/                    Trunk (pila LIFO O(1))
  service/                  DispatchService, Metrics, AppConfig, reportes, bitácora y datos de ejemplo
  ui/                       Ventana principal y las cuatro pantallas
config.properties           Capacidades configurables
out/                        Clases compiladas (generado, no versionado)
```

## Comandos (Git Bash)

```bash
# Requisitos: JDK 17 instalado y disponible en el PATH
java -version
javac -version

# No hay dependencias externas que instalar

# Compilar el proyecto
rm -rf out
javac -d out $(find src -name "*.java")

# Ejecutar la aplicación
java -cp out courier.Main

# Verificación de compilación (no hay suite de pruebas automatizada adicional)
rm -rf out
javac -d out $(find src -name "*.java")
```

## Notas

- No se usa Maven, Gradle, Vite, bundlers ni frameworks de interfaz.
- El código, los identificadores y los nombres de archivos están en inglés; la interfaz y este README están en español.
- Los mensajes de la aplicación se mantienen en ASCII para que la compilación funcione con la codificación por defecto del JDK en cualquier sistema.
