package dao;

import db.Db;
import model.Envio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnvioDAO {

    // INSERT: Ahora guardamos TODO el texto
    private static final String INSERT_SQL = """
            INSERT INTO envio (
                pedido_id, repartidor_id, fecha_salida, numero_seguimiento, estado,
                nombre_empresa, nombre_repartidor, telefono_repartidor, 
                nombre_cliente, direccion_entrega
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM envio ORDER BY id";

    private static final String SELECT_BY_PEDIDO_ID_SQL =
            "SELECT * FROM envio WHERE pedido_id = ?";

    private static final String UPDATE_ESTADO_SQL =
            "UPDATE envio SET estado = ? WHERE id = ?";

    // --- INSERTAR (AHORA CON SNAPSHOT) ---
    public void insert(Envio e) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            pst.setInt(1, e.getPedidoId());
            pst.setInt(2, e.getRepartidorId());
            pst.setDate(3, new java.sql.Date(e.getFechaSalida().getTime()));
            pst.setString(4, e.getNumeroSeguimiento());
            pst.setString(5, e.getEstado());

            // GUARDAMOS LA COPIA DE SEGURIDAD (SNAPSHOT)
            pst.setString(6, e.getNombreEmpresa());
            pst.setString(7, e.getNombreRepartidor());
            pst.setString(8, e.getTelefonoRepartidor());
            pst.setString(9, e.getNombreCliente());
            pst.setString(10, e.getDireccionCliente());

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) e.setId(rs.getInt(1));
            }
        }
    }

    public void insert(Envio e, Connection con) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            pst.setInt(1, e.getPedidoId());
            pst.setInt(2, e.getRepartidorId());
            pst.setDate(3, new java.sql.Date(e.getFechaSalida().getTime()));
            pst.setString(4, e.getNumeroSeguimiento());
            pst.setString(5, e.getEstado());

            // GUARDAMOS LA COPIA DE SEGURIDAD (SNAPSHOT)
            pst.setString(6, e.getNombreEmpresa());
            pst.setString(7, e.getNombreRepartidor());
            pst.setString(8, e.getTelefonoRepartidor());
            pst.setString(9, e.getNombreCliente());
            pst.setString(10, e.getDireccionCliente());

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) e.setId(rs.getInt(1));
            }
        }
    }

    // --- UPDATE (Solo permitimos cambiar estado o tracking, el historial no se toca) ---
    public void updateEstado(Envio e) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(UPDATE_ESTADO_SQL)) {
            pst.setString(1, e.getEstado());
            pst.setInt(2, e.getId());
            pst.executeUpdate();
        }
    }

    // --- LECTURA RAPIDA ---
    public List<Envio> findAll() throws SQLException {
        List<Envio> lista = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) lista.add(mapRow(rs));
        }
        return lista;
    }

    public Envio findByPedidoId(int pedidoId) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_BY_PEDIDO_ID_SQL)) {
            pst.setInt(1, pedidoId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        }
    }

    private Envio mapRow(ResultSet rs) throws SQLException {
        Envio e = new Envio(
                rs.getInt("id"),
                rs.getInt("pedido_id"),
                rs.getInt("repartidor_id"),
                rs.getDate("fecha_salida")
        );
        e.setNumeroSeguimiento(rs.getString("numero_seguimiento"));
        e.setEstado(rs.getString("estado"));

        e.setNombreEmpresa(rs.getString("nombre_empresa"));
        e.setNombreRepartidor(rs.getString("nombre_repartidor"));
        e.setTelefonoRepartidor(rs.getString("telefono_repartidor"));
        e.setNombreCliente(rs.getString("nombre_cliente"));
        e.setDireccionCliente(rs.getString("direccion_entrega"));

        return e;
    }
}