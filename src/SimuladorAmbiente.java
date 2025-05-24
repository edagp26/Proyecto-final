import java.io.*; // Importa clases para manejo de archivos
import java.time.LocalDate; // Importa clase para fechas
import java.time.format.DateTimeFormatter; // Importa clase para formatear fechas
import java.util.*; // Importa utilidades como Map, List, etc.

public class SimuladorAmbiente { // Define la clase principal
    private static final double TEMP_VARIACION = 2.0; // Variación máxima de temperatura simulada
    private static final double HUMEDAD_MIN = 60.0; // Humedad mínima simulada
    private static final double HUMEDAD_MAX = 95.0; // Humedad máxima simulada
    private static final double TEMP_MIN = 19.0; // Temperatura mínima de referencia
    private static final double TEMP_MAX = 30.5; // Temperatura máxima de referencia
    private static final String ARCHIVO_SIMULACION = "simulation/simulaciones.csv"; // Ruta del archivo de simulación
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE; // Formato de fecha

    // Clase interna para almacenar los datos de cada registro horario
    private static class Registro {
        String hora; // Hora del registro
        double temperatura; // Temperatura simulada
        double humedad; // Humedad simulada
        String recomendacion; // Recomendación generada

        Registro(String hora, double temperatura, double humedad, String recomendacion) { // Constructor
            this.hora = hora; // Asigna la hora
            this.temperatura = temperatura; // Asigna la temperatura
            this.humedad = humedad; // Asigna la humedad
            this.recomendacion = recomendacion; // Asigna la recomendación
        }
    }

    // Lee todas las plantas y sus requerimientos de humedad y sol desde el archivo CSV
    private static Map<String, String[]> leerPlantasYRequerimientos(String archivoPlantas) throws IOException {
        Map<String, String[]> plantas = new LinkedHashMap<>(); // Mapa para almacenar plantas y requerimientos
        try (BufferedReader br = new BufferedReader(new FileReader(archivoPlantas))) { // Abre el archivo
            String linea; // Variable para cada línea
            br.readLine(); // Salta la línea de encabezado
            while ((linea = br.readLine()) != null) { // Lee cada línea
                String[] partes = linea.split(";"); // Separa por punto y coma
                if (partes.length >= 5) { // Verifica que haya suficientes columnas
                    // partes[3]: humedad, partes[4]: sol
                    plantas.put(partes[0].trim(), new String[]{partes[3].trim().toLowerCase(), partes[4].trim().toLowerCase()});
                }
            }
        }
        return plantas; // Devuelve el mapa de plantas
    }

    // Método principal de simulación
    public static void simular() {
        try {
            Scanner sc = new Scanner(System.in); // Scanner para leer entrada del usuario
            System.out.print("¿Cuántos días desea simular?, escriba como numero entero "); // Pide cantidad de días
            int dias = sc.nextInt(); // Lee el número de días
            sc.nextLine(); // Limpia el buffer

            Map<String, String[]> plantas = leerPlantasYRequerimientos("data/Plantas.csv"); // Lee plantas y requerimientos
            List<String> horas = Arrays.asList("0:00", "3:00", "6:00", "9:00", "12:00", "15:00", "18:00", "21:00"); // Horas a simular
            Map<String, Double> tempReferencia = leerTemperaturasReferencia("data/TemperaturaHumedadPromedio.csv"); // Lee temperaturas de referencia

            LocalDate fechaInicio = obtenerSiguienteFecha(); // Obtiene la fecha de inicio para la simulación
            Random rand = new Random(); // Generador de números aleatorios

            // Generar matriz de temperatura y humedad por día y hora
            double[][] tempSimPorDiaHora = new double[dias][horas.size()]; // Matriz de temperaturas simuladas
            double[][] humedadSimPorDiaHora = new double[dias][horas.size()]; // Matriz de humedades simuladas
            for (int d = 0; d < dias; d++) { // Por cada día
                for (int h = 0; h < horas.size(); h++) { // Por cada hora
                    double tempRef = tempReferencia.getOrDefault(horas.get(h), 25.0); // Temperatura base
                    double tempSim = tempRef + (rand.nextDouble() * 2 * TEMP_VARIACION - TEMP_VARIACION); // Simula temperatura
                    double humedadSim = calcularHumedad(tempSim); // Calcula humedad en base a temperatura
                    tempSimPorDiaHora[d][h] = tempSim; // Guarda temperatura simulada
                    humedadSimPorDiaHora[d][h] = humedadSim; // Guarda humedad simulada
                }
            }

            // Guardar simulaciones por planta y día
            Map<String, List<List<Registro>>> simulacionesPorPlanta = new LinkedHashMap<>(); // Mapa de simulaciones por planta
            List<String> nombresPlantas = new ArrayList<>(plantas.keySet()); // Lista de nombres de plantas

            for (String nombrePlanta : nombresPlantas) { // Por cada planta
                String[] req = plantas.get(nombrePlanta); // Obtiene requerimientos
                String requerimientoHumedad = req[0]; // Requerimiento de humedad
                String toleranciaSol = req[1]; // Tolerancia al sol
                List<List<Registro>> simulacionesPorDia = new ArrayList<>(); // Lista de simulaciones por día
                for (int d = 0; d < dias; d++) { // Por cada día
                    List<Registro> simulacion = new ArrayList<>(); // Lista de registros para el día
                    for (int h = 0; h < horas.size(); h++) { // Por cada hora
                        double tempSim = tempSimPorDiaHora[d][h]; // Temperatura simulada
                        double humedadSim = humedadSimPorDiaHora[d][h]; // Humedad simulada
                        String recomendacion = generarRecomendacion(tempSim, humedadSim, requerimientoHumedad, toleranciaSol); // Genera recomendación
                        simulacion.add(new Registro(horas.get(h), tempSim, humedadSim, recomendacion)); // Agrega registro
                    }
                    simulacionesPorDia.add(simulacion); // Agrega simulación del día
                }
                simulacionesPorPlanta.put(nombrePlanta, simulacionesPorDia); // Guarda simulaciones de la planta
            }

            guardarSimulacionesCSV(simulacionesPorPlanta, fechaInicio, dias, nombresPlantas); // Guarda resultados en archivo

        
            for (int d = 0; d < dias; d++) { // Por cada día
                LocalDate fecha = fechaInicio.plusDays(d); // Calcula la fecha
                System.out.println("Fecha"); // Imprime encabezado de fecha
                System.out.println(fecha); // Imprime la fecha
                for (String nombrePlanta : nombresPlantas) { // Por cada planta
                    System.out.println("PLANTA;" + nombrePlanta); // Imprime nombre de la planta
                    System.out.println("Hora;Temperatura;Humedad;Recomendaciones"); // Imprime encabezados
                    List<Registro> registros = simulacionesPorPlanta.get(nombrePlanta).get(d); // Obtiene registros del día
                    for (Registro r : registros) { // Por cada registro
                        System.out.printf("%s;%.1f;%.1f;%s\n", r.hora, r.temperatura, r.humedad, r.recomendacion); // Imprime datos
                    }
                }
            }
        } catch (IOException e) { // Captura errores de archivo
            System.out.println("Error al leer el archivo de referencia: " + e.getMessage()); // Muestra mensaje de error
        }
    }

    // Genera una recomendación según la humedad, requerimiento y tolerancia al sol
    private static String generarRecomendacion(double temp, double humedad, String requerimientoHumedad, String toleranciaSol) {
        double bajo = HUMEDAD_MIN + (HUMEDAD_MAX - HUMEDAD_MIN) / 3; // Umbral bajo de humedad
        double alto = HUMEDAD_MAX - (HUMEDAD_MAX - HUMEDAD_MIN) / 3; // Umbral alto de humedad

        String nivel; // Nivel de humedad
        if (humedad < bajo) nivel = "baja"; // Si es menor al bajo, es baja
        else if (humedad > alto) nivel = "alta"; // Si es mayor al alto, es alta
        else nivel = "media"; // Si no, es media

        StringBuilder recomendacion = new StringBuilder(); // Para construir la recomendación
        if (nivel.equals(requerimientoHumedad)) { // Si coincide con el requerimiento
            recomendacion.append("Humedad adecuada para la planta"); // Es adecuada
        } else if (nivel.equals("baja")) { // Si es baja
            recomendacion.append("Humedad baja en el ambiente, si la tierra esta seca se recomienda regarla"); // Recomienda regar
        } else if (nivel.equals("media")) { // Si es media
            recomendacion.append("Humedad media en el ambiente, verificar que la tierra no este seca"); // Recomienda verificar
        } else { // Si es alta
            recomendacion.append("Humedad alta en el ambiente, verificar que no haya encharcamiento"); // Recomienda evitar encharcamiento
        }

        if (temp > 25) { // Si la temperatura es mayor a la ambiente promedio
            if (toleranciaSol.equals("alto")) { // Si tolera el sol
                recomendacion.append(" y puede sacarse al sol."); // Puede ir al sol
            } else { // Si no tolera el sol
                recomendacion.append(" y debe alejarse del sol."); // Debe alejarse del sol
            }
        } else {
            recomendacion.append("."); // Punto final
        }
        return recomendacion.toString(); // Devuelve la recomendación
    }

    // Lee las temperaturas de referencia por hora desde un archivo CSV
    private static Map<String, Double> leerTemperaturasReferencia(String archivo) throws IOException {
        Map<String, Double> mapa = new HashMap<>(); // Mapa para almacenar hora y temperatura
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) { // Abre el archivo
            String linea; // Variable para cada línea
            while ((linea = br.readLine()) != null && !linea.isEmpty()) { // Lee cada línea
                if (linea.contains(";")) { // Si la línea contiene punto y coma
                    String[] partes = linea.split(";"); // Separa por punto y coma
                    if (partes.length >= 2 && partes[0].matches("\\d{1,2}:\\d{2}")) { // Verifica formato de hora
                        double temp = Double.parseDouble(partes[1].replace(",", ".")); // Convierte la temperatura
                        mapa.put(partes[0], temp); // Agrega al mapa
                    }
                }
            }
        }
        return mapa; // Devuelve el mapa de temperaturas
    }

    // Calcula la humedad simulada en base a la temperatura simulada
    private static double calcularHumedad(double temp) {
        double proporcion = (temp - TEMP_MIN) / (TEMP_MAX - TEMP_MIN); // Calcula la proporción de temperatura
        return HUMEDAD_MAX - proporcion * (HUMEDAD_MAX - HUMEDAD_MIN); // Calcula la humedad simulada
    }

    // Obtiene la siguiente fecha disponible para la simulación, según el archivo de simulaciones
    private static LocalDate obtenerSiguienteFecha() {
        File archivo = new File(ARCHIVO_SIMULACION);
        if (!archivo.exists()) {
            return LocalDate.now();
        }
        Set<LocalDate> fechasUsadas = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("Fecha")) {
                    String fechaStr = linea.contains(":") ? linea.split(":")[1].trim() : br.readLine();
                    if (fechaStr != null && !fechaStr.isEmpty()) {
                        fechasUsadas.add(LocalDate.parse(fechaStr, FORMATO_FECHA));
                    }
                }
            }
        } catch (IOException e) {
            return LocalDate.now();
        }
        LocalDate fecha = fechasUsadas.isEmpty() ? LocalDate.now() : Collections.max(fechasUsadas).plusDays(1);
        // Asegura que la fecha no esté repetida
        while (fechasUsadas.contains(fecha)) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }

    // Guarda los resultados de la simulación en el archivo CSV, agrupando por fecha y luego por planta
    private static void guardarSimulacionesCSV(
            Map<String, List<List<Registro>>> simulacionesPorPlanta, // Mapa de simulaciones por planta
            LocalDate fechaInicio, // Fecha de inicio
            int dias, // Número de días simulados
            List<String> plantas // Lista de nombres de plantas
    ) throws IOException {
        File carpeta = new File("simulation"); // Carpeta de salida
        if (!carpeta.exists()) carpeta.mkdirs(); // Crea la carpeta si no existe
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_SIMULACION, true))) { // Abre el archivo para agregar
            for (int d = 0; d < dias; d++) { // Por cada día
                LocalDate fecha = fechaInicio.plusDays(d); // Calcula la fecha
                pw.println("Fecha: " + fecha.format((FORMATO_FECHA))); // Escribe la fecha
                for (String nombrePlanta : plantas) { // Por cada planta
                    pw.println("PLANTA;" + nombrePlanta); // Escribe nombre de la planta
                    pw.println("Hora;Temperatura;Humedad;Recomendaciones"); // Escribe encabezados
                    List<Registro> registros = simulacionesPorPlanta.get(nombrePlanta).get(d); // Obtiene registros del día
                    for (Registro r : registros) { // Por cada registro
                        pw.printf("%s;%.1f;%.1f;%s\n", r.hora, r.temperatura, r.humedad, r.recomendacion); // Escribe los datos
                    }
                }
            }
        }
        System.out.println("Simulación guardada en: " + ARCHIVO_SIMULACION); // Mensaje de confirmación
    }
}