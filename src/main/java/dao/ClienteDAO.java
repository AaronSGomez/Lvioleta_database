package dao;

import db.Db;
import model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private static final String INSERT_SQL ="INSERT INTO cliente (id, nombre, email) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT id, nombre, email FROM cliente WHERE id=?";

    private static final String SELECT_ALL_SQL = "SELECT id, nombre, email FROM cliente";

    public void insert(Cliente cliente)throws SQLException {
        try(Connection con = Db.getConnection(); PreparedStatement ps= con.prepareStatement(INSERT_SQL)){
            ps.setInt(1, cliente.getId());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getEmail());
            ps.executeUpdate();
        }
    }

    public Cliente findById (int id)throws SQLException {
        try(Connection con = Db.getConnection(); PreparedStatement ps= con.prepareStatement(SELECT_BY_ID_SQL)){
            ps.setInt(1, id);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()) { //esto llama a la funcion next si no existe sale y devuelve el null
                    return new Cliente(rs.getInt("id"), rs.getString("nombre"), rs.getString("email"));
                }
            }
            return  null;
        }
    }

    public List<Cliente> findAll()throws SQLException {
        List<Cliente> out = new ArrayList<>();
        try(Connection con = Db.getConnection();  PreparedStatement ps= con.prepareStatement(SELECT_ALL_SQL); ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                out.add(new Cliente(rs.getInt("id"), rs.getString("nombre"), rs.getString("email")));
            }
        }
        return out;
    }



}