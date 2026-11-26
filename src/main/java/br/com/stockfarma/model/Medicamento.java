package br.com.stockfarma.model;

import java.time.LocalDate;

public record Medicamento(String codigo, String nome, Categoria categoria,
                          double preco, int quantidade, LocalDate validade) {

    // impede que exista um objeto inválido
    public Medicamento {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("Código obrigatório");
        if (preco <= 0) throw new IllegalArgumentException("Preço deve ser positivo");
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade negativa");
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome obrigatório");
        if (categoria == null) throw new IllegalArgumentException("Categoria obrigatória");
        if (validade == null) throw new IllegalArgumentException("Validade obrigatória");
    }

    public boolean vencido() { return validade.isBefore(LocalDate.now()); }

    // Records são imutáveis: então para "alterar" a quantidade, criamos um objeto novo
    public Medicamento comQuantidade(int novaQuantidade) {
        return new Medicamento(codigo, nome, categoria, preco, novaQuantidade, validade);
    }
}