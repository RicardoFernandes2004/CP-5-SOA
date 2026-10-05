package br.com.fiap3esa.autoescola3esa.domain.instrucao.validacao;

import br.com.fiap3esa.autoescola3esa.domain.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.InstrucaoRepository;
import org.springframework.stereotype.Component;

@Component
public class ValidadorDisponibilidadeInstrutor implements ValidadorAgendamento {
    private final InstrucaoRepository repository;

    public ValidadorDisponibilidadeInstrutor(InstrucaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        if (dados.instrutorId() != null && repository.existsByInstrutorIdAndDataAndMotivoCancelamentoIsNull(
                dados.instrutorId(), dados.data())) {
            throw new ValidacaoException("O instrutor já possui outra instrução nessa data/hora!");
        }
    }
}
