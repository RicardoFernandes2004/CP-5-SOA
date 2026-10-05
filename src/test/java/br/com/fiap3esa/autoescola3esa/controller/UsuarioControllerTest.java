package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.usuario.Perfil;
import br.com.fiap3esa.autoescola3esa.domain.usuario.Usuario;
import br.com.fiap3esa.autoescola3esa.domain.usuario.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsuarioControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsuarioRepository repository;

    @Autowired
    PasswordEncoder encoder;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Expectativa: 403 quando usuário comum tenta listar usuários.")
    void userNaoListaUsuarios() throws Exception {
        mockMvc.perform(get("/usuarios")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Expectativa: admin cadastra usuário (201) com senha encriptada.")
    void adminCadastraUsuario() throws Exception {
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"novo@autoescola.com\",\"senha\":\"segredo1\",\"perfil\":\"USER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senha").doesNotExist());

        Usuario salvo = repository.findByLogin("novo@autoescola.com");
        assertThat(salvo.getSenha()).isNotEqualTo("segredo1");
        assertThat(encoder.matches("segredo1", salvo.getSenha())).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Expectativa: 400 para cadastro com senha curta.")
    void cadastroInvalido() throws Exception {
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"x@autoescola.com\",\"senha\":\"123\",\"perfil\":\"USER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Expectativa: usuário autenticado altera a própria senha (204); senha atual errada dá 400.")
    void alterarPropriaSenha() throws Exception {
        Usuario usuario = repository.save(new Usuario(null, "comum@autoescola.com", encoder.encode("antiga1"), Perfil.USER));

        mockMvc.perform(put("/usuarios/senha").with(user(usuario)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"errada\",\"novaSenha\":\"novaSenha1\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/usuarios/senha").with(user(usuario)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"antiga1\",\"novaSenha\":\"novaSenha1\"}"))
                .andExpect(status().isNoContent());
        assertThat(encoder.matches("novaSenha1", repository.findByLogin("comum@autoescola.com").getSenha())).isTrue();
    }
}
