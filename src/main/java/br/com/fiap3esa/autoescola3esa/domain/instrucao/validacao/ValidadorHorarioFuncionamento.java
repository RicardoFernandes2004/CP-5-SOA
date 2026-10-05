package br.com.fiap3esa.autoescola3esa.domain.instrucao.validacao;

import br.com.fiap3esa.autoescola3esa.domain.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosAgendamentoInstrucao;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Component
public class ValidadorHorarioFuncionamento implements ValidadorAgendamento {
    private static final int ABERTURA = 6;
    private static final int ULTIMO_HORARIO = 20; // instrução de 1h, encerramento às 21:00

    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        LocalDateTime data = dados.data();
        if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ValidacaoException("A auto-escola funciona de segunda a sábado!");
        }
        if (data.getHour() < ABERTURA || data.getHour() > ULTIMO_HORARIO) {
            throw new ValidacaoException(
                    "As instruções devem começar entre " + ABERTURA + ":00 e " + ULTIMO_HORARIO + ":00!");
        }
        if (data.getMinute() != 0) {
            throw new ValidacaoException("As instruções têm duração fixa de 1 hora e começam em hora cheia!");
        }
    }
}
