package br.com.fiap3esa.autoescola3esa.domain.instrucao.validacao;

import br.com.fiap3esa.autoescola3esa.domain.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorRepository;
import org.springframework.stereotype.Component;

@Component
public class ValidadorInstrutorAtivo implements ValidadorAgendamento {
    private final InstrutorRepository repository;

    public ValidadorInstrutorAtivo(InstrutorRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        // instrutor opcional: quando ausente, o sistema escolhe um livre e ativo
        if (dados.instrutorId() != null && !repository.existsByIdAndAtivoTrue(dados.instrutorId())) {
            throw new ValidacaoException("Não é possível agendar instrução com instrutor inativo!");
        }
    }
}
