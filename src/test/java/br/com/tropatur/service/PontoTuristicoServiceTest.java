package br.com.tropatur.service;

import br.com.tropatur.dto.PontoTuristicoRequest;
import br.com.tropatur.exception.PontoNaoEncontradoException;
import br.com.tropatur.model.PontoTuristico;
import br.com.tropatur.repository.PontoTuristicoRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PontoTuristicoServiceTest {
    private PontoTuristicoRepository repository;
    private PontoTuristicoService service;

    @BeforeEach void preparar() {
        repository = mock(PontoTuristicoRepository.class);
        service = new PontoTuristicoService(repository);
    }

    @Test void atualizarRegistroAusenteNaoCriaOutro() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        var request = new PontoTuristicoRequest("Exemplo", "Fictício", "Endereço", BigDecimal.ZERO, BigDecimal.ZERO);
        assertThatThrownBy(() -> service.atualizar(42L, request))
            .isInstanceOf(PontoNaoEncontradoException.class);
        verify(repository, never()).save(any());
    }

    @Test void excluirRegistroAusenteNaoExecutaRemocao() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.excluir(42L))
            .isInstanceOf(PontoNaoEncontradoException.class);
        verify(repository, never()).delete(any(PontoTuristico.class));
    }

    @Test void atualizarPreservaInstanciaExistenteENormalizaTexto() {
        var existente = new PontoTuristico("Anterior", "Anterior", "Anterior", BigDecimal.ZERO, BigDecimal.ZERO);
        when(repository.findById(42L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);
        var request = new PontoTuristicoRequest("  Novo nome  ", "  Descrição nova  ", "  Endereço novo  ", new BigDecimal("-23.59"), new BigDecimal("-48.05"));
        var response = service.atualizar(42L, request);
        verify(repository).save(same(existente));
        assertThat(response.nome()).isEqualTo("Novo nome");
        assertThat(response.descricao()).isEqualTo("Descrição nova");
        assertThat(response.endereco()).isEqualTo("Endereço novo");
        assertThat(response.latitude()).isEqualByComparingTo("-23.59");
        assertThat(response.longitude()).isEqualByComparingTo("-48.05");
    }
}
