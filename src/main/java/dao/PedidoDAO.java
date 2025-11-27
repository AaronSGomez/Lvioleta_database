package dao;

import db.Db;
import model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {
    private static final String INSERT_SQL ="INSERT INTO producto (id, nombre, precio) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT id, nombre, precio FROM producto WHERE id=?";

    private static final String SELECT_ALL_SQL = "SELECT id, nombre, precio FROM producto";

    public void insert(Producto producto)throws SQLException {
        try(Connection con = Db.getConnection(); PreparedStatement ps= con.prepareStatement(INSERT_SQL)){
            ps.setInt(1, producto.getId());
            ps.setString(2, producto.getNombre());
            ps.setString(3, ""+producto.getPrecio());
            ps.executeUpdate();
        }
    }

    public Producto findById (int id)throws SQLException {
        try(Connection con = Db.getConnection(); PreparedStatement ps= con.prepareStatement(SELECT_BY_ID_SQL)){
            ps.setInt(1, id);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()) { //esto llama a la funcion next si no existe sale y devuelve el null
                    return new Producto(rs.getInt("id"), rs.getString("nombre"), rs.getDouble("precio"));
                }
            }
            return  null;
        }
    }

    public List<Producto> findAll()throws SQLException {
        List<Producto> out = new ArrayList<>();
        try(Connection con = Db.getConnection();  PreparedStatement ps= con.prepareStatement(SELECT_ALL_SQL); ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                out.add(new Producto(rs.getInt("id"), rs.getString("nombre"), rs.getDouble("precio")));
            }
        }
        return out;
    }

}
