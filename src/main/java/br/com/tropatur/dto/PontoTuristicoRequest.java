package br.com.tropatur.dto;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record PontoTuristicoRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 200, message = "Nome deve ter até 200 caracteres")
        @Schema(example = "Ponto de demonstração — fictício") String nome,
        @NotBlank(message = "Descrição é obrigatória") @Size(max = 10000, message = "Descrição deve ter até 10000 caracteres")
        @Schema(example = "Registro fictício para testar a API; não é conteúdo histórico verificado.") String descricao,
        @NotBlank(message = "Endereço é obrigatório") @Size(max = 300, message = "Endereço deve ter até 300 caracteres")
        @Schema(example = "Endereço fictício para demonstração") String endereco,
        @NotNull(message = "Latitude é obrigatória")
        @DecimalMin(value = "-90", message = "Latitude deve ser maior ou igual a -90")
        @DecimalMax(value = "90", message = "Latitude deve ser menor ou igual a 90")
        @Digits(integer = 3, fraction = 7, message = "Latitude aceita até 7 casas decimais")
        @Schema(example = "-23.59") BigDecimal latitude,
        @NotNull(message = "Longitude é obrigatória")
        @DecimalMin(value = "-180", message = "Longitude deve ser maior ou igual a -180")
        @DecimalMax(value = "180", message = "Longitude deve ser menor ou igual a 180")
        @Digits(integer = 3, fraction = 7, message = "Longitude aceita até 7 casas decimais")
        @Schema(example = "-48.05") BigDecimal longitude
) { }
