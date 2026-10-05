package br.com.fiap3esa.autoescola3esa.infra.viacep;

import br.com.fiap3esa.autoescola3esa.domain.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Consumo da API externa ViaCEP para consulta de endereços a partir do CEP.
 */
@Component
public class ViaCepClient {
    private final RestClient restClient;

    public ViaCepClient(@Value("${viacep.url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public DadosEndereco buscarEndereco(String cep) {
        String somenteDigitos = cep.replaceAll("\\D", "");
        if (somenteDigitos.length() != 8) {
            throw new ValidacaoException("CEP deve conter 8 dígitos!");
        }
        DadosViaCep resposta;
        try {
            resposta = restClient.get()
                    .uri("/ws/{cep}/json/", somenteDigitos)
                    .retrieve()
                    .body(DadosViaCep.class);
        } catch (RestClientException e) {
            throw new ValidacaoException("Não foi possível consultar o ViaCEP: " + e.getMessage());
        }
        if (resposta == null || Boolean.TRUE.equals(resposta.erro())) {
            throw new ValidacaoException("CEP não encontrado!");
        }
        return resposta.toDadosEndereco();
    }
}
