package br.com.stockfarma.model;
import java.time.LocalDateTime;
public record Movimentacao(TipoMovimentacao tipo, String codigo, int quantidade, LocalDateTime dataHora){}