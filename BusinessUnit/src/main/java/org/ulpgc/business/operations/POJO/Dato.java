package org.ulpgc.business.operations.DAO;

public class Dato {
    private long fecha;
    private int fkTipoDato;
    private int fkPeriodo;
    private int anyo;
    private double valor;
    private boolean secreto;

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }

    public int getFkTipoDato() { return fkTipoDato; }
    public void setFkTipoDato(int fkTipoDato) { this.fkTipoDato = fkTipoDato; }

    public int getFkPeriodo() { return fkPeriodo; }
    public void setFkPeriodo(int fkPeriodo) { this.fkPeriodo = fkPeriodo; }

    public int getAnyo() { return anyo; }
    public void setAnyo(int anyo) { this.anyo = anyo; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public boolean isSecreto() { return secreto; }
    public void setSecreto(boolean secreto) { this.secreto = secreto; }
}
