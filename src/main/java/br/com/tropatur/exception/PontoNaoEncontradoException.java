package br.com.tropatur.exception;

public class PontoNaoEncontradoException extends RuntimeException {
    public PontoNaoEncontradoException(Long id) {
        super("Ponto turístico de id " + id + " não encontrado");
    }
}
