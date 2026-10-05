package br.com.fiap3esa.autoescola3esa.domain.instrucao.validacao;

import br.com.fiap3esa.autoescola3esa.TestData;
import br.com.fiap3esa.autoescola3esa.domain.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.Instrucao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Testes unitários (sem banco) das regras de data/hora de agendamento e cancelamento.
class ValidadoresHorarioTest {
    private final ValidadorHorarioFuncionamento horario = new ValidadorHorarioFuncionamento();
    private final ValidadorAntecedenciaAgendamento antecedencia = new ValidadorAntecedenciaAgendamento();
    private final ValidadorAntecedenciaCancelamento cancelamento = new ValidadorAntecedenciaCancelamento();

    private final LocalDateTime segunda10h = TestData.proximaSegundaAs10();

    private static DadosAgendamentoInstrucao em(LocalDateTime data) {
        return new DadosAgendamentoInstrucao(1L, null, data);
    }

    @Test
    @DisplayName("Expectativa: aceitar horários entre 06:00 e 20:00 de segunda a sábado.")
    void horarioValido() {
        assertDoesNotThrow(() -> horario.validar(em(segunda10h)));
        assertDoesNotThrow(() -> horario.validar(em(segunda10h.withHour(6))));
        assertDoesNotThrow(() -> horario.validar(em(segunda10h.withHour(20))));
        assertDoesNotThrow(() -> horario.validar(em(segunda10h.plusDays(5)))); // sábado
    }

    @Test
    @DisplayName("Expectativa: recusar domingo.")
    void domingo() {
        assertThrows(ValidacaoException.class, () -> horario.validar(em(segunda10h.minusDays(1))));
    }

    @Test
    @DisplayName("Expectativa: recusar antes das 06:00 e início às 21:00 (terminaria às 22:00).")
    void foraDoFuncionamento() {
        assertThrows(ValidacaoException.class, () -> horario.validar(em(segunda10h.withHour(5))));
        assertThrows(ValidacaoException.class, () -> horario.validar(em(segunda10h.withHour(21))));
    }

    @Test
    @DisplayName("Expectativa: recusar horário quebrado (instrução dura 1h, em hora cheia).")
    void horaQuebrada() {
        assertThrows(ValidacaoException.class, () -> horario.validar(em(segunda10h.withMinute(30))));
    }

    @Test
    @DisplayName("Expectativa: exigir 30 minutos de antecedência no agendamento.")
    void antecedenciaAgendamento() {
        assertThrows(ValidacaoException.class,
                () -> antecedencia.validar(em(LocalDateTime.now().plusMinutes(29))));
        assertDoesNotThrow(() -> antecedencia.validar(em(LocalDateTime.now().plusMinutes(31))));
    }

    @Test
    @DisplayName("Expectativa: exigir 24 horas de antecedência no cancelamento.")
    void antecedenciaCancelamento() {
        assertThrows(ValidacaoException.class,
                () -> cancelamento.validar(instrucaoEm(LocalDateTime.now().plusHours(23))));
        assertDoesNotThrow(() -> cancelamento.validar(instrucaoEm(LocalDateTime.now().plusHours(25))));
    }

    private static Instrucao instrucaoEm(LocalDateTime data) {
        return new Instrucao(null, null, data);
    }
}
