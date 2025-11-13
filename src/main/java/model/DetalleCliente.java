package model;

/**
 * Detalle ampliado de un Cliente.
 * Clave 1:1: o bien comparte el mismo id que cliente, o lleva FK única a cliente.
 */
public class DetalleCliente {
    private Integer id;          // PK (puede ser igual a cliente.id)
    private String direccion;
    private String telefono;
    private String notas;

    // NO guardamos Cliente aquí para evitar ciclos fuertes en toString/equals

    public DetalleCliente() {}
    public DetalleCliente(Integer id, String direccion, String telefono, String notas) {
        this.id = id; this.direccion = direccion; this.telefono = telefono; this.notas = notas;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    @Override public String toString() {
        return "DetalleCliente{id=%d, dir='%s', tel='%s', notas='%s'}"
                .formatted(id, direccion, telefono, notas);
    }
}
