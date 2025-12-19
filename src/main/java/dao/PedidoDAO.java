package dao;

import db.Db;
import model.Pedido;
import model.Producto;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la entidad Pedido.
 * Relación N:1 con Cliente.
 */
public class PedidoDAO {

    private static final String INSERT_SQL =
            "INSERT INTO pedido (id, cliente_id, fecha) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id, cliente_id, fecha FROM pedido WHERE id = ?";

    private static final String SELECT_ALL_SQL =
    """
    SELECT p.id, p.cliente_id, p.fecha,
    c.nombre AS nombre_cliente,
    COALESCE(SUM(dp.cantidad * dp.precio_unit), 0) AS total_calculado
    FROM pedido p
    INNER JOIN cliente c ON p.cliente_id = c.id
    LEFT JOIN detalle_pedido dp ON p.id = dp.pedido_id
    GROUP BY p.id, p.cliente_id, p.fecha, c.nombre
    """;

    private static final String DELETE_SQL =
            "DELETE FROM pedido WHERE id = ?";

    private static final String UPDATE_SQL =
            "UPDATE pedido SET cliente_id = ?, fecha = ? WHERE id = ?";

    private static final String SEARCH_SQL = """
                    SELECT id, cliente_id, fecha
                    FROM pedido
                    WHERE id = ?
                        OR cliente_id = ?
                    ORDER BY id                    
                    """;

    public void insert(Pedido p) throws SQLException {
        try (Connection con = Db.getConnection();
            PreparedStatement pst = con.prepareStatement(INSERT_SQL);){
            pst.setInt(1, p.getId());
            pst.setInt(2, p.getClienteId());
            pst.setDate(3, Date.valueOf(p.getFecha()));

            pst.executeUpdate();
        }
    }

    public void insert(Pedido p, Connection con) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(INSERT_SQL);){
            pst.setInt(1, p.getId());
            pst.setInt(2, p.getClienteId());
            pst.setDate(3, Date.valueOf(p.getFecha()));
            pst.executeUpdate();
        }
    }

    public void update(Pedido p, Connection con) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(UPDATE_SQL);){
            pst.setInt(1, p.getClienteId());
            pst.setDate(2, Date.valueOf(p.getFecha()));
            pst.setInt(3, p.getId());
            pst.executeUpdate();
        }
    }

    public void delete(Pedido p) throws SQLException {
        try (Connection con = Db.getConnection();
        PreparedStatement pst = con.prepareStatement(DELETE_SQL);){
            pst.setInt(1, p.getId());
            pst.executeUpdate();
        }
    }

    public Pedido findById(int id) throws SQLException {
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

    public List<Pedido> findAll() throws SQLException {
        List<Pedido> lista = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Pedido p = new Pedido();

                // Llenamos los datos normales
                p.setId(rs.getInt("id"));
                p.setClienteId(rs.getInt("cliente_id"));
                p.setFecha(rs.getDate("fecha").toLocalDate());

                // Llenamos los datos AUXILIARES
                p.setNombreCliente(rs.getString("nombre_cliente"));
                p.setTotalImporte(rs.getDouble("total_calculado"));

                lista.add(p);
            }
        }
        return lista;
    }

    public List<Pedido> search(String filtro) throws SQLException {

        String patron = "%" + filtro + "%";

        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SEARCH_SQL)) {
            pst.setString(1, patron);
            pst.setString(2, patron);
            //pst.setString(3, patron);

            List<Pedido> out = new ArrayList<>();

            try(ResultSet rs = pst.executeQuery()){

                while (rs.next()){
                    out.add(mapRow(rs));
                }
            }
            return out;
        }
    }

    private Pedido mapRow(ResultSet rs) throws SQLException {
        return new Pedido(
                rs.getInt("id"),
                rs.getInt("cliente_id"),
                rs.getDate("fecha").toLocalDate()
        );
    }
}