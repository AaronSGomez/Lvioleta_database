package app;

import model.*;

import java.time.LocalDate;

/**
 * Crea datos en memoria para explicar:
 *  - 1:1  Cliente ↔ DetalleCliente
 *  - 1:N  Cliente ↔ Pedido
 *  - N:M  Pedido ↔ Producto (vía DetallePedido)
 */
public class DemoRelaciones {
    public static void main(String[] args) {
        // --- 1:1 Cliente ↔ DetalleCliente
        Cliente cli = new Cliente(1, "Ana Pérez", "ana@example.com");
        DetalleCliente det = new DetalleCliente(1, "C/ Sol 3", "600111222", "VIP");
        cli.setDetalle(det);

        // --- Productos del catálogo
        Producto p1 = new Producto(10, "Teclado", 25.0);
        Producto p2 = new Producto(11, "Ratón", 15.0);
        Producto p3 = new Producto(12, "Auriculares", 40.0);

        // --- 1:N Cliente ↔ Pedido (cliente 1 hace 2 pedidos)
        Pedido ped1 = new Pedido(100, cli.getId(), LocalDate.now());
        Pedido ped2 = new Pedido(101, cli.getId(), LocalDate.now().minusDays(1));

        // --- N:M Pedido ↔ Producto: añadimos líneas
        ped1.getLineas().add(new DetallePedido(ped1.getId(), p1.getId(), 1, p1.getPrecio()));
        ped1.getLineas().add(new DetallePedido(ped1.getId(), p3.getId(), 2, p3.getPrecio()));

        ped2.getLineas().add(new DetallePedido(ped2.getId(), p2.getId(), 3, p2.getPrecio()));

        // Vinculamos los pedidos al cliente (1:N)
        cli.getPedidos().add(ped1);
        cli.getPedidos().add(ped2);

        // --- Mostrar
        System.out.println("CLIENTE:");
        System.out.println("  " + cli);
        System.out.println("  Detalle 1:1 -> " + cli.getDetalle());
        System.out.println("  Pedidos 1:N ->");
        cli.getPedidos().forEach(p -> {
            System.out.println("   " + p);
            p.getLineas().forEach(l -> System.out.println("     " + l));
        });
        System.out.printf("  Total gastado: %.2f%n",
                cli.getPedidos().stream().mapToDouble(Pedido::getTotal).sum());
    }
}
