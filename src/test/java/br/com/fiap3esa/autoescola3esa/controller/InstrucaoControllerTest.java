package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.TestData;
import br.com.fiap3esa.autoescola3esa.domain.aluno.Aluno;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoRepository;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser
class InstrucaoControllerTest {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AlunoRepository alunoRepository;

    @Autowired
    InstrutorRepository instrutorRepository;

    @Test
    @DisplayName("Expectativa: retornar código 400 para dados inválidos.")
    void agendarDadosInvalidos() throws Exception {
        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Expectativa: agendar (201), recusar conflito de instrutor (400) e cancelar com motivo (200).")
    void agendarECancelar() throws Exception {
        Aluno aluno = alunoRepository.save(TestData.aluno("44444444444"));
        Aluno outroAluno = alunoRepository.save(TestData.aluno("55555555555"));
        Instrutor instrutor = instrutorRepository.save(TestData.instrutor("44444444444"));
        LocalDateTime data = TestData.proximaSegundaAs10();

        String location = mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(agendamento(aluno.getId(), instrutor.getId(), data)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.instrutorId").value(instrutor.getId()))
                .andReturn().getResponse().getHeader("Location");
        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(agendamento(outroAluno.getId(), instrutor.getId(), data)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O instrutor já possui outra instrução nessa data/hora!"));

        mockMvc.perform(delete("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":%d,\"motivo\":\"ALUNO_DESISTIU\"}".formatted(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivoCancelamento").value("ALUNO_DESISTIU"));
    }

    @Test
    @DisplayName("Expectativa: recusar o terceiro agendamento do aluno no mesmo dia.")
    void limiteDiario() throws Exception {
        Aluno aluno = alunoRepository.save(TestData.aluno("66666666666"));
        Instrutor instrutor = instrutorRepository.save(TestData.instrutor("66666666666"));
        LocalDateTime data = TestData.proximaSegundaAs10();

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                            .content(agendamento(aluno.getId(), instrutor.getId(), data.plusHours(i))))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(agendamento(aluno.getId(), instrutor.getId(), data.plusHours(2))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Expectativa: sem instrutor informado, o sistema escolhe um livre.")
    void instrutorAleatorio() throws Exception {
        Aluno aluno = alunoRepository.save(TestData.aluno("77777777777"));
        instrutorRepository.save(TestData.instrutor("77777777777"));

        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(agendamento(aluno.getId(), null, TestData.proximaSegundaAs10().withHour(15))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.instrutorId").exists());
    }

    private static String agendamento(Long alunoId, Long instrutorId, LocalDateTime data) {
        return "{\"alunoId\":%d,\"instrutorId\":%s,\"data\":\"%s\"}"
                .formatted(alunoId, instrutorId, data.format(FORMATO));
    }
}
