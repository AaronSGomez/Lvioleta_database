package dao;

import db.Db;
import model.DetallePedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de DetallePedido.
 * Tabla intermedia N:M entre Pedido y Producto.
 */
public class DetallePedidoDAO {

    private static final String INSERT_SQL =
            """
            INSERT INTO detalle_pedido
            (pedido_id, producto_id, cantidad, precio_unit)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SELECT_ALL_SQL =
            """
            SELECT pedido_id, producto_id, p.nombre AS nombre_producto, cantidad, precio_unit
            FROM detalle_pedido
            INNER JOIN producto p ON p.id = producto_id
            ORDER BY pedido_id, producto_id
            """;

    private static final String SELECT_BY_PEDIDO_SQL =
            """
            SELECT pedido_id, producto_id,p.nombre AS nombre_producto, cantidad, precio_unit
            FROM detalle_pedido
            INNER JOIN producto p ON p.id = producto_id
            WHERE pedido_id = ?
            ORDER BY producto_id
            """;

    private static final String DELETE_SQL =
            "DELETE FROM detalle_pedido WHERE pedido_id = ?";


    public void insert(DetallePedido dp) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(INSERT_SQL)) {

            pst.setInt(1, dp.getPedidoId());
            pst.setInt(2, dp.getProductoId());
            pst.setInt(3, dp.getCantidad());
            pst.setDouble(4, dp.getPrecioUnit());

            pst.executeUpdate();
        }
    }

    public void insert(DetallePedido dp, Connection con) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(INSERT_SQL)) {

            pst.setInt(1, dp.getPedidoId());
            pst.setInt(2, dp.getProductoId());
            pst.setInt(3, dp.getCantidad());
            pst.setDouble(4, dp.getPrecioUnit());

            pst.executeUpdate();
        }
    }

    public List<DetallePedido> findAll() throws SQLException {
        List<DetallePedido> out = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
              DetallePedido dp = new DetallePedido();
              dp.setPedidoId(rs.getInt("pedido_id"));
              dp.setProductoId(rs.getInt("producto_id"));
              dp.setNombreProducto(rs.getString("nombre_producto"));
              dp.setCantidad(rs.getInt("cantidad"));
              dp.setPrecioUnit(rs.getDouble("precio_unit"));
              out.add(dp);
            }
        }
        return out;
    }

    public List<DetallePedido> findById(int pedidoId) throws SQLException {
        List<DetallePedido> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_BY_PEDIDO_SQL);) {
            pst.setInt(1, pedidoId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                DetallePedido dp = new DetallePedido();
                dp.setPedidoId(rs.getInt("pedido_id"));
                dp.setProductoId(rs.getInt("producto_id"));
                dp.setNombreProducto(rs.getString("nombre_producto"));
                dp.setCantidad(rs.getInt("cantidad"));
                dp.setPrecioUnit(rs.getDouble("precio_unit"));
                out.add(dp);
            }
        }
        return out;
    }

    public void insertList(Integer idPedidoPadre, List<DetallePedido> detalles, Connection con) throws SQLException {
        try (var stmt = con.prepareStatement(INSERT_SQL)) {
            for (DetallePedido d : detalles) {
                stmt.setInt(1, idPedidoPadre);
                stmt.setInt(2, d.getProductoId());
                stmt.setInt(3, d.getCantidad());
                stmt.setDouble(4, d.getPrecioUnit());
                stmt.executeUpdate();
            }
        }
    }

    public void deleteById(Integer idPedidoPadre) throws SQLException {
        try (Connection con = Db.getConnection();
        PreparedStatement pst = con.prepareStatement(DELETE_SQL)) {
            pst.setInt(1, idPedidoPadre);
            pst.executeUpdate();
        }
    }

    private DetallePedido mapRow(ResultSet rs) throws SQLException {
        return new DetallePedido(
                rs.getInt("pedido_id"),
                rs.getInt("producto_id"),
                rs.getInt("cantidad"),
                rs.getDouble("precio_unit")
        );
    }
}