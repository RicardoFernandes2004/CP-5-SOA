package br.com.fiap3esa.autoescola3esa.infra.viacep;

import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;

// Formato da resposta de https://viacep.com.br/ws/{cep}/json/
public record DadosViaCep(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        Boolean erro) {
    public DadosEndereco toDadosEndereco() {
        return new DadosEndereco(
                logradouro, null, complemento, bairro, localidade, uf, cep.replace("-", ""));
    }
}
