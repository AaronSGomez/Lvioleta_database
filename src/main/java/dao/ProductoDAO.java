package dao;

import db.Db;
import model.Cliente;
import model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {
    private static final String INSERT_SQL ="INSERT INTO producto (id, nombre, precio) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT id, nombre, precio FROM producto WHERE id=?";

    private static final String SELECT_ALL_SQL = "SELECT id, nombre, precio FROM producto";

    private static final String SEARCH_SQL = """
                    SELECT id, nombre, precio
                    FROM producto
                    WHERE CAST(id AS TEXT) ILIKE ? 
                        OR nombre ILIKE ?  
                    ORDER BY id                    
                    """;

    //OR precio ILIKE ?  - > eliminar esta opcion da problemas al buscar doubles, porque no es un String

    public void insert(Producto producto)throws SQLException {
        try(Connection con = Db.getConnection(); PreparedStatement ps= con.prepareStatement(INSERT_SQL)){
            ps.setInt(1, producto.getId());
            ps.setString(2, producto.getNombre());
            ps.setDouble(3, producto.getPrecio());
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
                out.add(mapRow(rs));
            }
        }
        return out;
    }

    public List<Producto> search(String filtro) throws SQLException {

        String patron = "%" + filtro + "%";

        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SEARCH_SQL)) {
            pst.setString(1, patron);
            pst.setString(2, patron);
            //pst.setString(3, patron);

            List<Producto> out = new ArrayList<>();

            try(ResultSet rs = pst.executeQuery()){

                while (rs.next()){
                    out.add(mapRow(rs));
                }
            }
            return out;
        }
    }

    private Producto mapRow(ResultSet rs) throws SQLException {

        Producto p = new Producto(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getDouble("precio")
        );

        return p;
    }


}
