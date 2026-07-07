# 🍽️ Restaurante API

API REST para gerenciamento de pedidos em um restaurante, desenvolvida em **Spring Boot** como parte do curso do professor **Matheus Leandro Ferreira**, no YouTube.

O projeto simula o fluxo operacional de um restaurante: cadastro de produtos e mesas, abertura de pedidos, acompanhamento na cozinha, fechamento de conta e processamento de pagamento via serviço externo (simulado com Feign).

## 📋 Sobre o projeto

A aplicação cobre o ciclo completo de um atendimento:

1. Um **produto** é cadastrado em uma **categoria**, com preço e tempo de preparo.
2. Um **pedido** é aberto vinculado a uma **mesa**.
3. **Itens** são adicionados ao pedido, cada um com seu próprio status de preparo.
4. A **cozinha** consulta os itens pendentes e vai atualizando o status (`iniciar preparo` → `marcar pronto` → `entregar`).
5. Um worker em background verifica periodicamente se algum item está **atrasado** em relação ao tempo de preparo esperado.
6. Ao final, a conta é **fechada** (com subtotal, taxa de serviço e desconto) e o **pagamento** é processado através de um cliente Feign para uma API externa de pagamentos.

## 🛠️ Tecnologias

- **Java 21**
- **Spring Boot 3.5.16**
  - Spring Web
  - Spring Data JPA
  - Spring Validation
  - Spring Cloud OpenFeign
- **Flyway** — versionamento e migração do schema do banco
- **PostgreSQL**
- **SpringDoc OpenAPI (Swagger)** — documentação interativa da API
- **Lombok**
- **Virtual Threads** (Project Loom) — habilitadas para o agendamento (`@EnableScheduling`) e o worker da cozinha
- **Maven**

## 🗂️ Estrutura do domínio

| Entidade | Descrição |
|---|---|
| `Mesa` | Mesas do restaurante, com status (`LIVRE`, `OCUPADA`, `RESERVADA`, `INATIVA`) |
| `CategoriaProduto` | Categorias dos produtos do cardápio |
| `Produto` | Itens do cardápio, vinculados a uma categoria, com preço e tempo de preparo |
| `Pedido` | Pedido aberto em uma mesa, com status (`ABERTO`, `EM_PREPARO`, `PRONTO`, `ENTREGUE`, `FECHADO`, `CANCELADO`) |
| `PedidoItem` | Item de um pedido, com status próprio (`PENDENTE`, `EM_PREPARO`, `PRONTO`, `ENTREGUE`, `CANCELADO`) e marcações de tempo (início do preparo, pronto, entrega) |
| `Pagamento` | Pagamento vinculado a um pedido, com forma de pagamento (`DINHEIRO`, `CARTAO_CREDITO`, `CARTAO_DEBITO`, `PIX`) e status |
| `FechamentoConta` | Fechamento da conta de um pedido, com subtotal, taxa de serviço, desconto e total |

O schema do banco é criado e evoluído por meio de migrations do Flyway (`V1__create_restaurante_schema.sql`, `V2__insert_dados_iniciais.sql`, `V3__add_datas_preparo_pedido_itens.sql`), com `ddl-auto: validate` no Hibernate — ou seja, as entidades JPA são validadas contra o schema gerado pelas migrations, e não o contrário.

## 🚀 Endpoints principais

### Produtos (`/produtos`)
- `POST /produtos` — cadastrar produto
- `GET /produtos` — listar produtos (paginado)
- `GET /produtos/{id}` — buscar produto por id
- `PUT /produtos/{id}` — atualizar produto
- `DELETE /produtos/{id}` — remover produto

### Pedidos (`/pedidos`)
- `POST /pedidos` — abrir pedido
- `GET /pedidos` — listar pedidos (paginado)
- `GET /pedidos/{id}` — buscar pedido por id
- `POST /pedidos/{pedidoId}/itens` — adicionar item ao pedido
- `GET /pedidos/{pedidoId}/itens` — listar itens do pedido
- `POST /pedidos/{pedidoId}/pagar` — processar pagamento do pedido

### Cozinha (`/cozinha`)
- `GET /cozinha/itens-pendentes` — listar itens pendentes de preparo
- `GET /cozinha/itens-em-preparo` — listar itens em preparo
- `PATCH /cozinha/itens/{itemId}/iniciar-preparo` — marcar item como em preparo
- `PATCH /cozinha/itens/{itemId}/marcar-pronto` — marcar item como pronto
- `PATCH /cozinha/itens/{itemId}/entregar` — marcar item como entregue

### Fechamento de conta (`/pedidos/{pedidoId}/fechamento`)
- `POST /pedidos/{pedidoId}/fechamento` — fechar a conta do pedido
- `GET /pedidos/{pedidoId}/fechamento` — consultar o fechamento do pedido

## ⏱️ Worker da cozinha

O `CozinhaWorker` roda a cada 60 segundos (`@Scheduled(fixedRate = 60000)`) e verifica todos os itens em preparo, comparando o tempo decorrido desde o início do preparo com o `tempoPreparoMinutos` cadastrado no produto. Quando um item ultrapassa o tempo esperado, um alerta é impresso no console. Cada verificação é despachada em uma **virtual thread** própria, usando `Executors.newVirtualThreadPerTaskExecutor()`.

## 💳 Integração de pagamento

O `PagamentoClient` é um cliente **OpenFeign** que se comunica com uma API externa (fake, usada para fins de estudo) responsável por processar pagamentos. A URL base é configurável via propriedade `pagamento.api.url`.

## ⚙️ Configuração e execução

### Pré-requisitos
- Java 21
- Maven (ou usar o `mvnw` incluso no projeto)
- PostgreSQL em execução
- Uma API de pagamentos fake rodando (ou apontar `pagamento.api.url` para o serviço correspondente)

### Variáveis de ambiente

A aplicação espera as seguintes variáveis de ambiente para conexão com o banco:

```
jdbc:postgresql://${DB_HOST}/${DB_BANCO}
DB_HOST=localhost:5432
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
```

### Rodando a aplicação

```bash
# clonar o repositório
git clone <url-do-repositorio>
cd restaurante

# rodar com o wrapper do Maven
./mvnw spring-boot:run
```

A aplicação sobe por padrão na porta `8080`. Ao iniciar, o Flyway aplica automaticamente as migrations no banco `restaurante`.

### Documentação da API (Swagger)

Com a aplicação em execução, a documentação interativa fica disponível em:

```
http://localhost:8080/swagger-ui.html
```

## 📁 Requisições de exemplo

O diretório `requests/` contém arquivos `.http` (compatíveis com plugins de cliente HTTP de IDEs como IntelliJ/VS Code) com exemplos de chamadas prontas para teste, começando pelo módulo de produtos (`produto.http`).

## 🎓 Créditos

Projeto desenvolvido para fins de estudo, acompanhando o curso de Spring Boot do professor **Matheus Leandro Ferreira** no YouTube.
