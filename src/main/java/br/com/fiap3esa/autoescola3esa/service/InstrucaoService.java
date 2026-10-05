package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoRepository;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.*;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.validacao.ValidadorAgendamento;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.validacao.ValidadorCancelamento;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class InstrucaoService {
    private final InstrucaoRepository repository;
    private final AlunoRepository alunoRepository;
    private final InstrutorRepository instrutorRepository;
    private final List<ValidadorAgendamento> validadoresAgendamento;
    private final List<ValidadorCancelamento> validadoresCancelamento;

    public InstrucaoService(InstrucaoRepository repository,
                            AlunoRepository alunoRepository,
                            InstrutorRepository instrutorRepository,
                            List<ValidadorAgendamento> validadoresAgendamento,
                            List<ValidadorCancelamento> validadoresCancelamento) {
        this.repository = repository;
        this.alunoRepository = alunoRepository;
        this.instrutorRepository = instrutorRepository;
        this.validadoresAgendamento = validadoresAgendamento;
        this.validadoresCancelamento = validadoresCancelamento;
    }

    @Transactional
    public DadosDetalhamentoInstrucao agendar(DadosAgendamentoInstrucao dados) {
        if (!alunoRepository.existsById(dados.alunoId())) {
            throw new AlunoNotFoundException("ID do aluno informado não existe!");
        }
        if (dados.instrutorId() != null && !instrutorRepository.existsById(dados.instrutorId())) {
            throw new InstrutorNotFoundException("ID do instrutor informado não existe!");
        }

        validadoresAgendamento.forEach(validador -> validador.validar(dados));

        Instrucao instrucao = new Instrucao(
                alunoRepository.getReferenceById(dados.alunoId()),
                escolherInstrutor(dados),
                dados.data());
        return new DadosDetalhamentoInstrucao(repository.save(instrucao));
    }

    @Transactional
    public DadosDetalhamentoInstrucao cancelar(DadosCancelamentoInstrucao dados) {
        Instrucao instrucao = repository.findById(dados.id())
                .orElseThrow(() ->
                        new InstrucaoNotFoundException("ID da instrução informado não existe!"));
        if (instrucao.isCancelada()) {
            throw new ValidacaoException("Instrução já está cancelada!");
        }

        validadoresCancelamento.forEach(validador -> validador.validar(instrucao));

        instrucao.cancelar(dados.motivo());
        return new DadosDetalhamentoInstrucao(repository.save(instrucao));
    }

    public Page<DadosDetalhamentoInstrucao> listarInstrucoes(Pageable paginacao) {
        return repository.findAllByMotivoCancelamentoIsNull(paginacao)
                .map(DadosDetalhamentoInstrucao::new);
    }

    public DadosDetalhamentoInstrucao detalharInstrucao(Long id) {
        return repository.findById(id)
                .map(DadosDetalhamentoInstrucao::new)
                .orElseThrow(() ->
                        new InstrucaoNotFoundException("ID da instrução informado não existe!"));
    }

    private Instrutor escolherInstrutor(DadosAgendamentoInstrucao dados) {
        if (dados.instrutorId() != null) {
            return instrutorRepository.getReferenceById(dados.instrutorId());
        }
        List<Instrutor> livres = repository.buscarInstrutoresLivresNaData(dados.data());
        if (livres.isEmpty()) {
            throw new ValidacaoException("Nenhum instrutor disponível nessa data/hora!");
        }
        return livres.get(ThreadLocalRandom.current().nextInt(livres.size()));
    }
}
