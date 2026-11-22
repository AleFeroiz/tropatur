package br.com.tropatur;

import br.com.tropatur.repository.PontoTuristicoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PontoTuristicoIntegrationTest {
    private static final String API = "/api/pontos-turisticos";
    private static final String VALIDO = """
            {"nome":" Ponto fictício ","descricao":"Somente teste","endereco":"Endereço fictício","latitude":-23.59,"longitude":-48.05}
            """;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired PontoTuristicoRepository repository;

    @BeforeEach void limparBanco() { repository.deleteAll(); }

    @Test void crudCompletoPersisteNoRepository() throws Exception {
        mvc.perform(get(API)).andExpect(status().isOk()).andExpect(content().json("[]"));
        var criado = mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome").value("Ponto fictício")).andReturn();
        long id = mapper.readTree(criado.getResponse().getContentAsString()).get("id").asLong();
        assertThat(repository.findById(id)).isPresent();
        mvc.perform(get(API)).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get(API + "/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(put(API + "/" + id).contentType(MediaType.APPLICATION_JSON).content(VALIDO.replace("Ponto fictício", "Nome atualizado")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Nome atualizado"));
        assertThat(repository.findById(id).orElseThrow().getNome()).isEqualTo("Nome atualizado");
        mvc.perform(delete(API + "/" + id)).andExpect(status().isNoContent()).andExpect(content().string(""));
        assertThat(repository.findById(id)).isEmpty();
        mvc.perform(get(API + "/" + id)).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"nome\":\"   \"}", "{invalid", "null"})
    void rejeitaCorposInvalidos(String json) throws Exception {
        mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());
        assertThat(repository.count()).isZero();
    }

    @Test void rejeitaCoordenadasForaDaFaixaESemPrecisaoSuportada() throws Exception {
        for (String json : new String[]{VALIDO.replace("-23.59", "90.0000001"), VALIDO.replace("-48.05", "-180.0000001"), VALIDO.replace("-23.59", "1.12345678")}) {
            mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());
        }
        assertThat(repository.count()).isZero();
    }

    @Test void aceitaLimitesGeograficos() throws Exception {
        mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(VALIDO.replace("-23.59", "-90").replace("-48.05", "180")))
                .andExpect(status().isCreated());
    }

    @Test void atualizacaoInvalidaNaoAlteraRegistro() throws Exception {
        var result = mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(VALIDO)).andReturn();
        long id = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(put(API + "/" + id).contentType(MediaType.APPLICATION_JSON).content(VALIDO.replace("-23.59", "91")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.latitude").exists());
        assertThat(repository.findById(id).orElseThrow().getLatitude()).isEqualByComparingTo("-23.59");
    }

    @Test void retorna404NasOperacoesSobreIdAusente() throws Exception {
        mvc.perform(get(API + "/999999")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
        mvc.perform(put(API + "/999999").contentType(MediaType.APPLICATION_JSON).content(VALIDO)).andExpect(status().isNotFound());
        mvc.perform(delete(API + "/999999")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "0", "-1", "1.5", "9999999999999999999999"})
    void rejeitaIdsInvalidos(String id) throws Exception {
        mvc.perform(get(API + "/" + id)).andExpect(status().isBadRequest());
    }

    @Test void rejeitaNomeLongoCampoDesconhecidoENumeroComoTexto() throws Exception {
        for (String json : new String[]{VALIDO.replace(" Ponto fictício ", "a".repeat(201)), VALIDO.replace("-23.59", "\"abc\""), VALIDO.replace("\"nome\"", "\"campoInexistente\"")}) {
            mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());
        }
    }

    @Test void disponibilizaInterfaceSwaggerEContratoComCincoOperacoes() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("TropaTur — API de pontos turísticos"))
                .andExpect(jsonPath("$.paths['/api/pontos-turisticos'].get").exists())
                .andExpect(jsonPath("$.paths['/api/pontos-turisticos'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/pontos-turisticos/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/pontos-turisticos/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/pontos-turisticos/{id}'].delete.responses['204']").exists());
    }
}
