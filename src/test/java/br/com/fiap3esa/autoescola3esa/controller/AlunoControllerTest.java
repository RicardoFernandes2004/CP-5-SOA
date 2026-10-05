package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.TestData;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AlunoControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("Expectativa: 403 sem autenticação.")
    void semToken() throws Exception {
        mockMvc.perform(get("/alunos")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("Expectativa: 400 para dados inválidos no cadastro.")
    void cadastroInvalido() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Expectativa: cadastrar (201), listar, atualizar e excluir logicamente (some da listagem).")
    void ciclo() throws Exception {
        String body = """
                {"nome":"Ana","email":"ana.teste@email.com","telefone":"11999999999",
                 "cpf":"98765432100","endereco":%s}""".formatted(TestData.ENDERECO_JSON);
        String location = mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("98765432100"))
                .andReturn().getResponse().getHeader("Location");
        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(put("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":%d,\"nome\":\"Ana Maria\"}".formatted(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Maria"));

        mockMvc.perform(delete("/alunos/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/alunos").param("size", "1000"))
                .andExpect(jsonPath("$.content[?(@.id == %d)]".formatted(id)).doesNotExist());
    }
}
