package dao;

import db.Db;
import model.EmpresaReparto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpresaRepartoDAO {

    private static final String INSERT_SQL =
            """
            INSERT INTO empresa_reparto
            (nombre, telefono, direccion)
            VALUES (?, ?, ?)
            """;

    private static final String SELECT_ALL_SQL =
            """
            SELECT *
            FROM empresa_reparto
            """;

    private static final String UPDATE_SQL =
            "UPDATE empresa_reparto SET nombre = ?, telefono = ?, direccion = ? WHERE id = ?";


    private static final String SELECT_BY_ID_SQL =
            """
            SELECT *
            FROM empresa_reparto
            WHERE id = ?
            """;

    private static final String DELETE_SQL =
            "DELETE FROM empresa_reparto WHERE id = ?";


    public void insert(EmpresaReparto r) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);){
            pst.setString(1, r.getRazonSocial());
            pst.setString(2, r.getTelefono());
            pst.setString(3,r.getDireccion());

            pst.executeUpdate();

            //recuperar id generado
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    int nuevoId = rs.getInt(1); // El primer campo es el ID generado
                    r.setId(nuevoId);  // ¡Actualizamos el objeto Java!
                }
            }
        }
    }

    public void insert(EmpresaReparto r, Connection con) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(INSERT_SQL);){
            pst.setInt(1, r.getId());
            pst.setString(2, r.getRazonSocial());
            pst.setString(3, r.getTelefono());
            pst.setString(4,r.getDireccion());

            pst.executeUpdate();
        }
    }

    public void update(EmpresaReparto r) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(UPDATE_SQL);){
            pst.setInt(1, r.getId());
            pst.setString(2, r.getRazonSocial());
            pst.setString(3, r.getTelefono());
            pst.setString(4,r.getDireccion());
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

    public EmpresaReparto findById(int id) throws SQLException {
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

    public List<EmpresaReparto> findAll() throws SQLException {
        List<EmpresaReparto> lista = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                EmpresaReparto p = new EmpresaReparto();
                p.setId(rs.getInt("id"));
                p.setRazonSocial(rs.getString("nombre"));
                p.setTelefono(rs.getString("telefono"));
                p.setDireccion(rs.getString("direccion"));
                lista.add(p);
            }
        }
        return lista;
    }

    private EmpresaReparto mapRow(ResultSet rs) throws SQLException {
        return new EmpresaReparto(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("telefono"),
                rs.getString("direccion")
        );
    }
}
