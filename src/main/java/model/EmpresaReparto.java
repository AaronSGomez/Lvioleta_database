package model;

public class EmpresaReparto {

    private int id;
    private String razonSocial;
    private String direccion;
    private String telefono;

    //constructor
    public EmpresaReparto(int id, String razonSocial, String telefono,  String direccion) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.telefono = telefono;
        this.direccion = direccion;
    }
    public EmpresaReparto() {}

    //getter y setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {this.telefono = telefono;}

    public String getDireccion() {return direccion;}

    public void setDireccion(String direccion) {this.direccion = direccion;}

    @Override
    public String toString() {
        return  "Repartidor{id=%d, nombre=%s, direccion=%s, telefono=%s}"
                .formatted(id, razonSocial,direccion, telefono);
    }
}
