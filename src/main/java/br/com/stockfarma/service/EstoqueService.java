package br.com.stockfarma.service;

import br.com.stockfarma.exception.EstoqueInsuficienteException;
import br.com.stockfarma.exception.MedicamentoNaoEncontradoException;
import br.com.stockfarma.model.Categoria;
import br.com.stockfarma.model.Medicamento;
import br.com.stockfarma.model.Movimentacao;
import br.com.stockfarma.model.TipoMovimentacao;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;

public class EstoqueService {
    // Map permite buscar diretamente pelo código.
    private final Map<String, Medicamento> medicamentos = new LinkedHashMap<>();
    // List mantém o histórico na ordem de inserção.
    private final List<Movimentacao> historico = new ArrayList<>();
    // Deque permite desfazer a última operação (LIFO).
    private final Deque<Movimentacao> movimentacoes = new ArrayDeque<>();

    public void carregar(List<Medicamento> lista) {
        medicamentos.clear();
        movimentacoes.clear();
        historico.clear();
        for (Medicamento medicamento : lista) {
            medicamentos.put(medicamento.codigo(), medicamento);
        }
    }

    public List<Medicamento> listar() {
        return new ArrayList<>(medicamentos.values());
    }

    public Medicamento buscar(String codigo) {
        Medicamento medicamento = medicamentos.get(codigo);
        if (medicamento == null) {
            throw new MedicamentoNaoEncontradoException(codigo);
        }
        return medicamento;
    }

    public void registrarEntrada(String codigo, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
        Medicamento atual = buscar(codigo);
        Medicamento atualizado = atual.comQuantidade(atual.quantidade() + quantidade);
        medicamentos.put(codigo, atualizado);
        Movimentacao movimentacao = new Movimentacao(TipoMovimentacao.ENTRADA, codigo,
                quantidade, LocalDateTime.now());
        historico.add(movimentacao);
        movimentacoes.push(movimentacao);
    }

    public void registrarSaida(String codigo, int quantidade) throws EstoqueInsuficienteException {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
        Medicamento atual = buscar(codigo);
        if (atual.quantidade() < quantidade) {
            throw new EstoqueInsuficienteException(codigo, quantidade, atual.quantidade());
        }
        Medicamento atualizado = atual.comQuantidade(atual.quantidade() - quantidade);
        medicamentos.put(codigo, atualizado);
        Movimentacao movimentacao = new Movimentacao(TipoMovimentacao.SAIDA, codigo,
                quantidade, LocalDateTime.now());
        historico.add(movimentacao);
        movimentacoes.push(movimentacao);
    }

    public void desfazerUltima() {
        if (movimentacoes.isEmpty()) {
            System.out.println("Nenhuma movimentação para desfazer.");
            return;
        }
        Movimentacao ultima = movimentacoes.pop();
        Medicamento atual = buscar(ultima.codigo());
        int quantidadeRevertida = ultima.tipo() == TipoMovimentacao.ENTRADA
                ? -ultima.quantidade()
                : ultima.quantidade();
        medicamentos.put(ultima.codigo(),
                atual.comQuantidade(atual.quantidade() + quantidadeRevertida));
        historico.add(new Movimentacao(ultima.tipo() == TipoMovimentacao.ENTRADA
                ? TipoMovimentacao.SAIDA : TipoMovimentacao.ENTRADA,
                ultima.codigo(), ultima.quantidade(), LocalDateTime.now()));
    }

    public List<Movimentacao> listarHistorico() {
        return new ArrayList<>(historico);
    }

    public Set<Categoria> categoriasComEstoqueBaixo(int limite) {
        // Set evita categorias repetidas.
        return medicamentos.values().stream()
                .filter(medicamento -> medicamento.quantidade() < limite)
                .map(Medicamento::categoria)
                .collect(java.util.stream.Collectors.toCollection(() -> EnumSet.noneOf(Categoria.class)));
    }
}
