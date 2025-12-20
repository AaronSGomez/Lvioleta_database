package model;

public class EmpresaReparto {

    private int id;
    private String razonSocial;
    private String telefone;

    //constructor
    public EmpresaReparto(int id, String razonSocial, String telefone) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.telefone = telefone;
    }

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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @Override
    public String toString() {
        return  "Repartidor{id=%d, nombre=%s, telefono=%s}"
                .formatted(id, razonSocial, telefone);
    }
}
