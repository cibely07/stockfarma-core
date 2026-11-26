package br.com.stockfarma.service;

import br.com.stockfarma.model.Categoria;
import br.com.stockfarma.model.Medicamento;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RelatorioService {

    private final EstoqueService estoque;

    public RelatorioService(EstoqueService estoque) { this.estoque = estoque; }

  
    // Na forma imperativa acumulamos o valor com for.
    public double valorTotalEstoqueImperativo() {
        double total = 0;
        for (Medicamento m : estoque.listar()) {
            total += m.preco() * m.quantidade();
        }
        return total;
    }

    // Na forma declarativa a Stream calcula e soma os valores.
    public double valorTotalEstoque() {
        return estoque.listar().stream()
                .mapToDouble(m -> m.preco() * m.quantidade())
                .sum();
    }

    public List<Medicamento> vencendoEm(int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(dias);
        return estoque.listar().stream()
                .filter(m -> !m.validade().isBefore(hoje) && !m.validade().isAfter(limite))
                .sorted(Comparator.comparing(Medicamento::validade))
                .toList();
    }

    public Map<Categoria, Long> quantidadePorCategoria() {
        return estoque.listar().stream()
                .collect(Collectors.groupingBy(Medicamento::categoria, Collectors.counting()));
    }

    public List<Medicamento> maisCaros(int n) {
        return estoque.listar().stream()
                .sorted(Comparator.comparing(Medicamento::preco).reversed())
                .limit(n)
                .toList();
    }

    public String nomesComEstoqueBaixo(int limite) {
        return estoque.listar().stream()
                .filter(m -> m.quantidade() < limite)
                .map(Medicamento::nome)
                .collect(Collectors.joining(", "));
    }

    public Optional<Medicamento> maisProximoDeVencer() {
        return estoque.listar().stream()
                .filter(m -> !m.vencido())
                .min(Comparator.comparing(Medicamento::validade));
    }

    public List<Medicamento> filtrar(Predicate<Medicamento> criterio) {
        return estoque.listar().stream().filter(criterio).toList();
    }
}