# Auto Escola 3ESA - API REST

API REST desenvolvida com Spring Boot para gerenciamento de uma auto-escola: cadastro de instrutores e alunos, agendamento e cancelamento de instrucoes, com autenticacao JWT, controle de acesso por perfil, documentacao Swagger, CORS, consumo da API externa ViaCEP e testes automatizados.

**Disciplina:** SOA e WebServices  
**Professor:** Carlos Eduardo Machado de Oliveira  
**Checkpoint 5** (inclui todas as demandas dos Checkpoints 3 e 4)

## Integrantes do Grupo

| Nome              | RM       |
|-------------------|----------|
| Ricardo Fernandes | RM554597 |
| Khadija Lima      | RM668971 |

## Tecnologias

- Java 25
- Spring Boot 4.0.5
- Spring Security + JWT (java-jwt)
- BCrypt (encriptacao de senhas)
- Spring Data JPA
- Flyway (migrações de banco)
- Bean Validation
- Lombok
- MySQL
- springdoc-openapi (Swagger UI)
- RestClient (consumo do ViaCEP)
- JUnit 5 + MockMvc + Spring Security Test

## Como Rodar

1. Ter o MySQL rodando na porta **3306** com um banco chamado `autoescola3esa`:
   ```sql
   CREATE DATABASE autoescola3esa;
   ```
   1. Se não tiver, rode o docker compose:
   ```bash
   docker compose up -d
   ```
2. Conferir as credenciais em `src/main/resources/application.properties` (padrão: `root` / `fiap`).
3. Executar o projeto:
   ```bash
   ./mvnw spring-boot:run
   ```
4. A API sobe na porta **8085**.

O segredo do token JWT vem da variavel de ambiente `JWT_SECRET` (ha um valor padrao para desenvolvimento em `application.properties`).

## Autenticacao

Todas as rotas exigem um token JWT, exceto `POST /login`, `GET /health_check` e a documentacao Swagger.

O Flyway ja cria um usuario administrador:

| Login | Senha | Perfil |
|-------|-------|--------|
| `admin@autoescola.com` | `123456` | ADMIN |

Para obter o token:

```bash
curl -X POST http://localhost:8085/login \
  -H 'Content-Type: application/json' \
  -d '{"login":"admin@autoescola.com","senha":"123456"}'
```

E enviar nas demais requisicoes o header `Authorization: Bearer <token>`.

As senhas sao gravadas no banco com hash BCrypt, nunca em texto puro.

## Documentacao (Swagger)

Com a API rodando:

- Swagger UI: http://localhost:8085/swagger-ui.html
- OpenAPI JSON: http://localhost:8085/v3/api-docs (YAML em `/v3/api-docs.yaml`)

Para testar rotas protegidas pelo Swagger: execute `POST /login`, copie o token e cole no botao **Authorize**.

## CORS

Configurado em `SecurityConfigurations`. Origens liberadas: `http://localhost:3000`, `http://127.0.0.1:3000`, `http://localhost:5500` e `http://127.0.0.1:5500`, com os metodos `GET, POST, PUT, DELETE, OPTIONS, HEAD` e os headers `Authorization, Content-Type, Accept, Origin`. Requisicoes vindas de outras origens sao recusadas (403).

## API externa: ViaCEP

`ViaCepClient` (`infra/viacep`) consome o https://viacep.com.br para consultar enderecos por CEP. A rota `GET /enderecos/{cep}` devolve o endereco ja no formato usado no cadastro de alunos e instrutores:

```bash
curl http://localhost:8085/enderecos/01001000 -H "Authorization: Bearer <token>"
# {"logradouro":"Praça da Sé","numero":null,"complemento":"lado ímpar","bairro":"Sé","cidade":"São Paulo","uf":"SP","cep":"01001000"}
```

CEP inexistente ou mal formatado retorna 400 com mensagem. A URL base fica em `viacep.url` no `application.properties`.

## Endpoints

### Health Check

| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | `/health_check` | Verificacao de integridade da API (publico) |

### Autenticacao

| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | `/login` | Autenticar e receber o token JWT (publico) |

### Usuarios

| Metodo | Rota | Descricao | Acesso |
|--------|------|-----------|--------|
| POST | `/usuarios` | Cadastrar usuario (senha encriptada com BCrypt) | ADMIN |
| GET | `/usuarios` | Listar usuarios (paginado, 10/pagina) | ADMIN |
| PUT | `/usuarios` | Atualizar o perfil de um usuario | ADMIN |
| DELETE | `/usuarios/{id}` | Excluir usuario | ADMIN |
| PUT | `/usuarios/senha` | Alterar a propria senha (exige a senha atual) | Autenticado |

### Instrutores

| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | `/instrutores` | Cadastrar instrutor |
| GET | `/instrutores` | Listar instrutores (paginado, 10/pagina, ordenado por nome) |
| GET | `/instrutores/{id}` | Detalhar instrutor por ID |
| PUT | `/instrutores` | Atualizar instrutor |
| DELETE | `/instrutores/{id}` | Excluir instrutor (soft delete) |

### Alunos

| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | `/alunos` | Cadastrar aluno |
| GET | `/alunos` | Listar alunos (paginado, 10/pagina, ordenado por nome) |
| GET | `/alunos/{id}` | Detalhar aluno por ID |
| PUT | `/alunos` | Atualizar aluno |
| DELETE | `/alunos/{id}` | Excluir aluno (soft delete) |

### Enderecos

| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | `/enderecos/{cep}` | Consultar endereco pelo CEP (ViaCEP) |

### Instrucoes

| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | `/instrucoes` | Agendar instrucao |
| DELETE | `/instrucoes` | Cancelar instrucao (informando o motivo) |
| GET | `/instrucoes` | Listar instrucoes ativas (paginado, 10/pagina) |
| GET | `/instrucoes/{id}` | Detalhar instrucao por ID |

Agendar (o campo `instrutorId` e opcional; se omitido, o sistema sorteia um instrutor livre no horario):

```json
POST /instrucoes
{ "alunoId": 1, "instrutorId": 2, "data": "2026-09-15 10:00" }
```

Cancelar (motivo: `ALUNO_DESISTIU`, `INSTRUTOR_CANCELOU` ou `OUTROS`):

```json
DELETE /instrucoes
{ "id": 1, "motivo": "ALUNO_DESISTIU" }
```

#### Regras de negocio

**Agendamento**

- Funcionamento de segunda a sabado, das 06:00 as 21:00 (instrucao de 1 hora, ultimo inicio as 20:00, sempre em hora cheia);
- Antecedencia minima de 30 minutos;
- Aluno e instrutor precisam estar ativos;
- No maximo 2 instrucoes por dia para o mesmo aluno;
- Um instrutor nao pode ter duas instrucoes na mesma data/hora;
- Instrutor opcional: sem ele, o sistema escolhe aleatoriamente um instrutor livre.

**Cancelamento**

- Motivo obrigatorio (`ALUNO_DESISTIU`, `INSTRUTOR_CANCELOU`, `OUTROS`);
- Antecedencia minima de 24 horas;
- O cancelamento nao apaga a instrucao, apenas registra o motivo.

## Regras de agendamento (validadores)

Cada regra de agendamento e cancelamento e uma classe em `domain/instrucao/validacao` que implementa `ValidadorAgendamento` ou `ValidadorCancelamento`. O `InstrucaoService` recebe todas por injecao (`List<ValidadorAgendamento>`), entao uma regra nova e so mais uma classe `@Component`.

## Testes

Os testes usam o perfil `test`, que aponta para o banco `autoescola3esa_test` (criado automaticamente no mesmo MySQL). Com o MySQL rodando (`docker compose up -d`):

```bash
./mvnw test
```

| Entidade | Teste | O que cobre |
|----------|-------|-------------|
| Aluno | `AlunoControllerTest` | 403 sem token, 400 com dados invalidos, cadastro/atualizacao/exclusao logica |
| Instrutor | `InstrutorControllerTest` | 403 sem token, 400 com dados invalidos, e-mail/CNH/especialidade nao mudam na atualizacao |
| Usuario | `UsuarioControllerTest` | so ADMIN gerencia usuarios, senha gravada com BCrypt, troca da propria senha |
| Instrucao | `InstrucaoControllerTest` | agendamento, conflito de instrutor, limite de 2 por dia, instrutor aleatorio, cancelamento |
| Instrucao | `InstrucaoRepositoryTest` | busca de instrutores livres (`@DataJpaTest`) |
| Instrucao | `ValidadoresHorarioTest` | horario de funcionamento e antecedencia de 30 min / 24 h (sem banco) |
