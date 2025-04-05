# Programación de Servicios y Procesos - Actividad de Aprendizaje 1 (AA1)
Aplicación de escritorio desarrollada con JavaFx en Java para la asignatura de Programación de Servicios y Procesos del 2º curso de DAM


## Requisitos para la realización de la actividad
Se pide una serie de requisitos a cumplir para desarrollar la actividad. Esta consiste en crear una aplicación Java multihilo junto al framework JavaFX que sirva como un editor de imágenes donde se podrán aplicar distintos filtros. Un filtro es cualquier función que toma una imagen como
entrada y la devuelve modificada. Es obligatorio, utilizando la libería estándar de Java, implementar los filtros de:
- Escala de grises
- Inversión de color
- Aumento del brillo

### Requisitos obligatorios
✅ 1. La aplicación permitirá procesar varias imágenes al mismo tiempo. Específicamente: abrir una imagen y procesarla. Para forzar la
concurrencia, se podrán introducir retardos en las tareas (Thread.sleep(ms)). Una vez que la imagen ha sido procesada, se podrá
guardar en el disco. Además, la aplicación permitirá visualizar la versión previa y modificada de la imagen en la propia interfaz de usuario.

✅ 2. La aplicación permitirá procesar imágenes por lotes. Es decir, seleccionar una carpeta y todas las imágenes que se encuentren dentro se procesarán de manera concurrente.

✅ 3. La aplicación permitirá seleccionar el path donde guardar las imágenes procesadas. Habrá uno por defecto. En ningún caso, se eliminará la imagen original. La nueva imagen con los filtros, tendrá por defecto un nombre nuevo.

✅ 4. La aplicación permitirá la ejecución de varios filtros por imagen de manera secuencial y en una misma interacción. Por ejemplo, aumentar el brillo y reducir el contraste de una imagen de manera consecutiva, mientras que, de manera concurrente, otra imagen es convertida a escala de grises. Todo esto se hará sin volver a abrir la imagen, que ya estará previamente cargada en memoria.

✅ 5. Se mantendrá un historial de todas las imágenes procesadas por la aplicación, así como los filtros realizados en cada una de ellas. Este fichero se podrá consultar desde la interfaz de usuario y se mostrará en un componente de JavaFX. Además, al iniciar la aplicación se mostrará un SplashScreen. También habrá una barra de progreso que mostrará el porcentaje de procesamiento de la imagen, y finalmente se mostrará un pop-up indicando la finalización del filtro en una determinada imagen. Si la tarea es cancelada o se produce un error también se deberá mostrar mediante un pop-up.

### Requisitos opcionales
✅ 1. En lugar de la clase Task, se utilizará la clase Service para la ejecución concurrente. Además, se hará uso de las clases Executors y ExecutorService para crear un pool de threads.

✅ 2. La aplicación dejará configurar un número máximo de imágenes procesadas de manera simultánea. De manera que si se excede ese
número, el resto de imágenes quedarán encoladas y se procesarán a medida que se acaben procesamientos anteriores.

⬜ 3. La aplicación permitirá deshacer los filtros aplicados o rehacerlos en el caso de haberlos deshecho.
A elegir una de estas opciones:
- Volver al estado original de la imagen
- Volver al estado previo de la imagen
- Volver a cualquier estado anterior de la imagen

⬜ 4. La aplicación también permite el uso de filtros en vídeos. En este caso, no hace falta mostrar los vídeos en la interfaz gráfica. Sólo seleccionar un vídeo, el filtro a utilizar y el vídeo procesado se guardará automáticamente en un directorio con un nombre distinto al vídeo original. Cada frame del vídeo se procesará por un hilo de manera concurrente.

⬜ 5. Realizar el seguimiento del proyecto utilizando la plataforma GitHub para almacenar el código y gestionando las issues (bug, mejoras, etc) a medida que se vaya trabajando en él. Al menos tiene que haber 2 ramas: main y develop. También puede haber Issues y ramas correspondientes.