package gestores;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class LogManager {
    private static final String ARCHIVO_LOG = "logs.txt";
    private static final String DIRECTORIO_LOG = "./";
    private int contadorLogs = 0;
    private String usuarioActual;
    private DateTimeFormatter formatter;


    public LogManager(String usuario) {
        this.usuarioActual = usuario != null ? usuario : "Sistema";
        this.formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        inicializarArchivo();
        cargarContador();
    }


    private void inicializarArchivo() {
        try {
            File archivo = new File(ARCHIVO_LOG);

            // Crear archivo si no existe
            if (!archivo.exists()) {
                archivo.createNewFile();
                escribirEncabezado();
            }

        } catch (IOException e) {
            System.err.println("Error al inicializar archivo de logs: " + e.getMessage());
        }
    }


    private void escribirEncabezado() {
        try (FileWriter fw = new FileWriter(ARCHIVO_LOG, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println("=====================================");
            out.println("  SISTEMA DE CAJERO AUTOMÁTICO");
            out.println("  ARCHIVO DE LOGS");
            out.println("  Fecha Creación: " + LocalDateTime.now().format(formatter));
            out.println("=====================================");
            out.println();

        } catch (IOException e) {
            System.err.println("Error al escribir encabezado: " + e.getMessage());
        }
    }


    private void cargarContador() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_LOG))) {
            String linea;
            int maxId = 0;

            while ((linea = reader.readLine()) != null) {
                // Buscar líneas que empiecen con LOG-
                if (linea.startsWith("LOG-")) {
                    try {
                        String idStr = linea.substring(4, 8);
                        int id = Integer.parseInt(idStr);
                        maxId = Math.max(maxId, id);
                    } catch (Exception e) {
                        // Ignorar líneas mal formateadas
                    }
                }
            }

            contadorLogs = maxId;

        } catch (FileNotFoundException e) {
            // Archivo no existe, contador en 0
            contadorLogs = 0;
        } catch (IOException e) {
            System.err.println("Error al cargar contador: " + e.getMessage());
        }
    }


    public void registrar(String accion, String descripcion) {
        try (FileWriter fw = new FileWriter(ARCHIVO_LOG, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            contadorLogs++;
            LocalDateTime ahora = LocalDateTime.now();

            String logEntry = String.format("LOG-%04d | %s | Usuario: %s | Acción: %s | %s",
                    contadorLogs,
                    ahora.format(formatter),
                    usuarioActual,
                    accion,
                    descripcion
            );

            out.println(logEntry);

        } catch (IOException e) {
            System.err.println("Error al escribir log: " + e.getMessage());
        }
    }


    public void registrarError(String accion, Exception error) {
        String descripcion = String.format("ERROR: %s - %s",
                error.getClass().getSimpleName(),
                error.getMessage()
        );
        registrar(accion, descripcion);
    }


    public List<String> leerTodosLosLogs() {
        List<String> logs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_LOG))) {
            String linea;

            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith("LOG-")) {
                    logs.add(linea);
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("Archivo de logs no encontrado");
        } catch (IOException e) {
            System.err.println("Error al leer logs: " + e.getMessage());
        }

        return logs;
    }


    public List<String> leerUltimosLogs(int cantidad) {
        List<String> todosLosLogs = leerTodosLosLogs();

        int inicio = Math.max(0, todosLosLogs.size() - cantidad);
        return todosLosLogs.subList(inicio, todosLosLogs.size());
    }


    public List<String> buscarPorAccion(String accion) {
        List<String> resultado = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_LOG))) {
            String linea;

            while ((linea = reader.readLine()) != null) {
                if (linea.contains("Acción: " + accion)) {
                    resultado.add(linea);
                }
            }

        } catch (IOException e) {
            System.err.println("Error al buscar logs: " + e.getMessage());
        }

        return resultado;
    }


    public List<String> buscarPorUsuario(String usuario) {
        List<String> resultado = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_LOG))) {
            String linea;

            while ((linea = reader.readLine()) != null) {
                if (linea.contains("Usuario: " + usuario)) {
                    resultado.add(linea);
                }
            }

        } catch (IOException e) {
            System.err.println("Error al buscar logs: " + e.getMessage());
        }

        return resultado;
    }


    public String getEstadisticasLogs() {
        List<String> logs = leerTodosLosLogs();

        int totalLogs = logs.size();
        int errores = 0;
        int operacionesExitosas = 0;

        for (String log : logs) {
            if (log.contains("ERROR")) {
                errores++;
            } else if (log.contains("exitosamente") || log.contains("realizado")) {
                operacionesExitosas++;
            }
        }

        return String.format(
                "=== ESTADÍSTICAS DE LOGS ===%n" +
                        "Total de Entradas: %d%n" +
                        "Operaciones Exitosas: %d%n" +
                        "Errores Registrados: %d%n" +
                        "Usuario Actual: %s",
                totalLogs, operacionesExitosas, errores, usuarioActual
        );
    }


    public void cambiarUsuario(String nuevoUsuario) {
        registrar("CAMBIO_USUARIO", "Usuario cambiado de " + usuarioActual + " a " + nuevoUsuario);
        this.usuarioActual = nuevoUsuario;
    }


    public String getRutaArchivo() {
        return new File(ARCHIVO_LOG).getAbsolutePath();
    }


    public boolean exportarLogs(String nombreArchivo) {
        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_LOG));
             FileWriter writer = new FileWriter(nombreArchivo)) {

            String linea;
            while ((linea = reader.readLine()) != null) {
                writer.write(linea + System.lineSeparator());
            }

            registrar("EXPORTAR", "Logs exportados a: " + nombreArchivo);
            return true;

        } catch (IOException e) {
            System.err.println("Error al exportar logs: " + e.getMessage());
            return false;
        }
    }
}
