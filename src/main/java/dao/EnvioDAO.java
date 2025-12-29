package dao;

import db.Db;
import model.Envio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnvioDAO {

    // --- SQL CONSTANTS ---

    private static final String INSERT_SQL =
            "INSERT INTO envio (pedido_id, repartidor_id, fecha_salida, numero_seguimiento, estado) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE envio SET repartidor_id = ?, fecha_salida = ?, numero_seguimiento = ?, estado = ? WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM envio WHERE id = ?";

    // JOINs para traer datos del Repartidor y de la Empresa de una sola vez
    private static final String SELECT_BASE =
            """
            SELECT e.id, e.pedido_id, e.repartidor_id, e.fecha_salida, e.numero_seguimiento, e.estado,
                   r.nombre AS nombre_repartidor, 
                   r.telefono AS telefono_repartidor,
                   er.nombre AS nombre_empresa,
                   c.nombre AS nombre_cliente,      
                   d.direccion AS direccion_cliente 
            FROM envio e
            INNER JOIN repartidor r ON e.repartidor_id = r.id
            INNER JOIN empresa_reparto er ON r.empresa_id = er.id
            INNER JOIN pedido p ON e.pedido_id = p.id     
            INNER JOIN cliente c ON p.cliente_id = c.id
            LEFT JOIN detalle_cliente d ON c.id = d.id   
            """;
    private static final String SELECT_ALL_SQL = SELECT_BASE + " ORDER BY e.id";

    // Vital para la interfaz: buscar el envío de un pedido concreto
    private static final String SELECT_BY_PEDIDO_ID_SQL = SELECT_BASE + " WHERE e.pedido_id = ?";

    private static final String SELECT_BY_ID_SQL = SELECT_BASE + " WHERE e.id = ?";


    // --- CRUD OPERATIONS ---

    public void insert(Envio e) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            pst.setInt(1, e.getPedidoId());
            pst.setInt(2, e.getRepartidorId());
            // Conversión de java.util.Date a java.sql.Date
            pst.setDate(3, new java.sql.Date(e.getFechaSalida().getTime()));
            pst.setString(4, e.getNumeroSeguimiento());
            pst.setString(5, e.getEstado());

            pst.executeUpdate();

            // Recuperar ID autogenerado (opcional pero recomendado)
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                }
            }
        }
    }

    public void update(Envio e) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(UPDATE_SQL)) {

            pst.setInt(1, e.getRepartidorId());
            pst.setDate(2, new java.sql.Date(e.getFechaSalida().getTime()));
            pst.setString(3, e.getNumeroSeguimiento());
            pst.setString(4, e.getEstado());
            pst.setInt(5, e.getId()); // WHERE id = ?

            pst.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(DELETE_SQL)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    // --- CONSULTAS ---

    public List<Envio> findAll() throws SQLException {
        List<Envio> lista = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public Envio findById(int id) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_BY_ID_SQL)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * Busca el envío asociado a un pedido específico.
     * @param pedidoId ID del pedido (tabla pedidos)
     * @return El objeto Envio o null si todavía no se ha enviado.
     */
    public Envio findByPedidoId(int pedidoId) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_BY_PEDIDO_ID_SQL)) {
            pst.setInt(1, pedidoId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null; // No hay envío para este pedido
            }
        }
    }

    // --- MAPEO ---

    private Envio mapRow(ResultSet rs) throws SQLException {
        Envio e = new Envio(
                rs.getInt("id"),
                rs.getInt("pedido_id"),
                rs.getInt("repartidor_id"),
                rs.getDate("fecha_salida")
        );

        e.setNumeroSeguimiento(rs.getString("numero_seguimiento"));
        e.setEstado(rs.getString("estado"));

        // Auxiliares de Logística
        e.setNombreRepartidor(rs.getString("nombre_repartidor"));
        e.setTelefonoRepartidor(rs.getString("telefono_repartidor"));
        e.setNombreEmpresa(rs.getString("nombre_empresa"));

        // NUEVOS Auxiliares de Cliente
        e.setNombreCliente(rs.getString("nombre_cliente"));
        e.setDireccionCliente(rs.getString("direccion_cliente"));

        return e;
    }
}