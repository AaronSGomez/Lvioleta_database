package dao;

import db.Db;
import model.Repartidor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepartidorDAO {

    private static final String INSERT_SQL =
            """
            INSERT INTO repartidor
            (nombre, telefono, empresa_id)
            VALUES (?, ?, ?)
            """;

    private static final String SELECT_ALL_SQL =
            """
            SELECT *
            FROM repartidor
            """;

    private static final String UPDATE_SQL =
            "UPDATE repartidor SET nombre = ?, telefono = ?, empresa_id = ? WHERE id = ?";

    private static final String SELECT_BY_EMPRESA_SQL =
            """
            SELECT *
            FROM repartidor
            WHERE empresa_id = ?
            ORDER BY id
            """;

    private static final String SELECT_BY_ID_SQL =
            """
            SELECT *
            FROM repartidor
            WHERE id = ?
            """;

    private static final String DELETE_SQL =
            "DELETE FROM detalle_pedido WHERE pedido_id = ?";


    public void insert(Repartidor r) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, r.getNombre());
            pst.setString(2, r.getTelefono());
            pst.setInt(3, r.getEmpresaId());

            pst.executeUpdate();


            //recuperar id generado
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    int nuevoId = rs.getInt(1); // El primer campo es el ID generado
                    r.setId(nuevoId);  // ¡Actualizamos el objeto Java!
                    System.out.println("Repartidor insertado con ID: " + nuevoId);
                }
            }
        }
    }

    public void insert(Repartidor r, Connection con) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(INSERT_SQL);){
            pst.setInt(1, r.getId());
            pst.setString(2, r.getNombre());
            pst.setString(3, r.getTelefono());
            pst.setInt(4,r.getEmpresaId());

            pst.executeUpdate();
        }
    }

    public void update(Repartidor r) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(UPDATE_SQL);){
            pst.setInt(1, r.getId());
            pst.setString(2, r.getNombre());
            pst.setString(3, r.getTelefono());
            pst.setInt(4,r.getEmpresaId());
            pst.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(DELETE_SQL);){
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    public Repartidor findById(int id) throws SQLException {
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

    public List<Repartidor> findByEmpresaId(int id) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_BY_EMPRESA_SQL)) {
            List <Repartidor> lista = new ArrayList<>();
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
                return lista;
            }
        }
    }

    public List<Repartidor> findAll() throws SQLException {
        List<Repartidor> lista = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Repartidor p = new Repartidor();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setTelefono(rs.getString("telefono"));
                p.setEmpresaId(rs.getInt("empresa_id"));
                lista.add(p);
            }
        }
        return lista;
    }

    private Repartidor mapRow(ResultSet rs) throws SQLException {
        return new Repartidor(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("telefono"),
                rs.getInt("empresa_id")
        );
    }
}