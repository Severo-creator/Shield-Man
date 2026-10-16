# API de mestres e escudos

Com Docker Desktop iniciado, execute `docker compose up -d`. O endereco da aplicacao e `http://localhost:8080`.

## Rotas

A [Swagger UI](http://localhost:8080/q/swagger-ui/) permite executar essas operações pelo navegador. A especificação gerada do código está disponível em [YAML](http://localhost:8080/q/openapi) e [JSON](http://localhost:8080/q/openapi?format=json). Veja [o passo a passo e os testes do OpenAPI](openapi.md).

| Metodo | Caminho | Resultado |
| --- | --- | --- |
| POST | `/mestres` | Cadastra mestre: 201, JSON e cabecalho Location |
| GET | `/mestres` | Lista mestres: 200 e array JSON |
| GET | `/mestres/{id}` | Consulta mestre: 200 ou 404 |
| PUT | `/mestres/{id}` | Atualiza o nome: 200 ou 404 |
| DELETE | `/mestres/{id}` | Exclui mestre sem escudos: 204, 404 ou 409 |
| POST | `/escudos` | Cadastra escudo de um mestre existente: 201 e Location |
| GET | `/escudos` | Lista escudos: 200 e array JSON |
| GET | `/escudos/{id}` | Consulta escudo: 200 ou 404 |
| PUT | `/escudos/{id}` | Atualiza o nome: 200 ou 404 |
| DELETE | `/escudos/{id}` | Exclui escudo: 204 ou 404 |

As duas listagens aceitam `nome` (trecho literal, sem distinguir maiusculas), `pagina` (a partir de 0) e `tamanho` (padrao 20, entre 1 e 100). O filtro `%` ou `_` procura esses caracteres literalmente. Os resultados sao ordenados por nome e UUID, e a paginacao e executada no banco.

`GET /escudos` tambem aceita `mestreId` para listar apenas os escudos de um mestre. Se esse identificador nao possuir registros, a lista e vazia.

O corpo de POST/PUT de mestre e PUT de escudo e `{"nome":"Nome desejado"}`. POST de escudo tambem exige `mestreId`. Nomes sao obrigatorios, com ate 120 caracteres para mestres e 160 para escudos. PUT de escudo altera somente o nome; nao permite trocar seu dono.

Identificadores devem ser UUIDs completos. `UUID_DO_ESCUDO` e apenas um marcador de exemplo, nao um identificador valido. Erros de entrada retornam 400; recursos ausentes, 404; exclusao de mestre com escudos, 409. O tratamento centralizado usa `application/problem+json` com `type`, `title`, `status`, `detail` e `instance`.

Os pedidos usam Bean Validation. JSON com campos desconhecidos, tipos errados, corpo nulo ou conteudo extra e rejeitado com 400. Parametros de paginacao ou mestreId fornecidos vazios tambem sao rejeitados. Veja [as regras e os testes de validacao](validacao.md).

## Experimentar no PowerShell

```powershell
$mestre = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/mestres' `
    -ContentType 'application/json' -Body '{"nome":"Guilherme"}'

$escudo = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/escudos' `
    -ContentType 'application/json' `
    -Body (@{ mestreId = $mestre.id; nome = 'Minha campanha' } | ConvertTo-Json)

Invoke-RestMethod -Uri "http://localhost:8080/escudos/$($escudo.id)"
Invoke-RestMethod -Uri "http://localhost:8080/escudos?mestreId=$($mestre.id)&pagina=0&tamanho=10"

Invoke-RestMethod -Method Put -Uri "http://localhost:8080/escudos/$($escudo.id)" `
    -ContentType 'application/json' -Body '{"nome":"Campanha atualizada"}'

# Remova primeiro o escudo, depois o mestre, quando quiser excluir os exemplos.
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/escudos/$($escudo.id)"
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/mestres/$($mestre.id)"
```

## Arquitetura

As rotas `MestreResource` e `EscudoResource` chamam os casos de uso em `application`. Eles dependem das interfaces em `domain/repository` e dos objetos em `domain/model`. `CasosDeUsoConfig` fornece os adaptadores PostgreSQL durante a execucao. Os testes sem banco usam as implementacoes em `infra/memory`. Veja tambem `docs/arquitetura.md`.

Os repositórios salvam apenas os cadastros de mestre e escudo. A V2 prepara as tabelas dos conteudos, mas esta API ainda nao salva documentos, posicoes, tabelas ou bancos de nomes. A API tambem ainda nao possui autenticacao.

## Testes

`mvn test` executa os testes sem PostgreSQL. Os testes externos ficam desabilitados ate a ativacao explicita.

Para testar as rotas reais e seus repositorios no PostgreSQL:

```powershell
docker compose up -d
docker compose run --rm --no-deps -T -e SHIELDMAN_TEST_API=true -e SHIELDMAN_API_URL=http://api:8080 api mvn -B -ntp -Dtest=ApiPostgresTest test
```

Esse teste cria cadastros com identificadores proprios e os remove no final. Verifica CRUD, Location, relacionamento, filtros, paginacao, validacao e codigos de resposta HTTP.

Para verificar os dados depois de reiniciar o container da aplicacao:

```powershell
./scripts/testar-persistencia.ps1
```

O script cria um mestre e um escudo temporarios, reinicia apenas `api`, consulta ambos e remove os cadastros criados. O banco e seu volume permanecem em execucao.
