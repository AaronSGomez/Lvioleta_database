package model;

import java.time.LocalDate;
import java.util.Date;

public class Envio {
    // Campos BBDD
    private int id;
    private int pedidoId;
    private int repartidorId;
    private Date fechaSalida;
    private String numeroSeguimiento;
    private String estado; // "EN_REPARTO", "ENTREGADO"...por defecto en preparación, solo se crea envio si se envia, sino deberia crear almacen y controlar stock

    // TODO los campos direccion y nombre de cliente  y empresa, no deben ser modificables en envio, persistencia de datos.
    // Campos AUXILIARES
    private String nombreRepartidor;
    private String nombreEmpresa; // Útil saber si es DHL o SEUR
    private String telefonoRepartidor;
    // NUEVOS CAMPOS AUXILIARES
    private String nombreCliente;
    private String direccionCliente;

    //Constructor
    public Envio(int id, int pedidoId, int repartidorId, Date fechaSalida) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.repartidorId = repartidorId;
        this.fechaSalida = fechaSalida;
        this.estado = "EN PREPARACION";
    }

    //getter y setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(int pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Date getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(Date fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public int getRepartidorId() {
        return repartidorId;
    }

    public void setRepartidorId(int repartidorId) {
        this.repartidorId = repartidorId;
    }

    public String getNumeroSeguimiento() {
        return numeroSeguimiento;
    }

    public void setNumeroSeguimiento(String numeroSeguimiento) {
        this.numeroSeguimiento = numeroSeguimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNombreRepartidor() {
        return nombreRepartidor;
    }

    public void setNombreRepartidor(String nombreRepartidor) {
        this.nombreRepartidor = nombreRepartidor;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getTelefonoRepartidor() {
        return telefonoRepartidor;
    }

    public void setTelefonoRepartidor(String telefonoRepartidor) {
        this.telefonoRepartidor = telefonoRepartidor;
    }

    public String getNombreCliente() { return nombreCliente; }

    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getDireccionCliente() { return direccionCliente; }

    public void setDireccionCliente(String direccionCliente) { this.direccionCliente = direccionCliente; }
}
