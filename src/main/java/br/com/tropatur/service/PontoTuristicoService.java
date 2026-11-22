package br.com.tropatur.service;

import java.util.List;
import br.com.tropatur.dto.*;
import br.com.tropatur.exception.PontoNaoEncontradoException;
import br.com.tropatur.model.PontoTuristico;
import br.com.tropatur.repository.PontoTuristicoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PontoTuristicoService {
    private final PontoTuristicoRepository repository;

    public PontoTuristicoService(PontoTuristicoRepository repository) { this.repository = repository; }

    public List<PontoTuristicoResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(PontoTuristicoResponse::from).toList();
    }

    public PontoTuristicoResponse consultar(Long id) { return PontoTuristicoResponse.from(buscar(id)); }

    @Transactional
    public PontoTuristicoResponse cadastrar(PontoTuristicoRequest request) {
        var ponto = new PontoTuristico(request.nome(), request.descricao(), request.endereco(), request.latitude(), request.longitude());
        return PontoTuristicoResponse.from(repository.save(ponto));
    }

    @Transactional
    public PontoTuristicoResponse atualizar(Long id, PontoTuristicoRequest request) {
        var ponto = buscar(id);
        ponto.atualizar(request.nome(), request.descricao(), request.endereco(), request.latitude(), request.longitude());
        return PontoTuristicoResponse.from(repository.save(ponto));
    }

    @Transactional
    public void excluir(Long id) { repository.delete(buscar(id)); }

    private PontoTuristico buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new PontoNaoEncontradoException(id));
    }
}
