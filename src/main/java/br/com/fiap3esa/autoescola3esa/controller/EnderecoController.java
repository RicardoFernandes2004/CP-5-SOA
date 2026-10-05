package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import br.com.fiap3esa.autoescola3esa.infra.viacep.ViaCepClient;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enderecos")
@SecurityRequirement(name = "bearer-key")
public class EnderecoController {
    private final ViaCepClient viaCep;

    public EnderecoController(ViaCepClient viaCep) {
        this.viaCep = viaCep;
    }

    // Consulta o endereço no ViaCEP, já no formato usado no cadastro de alunos e instrutores
    @GetMapping("/{cep}")
    public ResponseEntity<DadosEndereco> buscarPorCep(@PathVariable String cep) {
        return ResponseEntity.ok(viaCep.buscarEndereco(cep));
    }
}
