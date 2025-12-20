package model;

public class Repartidor {
    private int id;
    private String nombre;
    private String telefone;
    private int empresa_id;

    //cosntructor
    public Repartidor(int id, String nombre, String telefone, int empresa_id) {
        this.id = id;
        this.nombre = nombre;
        this.telefone = telefone;
        this.empresa_id = empresa_id;
    }


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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public int getEmpresa_id() {
        return empresa_id;
    }

    public void setEmpresa_id(int empresa_id) {
        this.empresa_id = empresa_id;
    }

    @Override
    public String toString() {
        return  "Repartidor{id=%d, nombre=%d, telefono=%s, empresa_id=%.2f}"
                .formatted(id, nombre, telefone, empresa_id);
    }
}
