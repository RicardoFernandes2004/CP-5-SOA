package br.com.fiap3esa.autoescola3esa.domain.instrucao.validacao;

import br.com.fiap3esa.autoescola3esa.domain.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosAgendamentoInstrucao;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ValidadorAntecedenciaAgendamento implements ValidadorAgendamento {
    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        if (dados.data().isBefore(LocalDateTime.now().plusMinutes(30))) {
            throw new ValidacaoException("A instrução deve ser agendada com antecedência mínima de 30 minutos!");
        }
    }
}
