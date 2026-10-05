package br.com.fiap3esa.autoescola3esa.domain.instrucao.validacao;

import br.com.fiap3esa.autoescola3esa.domain.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.Instrucao;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ValidadorAntecedenciaCancelamento implements ValidadorCancelamento {
    @Override
    public void validar(Instrucao instrucao) {
        if (instrucao.getData().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new ValidacaoException("A instrução só pode ser cancelada com antecedência mínima de 24 horas!");
        }
    }
}
