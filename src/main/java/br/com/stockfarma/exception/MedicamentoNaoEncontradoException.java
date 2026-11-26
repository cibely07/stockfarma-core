package br.com.stockfarma.exception;

public class MedicamentoNaoEncontradoException extends RuntimeException {
    public MedicamentoNaoEncontradoException(String codigo) {
        super("Medicamento não encontrado: " + codigo);
    }
}
