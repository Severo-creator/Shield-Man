# Separação de camadas (T6)

Os pacotes Java partem de `src/main/java/com/equipe/shieldman`:

```text
domain/
  model/          Objetos e regras: Mestre, Escudo, Painel, Infograma etc.
  repository/     Interfaces: MestreRepository e EscudoRepository.
application/      Casos de uso, validação de entrada e exceções da aplicação.
api/              Adaptadores HTTP: rotas, pedidos, respostas e erros HTTP.
infra/
  persistence/    Adaptadores PostgreSQL: JPA, Panache e conversões.
  memory/         Adaptadores em memória usados nos testes sem banco.
  config/         Montagem dos casos de uso pelo CDI do Quarkus.
```

## Direção das dependências

O domínio contém os objetos e os contratos que as outras camadas utilizam. Seus imports são exclusivamente da biblioteca padrão Java ou de outros arquivos dentro de `domain`. Não há import de Quarkus, Jakarta, Hibernate, Panache, JDBC, HTTP, `application`, `api` ou `infra` nesse pacote.

Os casos de uso em `application` dependem do domínio. Por exemplo, `GerenciarEscudos` recebe `domain.repository.EscudoRepository` e `domain.repository.MestreRepository`, sem conhecer as implementações PostgreSQL. Essa camada também continua sem imports de framework ou infraestrutura.

As rotas em `api` recebem pedidos HTTP e chamam os casos de uso. Elas convertem os resultados para JSON e as exceções para respostas HTTP. Não importam entidades JPA nem repositórios PostgreSQL.

Os adaptadores em `infra/persistence` implementam as interfaces do domínio usando Panache e entidades JPA. `PersistenciaMapper` concentra as conversões. `infra/memory` implementa as mesmas interfaces com mapas para os testes. `infra/config` é responsável pela montagem dos casos de uso com os beans CDI.

```text
api → application → domain
infra/persistence → domain (e tipos da aplicação usados para validação/erros)
infra/memory → domain (e tipos da aplicação usados para validação/erros)
infra/config → application e interfaces do domínio
```

Uma alteração no banco ou no transporte HTTP não exige adicionar dependências dessas tecnologias ao domínio.

## O que mudou

As classes antes em `com.equipe.shieldman.model` foram movidas para `com.equipe.shieldman.domain.model`. As interfaces antes em `com.equipe.shieldman.repository` foram movidas para `com.equipe.shieldman.domain.repository`. Os imports de produção e de testes foram atualizados, e os testes do modelo acompanham o novo pacote.

As URLs da API, os corpos JSON e as migrações continuam os mesmos: esta alteração reorganiza o código Java.

## Conferência

Para inspecionar os imports do domínio, use a busca do IntelliJ limitada à pasta `domain`, procurando por `import`. Todos devem apontar para `java.*` ou `com.equipe.shieldman.domain.*`.

Com ripgrep, a conferência pode ser feita na raiz do projeto:

```powershell
rg '^import ' src/main/java/com/equipe/shieldman/domain
```

Execute `mvn test` para conferir a compilação e os testes sem banco. Os testes HTTP com PostgreSQL estão descritos em `docs/api.md`.

O teste automatizado específico de arquitetura ainda não foi incluído, conforme o recorte da T6 informado no cartão. Ele pode ser acrescentado com ArchUnit na Sprint 2 para bloquear automaticamente dependências proibidas.
