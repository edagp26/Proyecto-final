# Proyecto-final
📁 Estructura del proyecto
bash
Copiar
Editar
SimuladorPlantas/
│
├── data/
│   ├── Plantas.csv                         # Plantas y sus requerimientos
│   ├── TemperaturaHumedadPromedio.csv      # Promedios históricos por hora
│
├── simulation/
│   └── simulaciones.csv                    # Archivo generado al simular
│
├── SimuladorAmbiente.java                  # Código fuente principal


⚙️ Requisitos
Java 11 o superior (para LocalDate, DateTimeFormatter).

Editor o IDE (NetBeans, Eclipse, IntelliJ IDEA, Visual Studio Code con extensión Java).

Sistema operativo con soporte de consola/terminal.


🧪 Uso
Al iniciar, el sistema solicita cuántos días deseas simular (como número entero).

Se generan datos horarios (cada 3 horas) para cada planta con base en archivos *.csv.

El archivo simulaciones.csv se va actualizando con nuevas fechas sin sobrescribir.

El resultado también se imprime por consola.

Se generan recomendaciones basadas en temperatura, humedad y tolerancia de cada planta.

✅ Buenas prácticas
Mantener el archivo simulaciones.csv en su carpeta (/simulation).

Validar que los archivos Plantas.csv y TemperaturaHumedadPromedio.csv estén bien formateados.

Usar punto (.) como separador decimal en TemperaturaHumedadPromedio.csv.

Ejecutar siempre desde el directorio donde se encuentran los archivos de datos.

Evitar duplicar fechas: el simulador agrega automáticamente la siguiente fecha disponible.

🛠️ Posibles mejoras
Interfaz gráfica (JavaFX o Swing).

Validación de entrada del usuario (evitar errores por texto, enter mal presionado, etc.).

Uso de una base de datos en lugar de archivos CSV.

Exportar resultados en otros formatos (JSON, XML).

Permitir simulaciones por planta específica.
