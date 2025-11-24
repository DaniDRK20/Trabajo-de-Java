package gestores;

import modelo.Cliente;
import excepciones.ClienteNoEncontradoException;
import java.util.*;
import java.util.stream.Collectors;


public class GestorClientes {

    private Map<String, Cliente> clientes;


    private Set<String> idsRegistrados;


    private Queue<OperacionReciente> colaOperaciones;


    private List<Cliente> listaRegistro;


    private Map<String, String> indiceTelefono;


    private static final int MAX_OPERACIONES_RECIENTES = 50;


    public GestorClientes() {
        this.clientes = new TreeMap<>();
        this.idsRegistrados = new HashSet<>();
        this.colaOperaciones = new LinkedList<>();
        this.listaRegistro = new ArrayList<>();
        this.indiceTelefono = new HashMap<>();
    }


    public void agregarCliente(Cliente cliente) throws IllegalArgumentException {

        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }

        String id = cliente.getId();


        if (idsRegistrados.contains(id)) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con el ID: " + id
            );
        }


        if (indiceTelefono.containsKey(cliente.getTelefono())) {
            throw new IllegalArgumentException(
                    "El teléfono ya está registrado para otro cliente"
            );
        }


        clientes.put(id, cliente);
        idsRegistrados.add(id);
        listaRegistro.add(cliente);
        indiceTelefono.put(cliente.getTelefono(), id);


        registrarOperacion("AGREGAR", id, "Cliente agregado exitosamente");
    }


    public void removerCliente(String id) throws ClienteNoEncontradoException {

        if (!idsRegistrados.contains(id)) {
            throw new ClienteNoEncontradoException(
                    "Cliente no encontrado", id
            );
        }


        Cliente cliente = clientes.get(id);


        Cliente remove = clientes.remove(id);
        idsRegistrados.remove(id);
        listaRegistro.remove(cliente);
        indiceTelefono.remove(cliente.getTelefono());


        registrarOperacion("REMOVER", id, "Cliente removido del sistema");
    }


    public Cliente buscarCliente(String id) throws ClienteNoEncontradoException {

        if (!idsRegistrados.contains(id)) {
            throw new ClienteNoEncontradoException(
                    "Cliente no encontrado", id
            );
        }


        Cliente cliente = clientes.get(id);


        registrarOperacion("BUSCAR", id, "Cliente consultado");

        return cliente;
    }


    public Cliente buscarPorTelefono(String telefono) throws ClienteNoEncontradoException {
        String id = indiceTelefono.get(telefono);

        if (id == null) {
            throw new ClienteNoEncontradoException(
                    "No se encontró cliente con ese teléfono"
            );
        }

        return buscarCliente(id);
    }


    public List<Cliente> listarClientesOrdenados() {
        // TreeMap ya mantiene orden, convertimos valores a lista
        return new ArrayList<>(clientes.values());
    }


    public List<Cliente> listarClientesPorNombre() {
        List<Cliente> lista = new ArrayList<>(clientes.values());

        // Algoritmo de ordenamiento usando Comparator
        lista.sort((c1, c2) -> {
            int comparacionNombre = c1.getNombre().compareTo(c2.getNombre());
            if (comparacionNombre != 0) {
                return comparacionNombre;
            }
            return c1.getApellido().compareTo(c2.getApellido());
        });

        return lista;
    }


    public List<Cliente> listarClientesPorSaldo() {
        List<Cliente> lista = new ArrayList<>(clientes.values());

        // Ordenar por saldo descendente
        lista.sort((c1, c2) ->
                Double.compare(c2.getCuenta().getSaldo(), c1.getCuenta().getSaldo())
        );

        return lista;
    }


    public List<Cliente> buscarPorNombre(String texto) {
        String textoBusqueda = texto.toLowerCase();


        return clientes.values().stream()
                .filter(c ->
                        c.getNombre().toLowerCase().contains(textoBusqueda) ||
                                c.getApellido().toLowerCase().contains(textoBusqueda)
                )
                .collect(Collectors.toList());
    }


    public List<Cliente> obtenerClientesConSaldoMayor(double montoMinimo) {
        return clientes.values().stream()
                .filter(c -> c.getCuenta().getSaldo() > montoMinimo)
                .collect(Collectors.toList());
    }


    public int getCantidadClientes() {
        return clientes.size();
    }


    public boolean existeCliente(String id) {
        return idsRegistrados.contains(id);
    }


    public String getEstadisticas() {
        double saldoTotal = clientes.values().stream()
                .mapToDouble(c -> c.getCuenta().getSaldo())
                .sum();

        double saldoPromedio = clientes.isEmpty() ? 0 :
                saldoTotal / clientes.size();

        Cliente clienteMayorSaldo = clientes.values().stream()
                .max((c1, c2) -> Double.compare(
                        c1.getCuenta().getSaldo(),
                        c2.getCuenta().getSaldo()
                ))
                .orElse(null);

        return String.format(
                "=== ESTADÍSTICAS DEL SISTEMA ===%n" +
                        "Total de Clientes: %d%n" +
                        "Saldo Total: $%.2f%n" +
                        "Saldo Promedio: $%.2f%n" +
                        "Cliente con Mayor Saldo: %s ($%.2f)",
                clientes.size(),
                saldoTotal,
                saldoPromedio,
                clienteMayorSaldo != null ? clienteMayorSaldo.getNombreCompleto() : "N/A",
                clienteMayorSaldo != null ? clienteMayorSaldo.getCuenta().getSaldo() : 0.0
        );
    }


    public List<OperacionReciente> getOperacionesRecientes(int cantidad) {
        List<OperacionReciente> operaciones = new ArrayList<>(colaOperaciones);

        // Retornar las últimas N operaciones
        int inicio = Math.max(0, operaciones.size() - cantidad);
        return operaciones.subList(inicio, operaciones.size());
    }


    private void registrarOperacion(String tipo, String idCliente, String descripcion) {

        if (colaOperaciones.size() >= MAX_OPERACIONES_RECIENTES) {
            colaOperaciones.poll();
        }


        colaOperaciones.offer(new OperacionReciente(tipo, idCliente, descripcion));
    }


    public void limpiarSistema() {
        clientes.clear();
        idsRegistrados.clear();
        colaOperaciones.clear();
        listaRegistro.clear();
        indiceTelefono.clear();
    }


    public static class OperacionReciente {
        private String tipo;
        private String idCliente;
        private String descripcion;
        private Date fecha;

        public OperacionReciente(String tipo, String idCliente, String descripcion) {
            this.tipo = tipo;
            this.idCliente = idCliente;
            this.descripcion = descripcion;
            this.fecha = new Date();
        }

        public String getTipo() { return tipo; }
        public String getIdCliente() { return idCliente; }
        public String getDescripcion() { return descripcion; }
        public Date getFecha() { return fecha; }

        @Override
        public String toString() {
            return String.format("[%s] %s - Cliente: %s - %s",
                    fecha, tipo, idCliente, descripcion);
        }
    }
}