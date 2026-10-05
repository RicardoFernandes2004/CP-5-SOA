package br.com.fiap3esa.autoescola3esa.domain.instrucao.validacao;

import br.com.fiap3esa.autoescola3esa.domain.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.InstrucaoRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ValidadorLimiteDiarioAluno implements ValidadorAgendamento {
    private static final int MAX_INSTRUCOES_POR_DIA = 2;

    private final InstrucaoRepository repository;

    public ValidadorLimiteDiarioAluno(InstrucaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        LocalDateTime inicioDoDia = dados.data().toLocalDate().atStartOfDay();
        long noDia = repository.countByAlunoIdAndDataBetweenAndMotivoCancelamentoIsNull(
                dados.alunoId(), inicioDoDia, inicioDoDia.plusDays(1).minusNanos(1));
        if (noDia >= MAX_INSTRUCOES_POR_DIA) {
            throw new ValidacaoException(
                    "O aluno já possui " + MAX_INSTRUCOES_POR_DIA + " instruções agendadas nesse dia!");
        }
    }
}
