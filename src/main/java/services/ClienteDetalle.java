package services;

import dao.ClienteDAO;
import dao.DetalleClienteDAO;
import db.Db;
import model.Cliente;
import model.DetalleCliente;

import java.sql.Connection;
import java.sql.SQLException;

public class ClienteDetalle {
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final DetalleClienteDAO detalleClienteDAO = new DetalleClienteDAO();

    public void guardarClienteCompleto(Cliente c, DetalleCliente d) throws SQLException{
        try(Connection con = Db.getConnection();){

            //solo esta permitido en bases que acepten control de transacciones
            con.setAutoCommit(false);

            try{
                clienteDAO.insert(c,con);
                detalleClienteDAO.insert(d,con);

                con.commit(); //si las dos fucionan se hacen

            }catch(SQLException e){
                con.rollback(); //si da fallo volvemos a punto anterior
                throw e;
            }
            finally {
                con.setAutoCommit(true);
            }

        }


    }



}
