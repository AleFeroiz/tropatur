package br.com.tropatur.controller;

import java.net.URI;
import java.util.List;
import br.com.tropatur.dto.*;
import br.com.tropatur.exception.ApiError;
import br.com.tropatur.service.PontoTuristicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/pontos-turisticos")
@Tag(name = "Pontos turísticos", description = "Cadastro e consulta dos pontos do TropaTur")
@ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(schema = @Schema(implementation = ApiError.class)))
public class PontoTuristicoController {
    private final PontoTuristicoService service;
    public PontoTuristicoController(PontoTuristicoService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Cadastrar ponto turístico", description = "Cria um ponto e retorna seu endereço no cabeçalho Location.")
    @ApiResponse(responseCode = "201", description = "Ponto cadastrado")
    public ResponseEntity<PontoTuristicoResponse> cadastrar(@Valid @RequestBody PontoTuristicoRequest request) {
        var ponto = service.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(ponto.id()).toUri();
        return ResponseEntity.created(location).body(ponto);
    }

    @GetMapping
    @Operation(summary = "Listar todos os pontos", description = "Retorna uma lista ordenada por id; lista vazia quando não há registros.")
    @ApiResponse(responseCode = "200", description = "Lista retornada")
    public List<PontoTuristicoResponse> listar() { return service.listar(); }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar ponto pelo id")
    @ApiResponse(responseCode = "200", description = "Ponto encontrado")
    @ApiResponse(responseCode = "404", description = "Ponto não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public PontoTuristicoResponse consultar(@PathVariable @Positive Long id) { return service.consultar(id); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ponto", description = "Substitui todos os campos editáveis de um ponto existente; não cria outro registro.")
    @ApiResponse(responseCode = "200", description = "Ponto atualizado")
    @ApiResponse(responseCode = "404", description = "Ponto não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public PontoTuristicoResponse atualizar(@PathVariable @Positive Long id, @Valid @RequestBody PontoTuristicoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir ponto definitivamente")
    @ApiResponse(responseCode = "204", description = "Ponto excluído; sem corpo", content = @Content)
    @ApiResponse(responseCode = "404", description = "Ponto não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Void> excluir(@PathVariable @Positive Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
