# Esquema do banco

A V1 cria os cadastros de mestre e escudo. A V2 acrescenta o esquema dos conteúdos sem alterar a V1 nem remover cadastros existentes. Fichas e iniciativa não estão incluídas.

| Classe ou coleção Java | Tabela |
| --- | --- |
| Mestre | `mestre` (V1) |
| Escudo | `escudo` (V1) |
| NavInfo | `navinfo`, identificado pelo UUID do escudo |
| Painel | `painel`, com no máximo um painel por escudo |
| Infograma | `infograma`, com título, tipo, escudo e ordem |
| Container | `container`, com conteúdo, painel, posição, dimensões e ordem |
| Documento | `documento`, com texto |
| Tabela | `tabela` |
| TabelaRolavel | `tabela_rolavel`, que complementa `tabela` |
| Linha e Coluna | `linha` e `coluna`, com ordem dentro da tabela |
| ValorCampo | `valor_campo`, identificado pela combinação de linha e coluna |
| ValorChance | `valor_chance`, com faixa inclusiva e referência à linha |
| BancoNomes | `banco_nomes` |
| Listas de nomes e sobrenomes | `nome_banco` e `sobrenome_banco`, preservando ordem e repetições |

`Validacao`, repositórios, casos de uso, rotas e mapeadores não são dados do domínio e não recebem tabelas.

## Relacionamentos e restrições

- Os infogramas pertencem ao NavInfo de um escudo, mesmo que ainda não sejam exibidos.
- Um contêiner só pode referenciar um painel e um infograma do mesmo escudo. O mesmo infograma não se repete no painel.
- Remover um contêiner preserva o infograma. Remover um infograma remove sua exibição e seus dados específicos; remover um escudo remove seus conteúdos e painel por cascata. A exclusão do mestre ainda exige remover seus escudos, conforme a V1.
- Os tipos de conteúdo são `DOCUMENTO`, `TABELA`, `TABELA_ROLAVEL` e `BANCO_NOMES`. Os relacionamentos dos subtipos verificam esses tipos.
- Uma célula pertence a uma linha e uma coluna da mesma tabela; o par não pode se repetir.
- Uma chance só pode usar uma linha da própria tabela rolável, com limites positivos e mínimo menor ou igual ao máximo.
- Posições são não negativas; tamanhos são positivos. Valores infinitos e NaN são rejeitados.
- Há índices e restrições de unicidade para as consultas por escudo, painel, tabela e ordem.

Algumas regras envolvem conjuntos de registros e continuam sob responsabilidade do domínio e dos futuros repositórios: preencher todas as células, criar os registros de subtipo junto ao infograma e configurar todas as faixas sem lacunas nem sobreposições antes de sortear. As escritas desses conjuntos devem ser transacionais.

A V2 cria NavInfo e painel vazios para escudos existentes. Para esse preenchimento inicial, o painel recebe o mesmo UUID do escudo em sua própria tabela. Novos escudos ainda precisarão ter seus registros de NavInfo e painel criados pelos futuros adaptadores; a migração não adiciona gatilhos de criação automática.

## Aplicação e testes

Ao iniciar a aplicação, o Flyway aplica V1 e V2 em um banco novo, ou apenas V2 quando V1 já está aplicada. Migrações aplicadas não devem ser editadas; alterações posteriores usam V3, V4 etc.

```powershell
docker compose up -d
docker compose logs -f api
```

Os testes de migração são habilitados explicitamente e usam um schema temporário exclusivo por teste, removido ao terminar. Não alteram o schema `public` nem os cadastros da aplicação:

```powershell
docker compose up -d db
docker compose run --rm --no-deps -T -e SHIELDMAN_TEST_MIGRATIONS=true api mvn -B -ntp -Dtest=MigracoesPostgresTest test
```

O container de teste reutiliza as variáveis de conexão do serviço `api`. Em `mvn test` normal, os testes com PostgreSQL ficam desabilitados; os testes sem banco continuam disponíveis. A suíte também provoca violações reais das restrições para conferir o tratamento de erros descrito em `docs/validacao.md`.

## Limite desta alteração

Criar tabelas não modifica o comportamento das classes Java. Os repositórios atuais continuam gravando apenas os cadastros de mestre e escudo. Ainda será necessário implementar mapeamentos JPA, reconstituição do domínio, repositórios e rotas dos conteúdos. Até essa implementação, recuperar um escudo continua produzindo painel e NavInfo vazios em memória.
