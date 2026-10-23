# VIDEO
https://youtu.be/T63QQwjR2Ro

# ShieldMan

Primeiras classes do projeto, em Java 21. O modelo representa as três áreas das telas descritas nos PDFs.

| Área da tela | Classe e responsabilidade |
| --- | --- |
| Barra esquerda: lista de escudos | `Mestre` possui os escudos e permite criar, consultar e remover cada um. |
| Escudo selecionado | `Escudo` guarda nome, identificação do mestre, painel e NavInfo. |
| Centro: painel | `Painel` organiza os contêineres do escudo. |
| Bloco dentro do painel | `Container` possui conteúdo, posição X/Y, largura e altura. |
| Informação do bloco | `Infograma` é a classe abstrata para os tipos de conteúdo. |
| Anotações e regras | `Documento` herda de `Infograma` e permite editar texto. |
| Tabelas de consulta | `Tabela` organiza linhas, colunas e células (`ValorCampo`). |
| Tabelas de sorteio | `TabelaRolavel` herda de `Tabela` e associa uma faixa (`ValorChance`) a cada linha. |
| Nomes aleatórios | `BancoNomes` combina nomes e sobrenomes cadastrados. |
| Barra direita: NavInfo | `NavInfo` mantém, lista e busca os infogramas disponíveis no escudo. |

## Relações e escolhas iniciais

```text
Mestre -> vários Escudos
Escudo -> um Painel + um NavInfo
Painel -> vários Containers
Container -> um Infograma
Documento é um Infograma
NavInfo -> vários Infogramas
Painel cria Container informando o NavInfo e o identificador do Infograma
Container consulta o NavInfo para obter seu Infograma
Tabela é um Infograma; TabelaRolavel é uma Tabela
BancoNomes é um Infograma
```

O contêiner guarda a organização visual; o infograma guarda a informação. Primeiro o conteúdo é adicionado ao NavInfo do escudo. Depois, o painel passa seu NavInfo e o UUID escolhido ao construtor do contêiner. O próprio contêiner consulta esse NavInfo e rejeita identificadores não cadastrados nele. O painel só inclui o novo contêiner depois que a criação é bem-sucedida. Infogramas ainda não exibidos também podem ficar no NavInfo. Remover um contêiner retira apenas sua exibição do painel e preserva o infograma no NavInfo, permitindo exibi-lo novamente. Edições no infograma aparecem nos dois lugares porque ambos usam o mesmo objeto.

Os escudos são criados pelo mestre e os contêineres pelo painel. As listas retornadas não podem ser alteradas diretamente; use os métodos de inclusão e remoção. Os identificadores são UUIDs. Posições usam coordenadas não negativas, e tamanhos devem ser positivos e finitos.

Este início considera um painel por escudo e um infograma por contêiner, sem repetir o mesmo infograma no painel. `ListContainer`, `InfoContainer`, fichas e iniciativa ficam para etapas posteriores. O texto está diretamente em `Documento`; ainda não há necessidade de uma classe `Texto` separada.

## Tabelas e sorteios

Crie as colunas com `adicionarColuna(titulo)` e as linhas com `adicionarLinha(List<String>)`, fornecendo um valor para cada coluna. Cada célula é um único objeto compartilhado por sua linha e coluna. Use `editarCampo(linhaId, colunaId, texto)` para atualizar o valor; acrescentar uma coluna a uma tabela com linhas cria células vazias para elas. As ordens de linhas e colunas começam em zero e seguem a inserção. A geração de texto usa ` | ` entre as células e uma linha de texto por linha da tabela.

Na `TabelaRolavel`, use `definirChance(linhaId, minimo, maximo)` para cadastrar ou alterar uma faixa inclusiva. Exemplo: Goblins de 1 a 3 e Lobos de 4 a 6. Uma rolagem sorteia uniformemente um inteiro de 1 até o maior limite; faixas maiores têm maior probabilidade. Antes de rolar, todas as linhas precisam ter uma faixa e as faixas precisam cobrir esse intervalo sem lacunas nem sobreposições. `resolverResultado(numero)` permite consultar um resultado conhecido; `rolarResultado()` sorteia e retorna o texto atual da linha correspondente.

O `BancoNomes` exige pelo menos um nome e aceita sobrenomes opcionais. Use `adicionarNome`, `adicionarSobrenome` e `gerarNome`. A geração ocorre localmente, sem integração com outro serviço. As operações aleatórias também aceitam um `RandomGenerator` para permitir testes reproduzíveis.

## Exemplo de uso

```java
Mestre mestre = new Mestre("Guilherme");
Escudo escudo = mestre.criarEscudo("Minha campanha");
Documento notas = new Documento("Resumo", "O grupo chegou à taverna.");
escudo.getNavInfo().adicionarInfograma(notas);
Container bloco = escudo.getPainel().adicionarContainer(notas.getId(), 20, 40);

bloco.moverPara(80, 120);
notas.editarTexto("O grupo encontrou uma pista na taverna.");

// Esquerda: mestre.getEscudos()
// Centro: escudo.getPainel().getContainers()
// Direita: escudo.getNavInfo().listarInfogramas()
```

O exemplo completo está em `src/main/java/com/equipe/shieldman/ExemploShieldMan.java`.

A API valida os pedidos com Bean Validation e padroniza os erros como problem details. Veja [as regras de entrada, os exemplos de erro e os testes](docs/validacao.md).

## Documentação interativa da API (T8)

Com Docker Desktop iniciado, execute `docker compose up -d` (ou `mise run up`) e aguarde a API iniciar. Abra a [Swagger UI](http://localhost:8080/q/swagger-ui/) para consultar e executar as 10 operações de mestres e escudos pelo navegador.

A especificação está disponível em [OpenAPI YAML](http://localhost:8080/q/openapi) e [OpenAPI JSON](http://localhost:8080/q/openapi?format=json). Ela é gerada pelo `quarkus-smallrye-openapi` a partir das rotas, dos tipos Java, das validações e das anotações de documentação. Inclui UUIDs, limites da paginação, filtros, corpos de entrada, respostas e erros em `application/problem+json`.

A Swagger UI também fica disponível na aplicação empacotada. O build exporta cópias em `target/openapi/openapi.yaml` e `target/openapi/openapi.json`; esses arquivos são gerados e não precisam ser editados. Veja [como experimentar as operações e verificar a documentação](docs/openapi.md).

## Executar

Com Java 21 e Maven configurados no terminal:

```sh
mvn test
mvn package
java -cp target/classes com.equipe.shieldman.ExemploShieldMan
```

Se usa o mise, também pode executar `mise run test` e `mise run build` com as tarefas já existentes. O serviço `java-build` do Docker Compose compila e executa os testes com `docker compose run --rm java-build`.

## Próxima etapa

O exemplo de console usa objetos em memória. A aplicação inclui repositórios PostgreSQL e rotas para criar, consultar, listar, renomear e excluir mestres e escudos. Veja [as rotas e os exemplos no PowerShell](docs/api.md). Ainda não há telas ou autenticação. O vínculo entre mestre e escudo representa a propriedade no modelo, mas não substitui autorização na API.

O projeto inclui configuração Quarkus e migrações para mestre, escudo e conteúdos. A V2 prepara tabelas para NavInfo, painel, contêineres, documentos, tabelas de consulta e sorteio, nomes e sobrenomes. Consulte [o esquema e os testes das migrações](docs/esquema-banco.md). As classes de domínio continuam sem dependências de framework. O próximo passo é implementar os adaptadores e rotas dos conteúdos: a existência das novas tabelas ainda não faz o Java salvar os conteúdos automaticamente.

## Repositórios e caso de uso de consulta

As interfaces ficam em `domain/repository`, junto ao domínio. Os objetos e regras ficam em `domain/model`, e os casos de uso em `application`. As implementações PostgreSQL ficam em `infra/persistence` e são os beans CDI usados na aplicação. As implementações em mapas ficam em `infra/memory`, sem anotações CDI: os testes as instanciam diretamente. Ambas salvam apenas o cadastro do mestre e o cadastro do escudo; a gravação dos escudos é feita separadamente da gravação do mestre. Veja [a organização das camadas e como conferir os imports](docs/arquitetura.md).

Para testar sem banco, compartilhe um `BancoEmMemoria` entre os dois repositórios:

```java
BancoEmMemoria banco = new BancoEmMemoria();
MestreRepository mestres = new MestreEmMemoriaRepository(banco);
EscudoRepository escudos = new EscudoEmMemoriaRepository(banco);

Mestre mestre = new Mestre("Guilherme");
mestres.salvar(mestre);
Escudo escudo = mestre.criarEscudo("Minha campanha");
escudos.salvar(escudo);

ConsultarEscudo consultar = new ConsultarEscudo(escudos);
Escudo recuperado = consultar.executar(escudo.getId()).orElseThrow();
```

O exemplo completo com imports está em `src/test/java/com/equipe/shieldman/application/ConsultarEscudoTest.java`. Os repositórios retornam novos objetos para preservar os dados armazenados: renomear um objeto recuperado só altera o cadastro salvo depois de chamar `salvar` novamente. O banco em memória é exclusivo de cada teste e perde os dados ao encerrar o processo.

`ConsultarEscudo`, `GerenciarEscudos` e `GerenciarMestres` conhecem apenas as interfaces de repositório. A configuração `infra/config/CasosDeUsoConfig` monta esses casos de uso na aplicação. As rotas chamam os casos de uso e retornam os dados dos cadastros. Nenhuma rota ou caso de uso conhece as entidades JPA ou a implementação PostgreSQL.

Com a aplicação iniciada, consulte `http://localhost:8080/mestres` ou `http://localhost:8080/escudos`. Cadastre um mestre e um escudo com POST para obter UUIDs reais. Os testes unitários usam repositórios em memória. `ApiPostgresTest` verifica HTTP e PostgreSQL quando habilitado explicitamente; `scripts/testar-persistencia.ps1` verifica a recuperação dos dados após reiniciar a aplicação. Consulte [a documentação da API](docs/api.md) para executar cada verificação.


Pré-requisitos: Docker Desktop instalado e iniciado; mise é opcional.
Inicialização: docker compose up -d ou mise run up.
Verificação: docker compose ps e docker compose logs -f api.
Encerramento: docker compose down, preservando o volume.
Configuração: usuário shieldman, banco shieldman e senha padrão de desenvolvimento; substituição opcional por variáveis ou .env.
