package br.com.tropatur.dto;

import java.math.BigDecimal;
import br.com.tropatur.model.PontoTuristico;

public record PontoTuristicoResponse(Long id, String nome, String descricao, String endereco,
                                      BigDecimal latitude, BigDecimal longitude) {
    public static PontoTuristicoResponse from(PontoTuristico ponto) {
        return new PontoTuristicoResponse(ponto.getId(), ponto.getNome(), ponto.getDescricao(),
                ponto.getEndereco(), ponto.getLatitude(), ponto.getLongitude());
    }
}
