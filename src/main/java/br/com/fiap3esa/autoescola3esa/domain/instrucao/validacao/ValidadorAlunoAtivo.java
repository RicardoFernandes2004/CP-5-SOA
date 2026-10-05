package br.com.fiap3esa.autoescola3esa.domain.instrucao.validacao;

import br.com.fiap3esa.autoescola3esa.domain.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoRepository;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosAgendamentoInstrucao;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAlunoAtivo implements ValidadorAgendamento {
    private final AlunoRepository repository;

    public ValidadorAlunoAtivo(AlunoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        if (!repository.existsByIdAndAtivoTrue(dados.alunoId())) {
            throw new ValidacaoException("Não é possível agendar instrução para aluno inativo!");
        }
    }
}
