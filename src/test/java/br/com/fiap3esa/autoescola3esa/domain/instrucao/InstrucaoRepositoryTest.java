package br.com.fiap3esa.autoescola3esa.domain.instrucao;

import br.com.fiap3esa.autoescola3esa.TestData;
import br.com.fiap3esa.autoescola3esa.domain.aluno.Aluno;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class InstrucaoRepositoryTest {
    @Autowired
    InstrucaoRepository repository;

    @Autowired
    TestEntityManager testEntity;

    private final LocalDateTime segunda10h = TestData.proximaSegundaAs10();

    @Test
    @DisplayName("Expectativa: instrutor com instrução no horário não aparece como livre.")
    void instrutorOcupado() {
        Instrutor instrutor = testEntity.persist(TestData.instrutor("11111111111"));
        Aluno aluno = testEntity.persist(TestData.aluno("11111111111"));
        testEntity.persist(new Instrucao(aluno, instrutor, segunda10h));

        assertThat(repository.buscarInstrutoresLivresNaData(segunda10h)).doesNotContain(instrutor);
    }

    @Test
    @DisplayName("Expectativa: instrutor sem instrução no horário aparece como livre.")
    void instrutorLivre() {
        Instrutor instrutor = testEntity.persist(TestData.instrutor("22222222222"));

        assertThat(repository.buscarInstrutoresLivresNaData(segunda10h)).contains(instrutor);
    }

    @Test
    @DisplayName("Expectativa: instrução cancelada libera o instrutor e não conta no limite diário.")
    void canceladaLiberaHorario() {
        Instrutor instrutor = testEntity.persist(TestData.instrutor("33333333333"));
        Aluno aluno = testEntity.persist(TestData.aluno("33333333333"));
        Instrucao instrucao = new Instrucao(aluno, instrutor, segunda10h);
        instrucao.cancelar(MotivoCancelamento.ALUNO_DESISTIU);
        testEntity.persist(instrucao);

        assertThat(repository.buscarInstrutoresLivresNaData(segunda10h)).contains(instrutor);
        assertThat(repository.countByAlunoIdAndDataBetweenAndMotivoCancelamentoIsNull(
                aluno.getId(), segunda10h.withHour(0), segunda10h.withHour(23))).isZero();
    }
}
