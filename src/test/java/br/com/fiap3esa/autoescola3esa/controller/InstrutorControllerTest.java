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
class InstrutorControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("Expectativa: 403 sem autenticação.")
    void semToken() throws Exception {
        mockMvc.perform(get("/instrutores")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("Expectativa: 400 para dados inválidos no cadastro.")
    void cadastroInvalido() throws Exception {
        mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Expectativa: cadastrar (201) e na atualização não alterar e-mail, CNH e especialidade.")
    void cadastroEAtualizacao() throws Exception {
        String body = """
                {"nome":"Carlos","email":"carlos.teste@email.com","telefone":"11988887777",
                 "cnh":"12345678900","especialidade":"CARROS","endereco":%s}""".formatted(TestData.ENDERECO_JSON);
        String location = mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(put("/instrutores").contentType(MediaType.APPLICATION_JSON).content("""
                        {"id":%d,"nome":"Carlos Silva","email":"outro@email.com",
                         "cnh":"00000000000","especialidade":"MOTOS"}""".formatted(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Carlos Silva"))
                .andExpect(jsonPath("$.email").value("carlos.teste@email.com"))
                .andExpect(jsonPath("$.cnh").value("12345678900"))
                .andExpect(jsonPath("$.especialidade").value("CARROS"));
    }
}
