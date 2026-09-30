# TASKMASTER API

API RESTful de gerenciamento de tarefas (Spring Boot 4, Java 17, MariaDB, H2 para testes).

## Requisitos

- JDK 17+
- MariaDB (ou MySQL) em `localhost:3306` (o banco `taskmaster` é criado automaticamente)

Credenciais padrão: `taskmaster` / `taskmaster123`. Para usar outras:

```bash
export DB_USERNAME=seu_usuario
export DB_PASSWORD=sua_senha
```

## Executando

```bash
./mvnw spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Testes

```bash
./mvnw test
```

Os testes usam o perfil `test` com H2 em memória (`src/test/resources/application-test.properties` e `schema.sql`).
No log de inicialização aparece `Database JDBC URL [jdbc:h2:mem:testdb]`.

- `TaskServiceTest`: testes unitários do serviço com Mockito simulando o repositório.
- `TaskControllerFunctionalTest`: testes funcionais com `@SpringBootTest` + `MockMvc`.

## Endpoints

| Método | URL | Sucesso | Erros |
|---|---|---|---|
| POST | `/tasks` | 201 | 400 |
| GET | `/tasks` | 200 | 400 |
| GET | `/tasks/{id}` | 200 | 404 |
| PUT | `/tasks/{id}` | 200 | 400, 404 |
| DELETE | `/tasks/{id}` | 204 | 404 |

Parâmetros de `GET /tasks`: `categoria`, `page` (começa em 0), `size` (padrão 10), `sort` (ex.: `dataLimite,asc`).

### Exemplo de corpo (POST/PUT)

```json
{
  "titulo": "Estudar Spring Boot",
  "descricao": "Revisar DTOs, paginação e testes",
  "categoria": "Estudo",
  "dataLimite": "2026-12-31"
}
```

### Roteiro para os prints (Postman/Insomnia)

1. `POST /tasks` com o corpo acima → 201
2. `POST /tasks` sem `titulo` → 400 (`titulo: o título é obrigatório`)
3. `POST /tasks` com `"dataLimite": "31/12/2026"` → 400 (formato inválido)
4. `PUT /tasks/1` com corpo válido → 200
5. `PUT /tasks/9999` → 404
6. `DELETE /tasks/1` → 204
7. `DELETE /tasks/9999` → 404
8. `GET /tasks` → 200 (página 0, tamanho 10)
9. `GET /tasks?page=1&size=5` → 200
10. `GET /tasks?sort=dataLimite,asc` → 200
11. `GET /tasks?categoria=Trabalho` → 200
12. `GET /tasks?categoria=Inexistente` → 200 com lista vazia
