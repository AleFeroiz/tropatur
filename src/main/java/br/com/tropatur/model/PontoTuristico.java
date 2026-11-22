package br.com.tropatur.model;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "ponto_turistico")
public class PontoTuristico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ponto")
    private Long id;
    @Column(nullable = false, length = 200)
    private String nome;
    @Column(nullable = false, columnDefinition = "text")
    private String descricao;
    @Column(nullable = false, length = 300)
    private String endereco;
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    protected PontoTuristico() { }

    public PontoTuristico(String nome, String descricao, String endereco, BigDecimal latitude, BigDecimal longitude) {
        atualizar(nome, descricao, endereco, latitude, longitude);
    }

    public void atualizar(String nome, String descricao, String endereco, BigDecimal latitude, BigDecimal longitude) {
        this.nome = nome.strip();
        this.descricao = descricao.strip();
        this.endereco = endereco.strip();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getEndereco() { return endereco; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
}
