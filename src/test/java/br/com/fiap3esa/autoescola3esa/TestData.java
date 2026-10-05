package br.com.fiap3esa.autoescola3esa;

import br.com.fiap3esa.autoescola3esa.domain.aluno.Aluno;
import br.com.fiap3esa.autoescola3esa.domain.endereco.Endereco;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Especialidade;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

// Dados de apoio compartilhados pelos testes.
public final class TestData {
    public static final String ENDERECO_JSON = """
            {"logradouro":"Rua Teste","numero":"10","bairro":"Vila Teste",
             "cidade":"Sao Paulo","uf":"SP","cep":"01001000"}""";

    private TestData() {
    }

    // Segunda-feira às 10h, com mais de 24h de folga (permite agendar e cancelar).
    public static LocalDateTime proximaSegundaAs10() {
        return LocalDateTime.now()
                .plusDays(2)
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    public static Endereco endereco() {
        return new Endereco("Rua Teste", "000", "Casa dos Fundos", "Vila Teste", "Test City", "TS", "01234567");
    }

    public static Aluno aluno(String cpf) {
        return new Aluno(null, "Aluno Teste", "aluno" + cpf + "@email.com", "(11) 98765-4321",
                cpf, endereco(), true);
    }

    public static Instrutor instrutor(String cnh) {
        return new Instrutor(null, "Instrutor Teste", "instrutor" + cnh + "@email.com", "(11) 91234-5678",
                cnh, Especialidade.MOTOS, endereco(), true);
    }
}
