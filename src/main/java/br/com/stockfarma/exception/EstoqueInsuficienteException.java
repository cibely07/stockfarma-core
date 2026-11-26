package br.com.stockfarma.exception;

// situação de negócio em que o chamador é obrigado a tratar
public class EstoqueInsuficienteException extends Exception {
    private final String codigo; 
    private final int solicitada;
    private final int disponivel; 

    public EstoqueInsuficienteException(String codigo, int solicitada, int disponivel) {
        super("Estoque insuficiente para " + codigo + ": solicitado " + solicitada + ", disponível " + disponivel);
        this.codigo = codigo;
        this.solicitada = solicitada;
        this.disponivel = disponivel;
    }

    public String getCodigo() { return codigo; }
    public int getSolicitada() { return solicitada; }
    public int getDisponivel() { return disponivel; }
}

