package services;

import dao.DetallePedidoDAO;
import dao.PedidoDAO;
import db.Db;
import model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PedidoDetalle {
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final DetallePedidoDAO detallePedidoDAO = new DetallePedidoDAO();

    public void insertPedidoCompleto(Pedido p, List<DetallePedido> d) throws SQLException{
        try(Connection con = Db.getConnection();){
            //solo esta permitido en bases que acepten control de transacciones
            con.setAutoCommit(false);
            try{
                //TODO
                pedidoDAO.insert(p,con);
                detallePedidoDAO.insertList(p.getId(),d,con);

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

    public void updatePedidoCompleto(Pedido p, List<DetallePedido> d) throws SQLException{
        try(Connection con = Db.getConnection();){
            //solo esta permitido en bases que acepten control de transacciones
            con.setAutoCommit(false);
            try{
                //TODO
                pedidoDAO.update(p,con);
                //borrar y insertar. como la lista esta en memoria es mas rapido que actualizar
                detallePedidoDAO.deleteById(p.getId());
                detallePedidoDAO.insertList(p.getId(),d,con);

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
