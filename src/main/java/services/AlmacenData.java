package services;

import model.AppData;
import model.Producto;
import dao.ProductoDAO;
import java.sql.SQLException;
import java.util.List;

public class AlmacenData {

    private static AppData data = new AppData();

    public static List<Producto> getProductos() {
        if (data.getProductos().isEmpty()) {
            // Si está vacía, es la primera vez. Cargamos desde BD.
            recargarProductos();
        }
        return data.getProductos();
    }

    public static void recargarProductos() {
        ProductoDAO dao = new ProductoDAO();
        try {
            List<Producto> nuevosDatos = dao.findAll();

            // Limpiamos y llenamos para mantener la misma referencia de objeto
            data.getProductos().clear();
            data.getProductos().addAll(nuevosDatos);

            System.out.println("📦 Productos cargados/recargados desde BD");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ... (Harías lo mismo para Clientes y Pedidos) ...


    // Método para acceder al objeto completo (para Exportar a JSON)
    public static AppData getAppData() {
        return data;
    }

    // Método para sobrescribir todo (para Importar desde JSON)
    public static void setAppData(AppData nuevosDatos) {
        data = nuevosDatos;
    }
}