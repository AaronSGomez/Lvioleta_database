package model;

public class Repartidor {
    private int id;
    private String nombre;
    private String telefono;
    private int empresa_id;

    //cosntructor
    public Repartidor(int id, String nombre, String telefono, int empresa_id) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.empresa_id = empresa_id;
    }
    public Repartidor() {}

    //getter y setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getEmpresaId() {
        return empresa_id;
    }

    public void setEmpresaId(int empresa_id) {
        this.empresa_id = empresa_id;
    }

    @Override
    public String toString() {
        return  "Repartidor{id=%d, nombre=%d, telefono=%s, empresa_id=%.2f}"
                .formatted(id, nombre, telefono, empresa_id);
    }
}
