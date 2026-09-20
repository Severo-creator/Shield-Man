VIDEO - https://youtu.be/pYMCCR2AIIo
Conteúdo na pasta Docs.


Nome: Guilherme Severo de Souza
Integração:  Projeto de Interfaces de Usuário





Visão do produto

Para mestres de RPG de mesa presencial ou remoto que precisam lembrar de informações de seus sistemas de rpg.

O Shield-Man é uma ferramenta para modelar escudos de mestre improvisado
Que facilita os mestres à lembrarem de pontos importantes sem abrir várias abas
Diferente de muitos VTTs que tem designs confusos e muita informação de uma vez.

Nosso produto visa simplicidade de praticidade na hora de ministrar informações em uma sessão de rpg




Escolho Java com Quarkus

Escolho essa opção por estar mais familiarizado e entender melhor a base dessa linguagem. Como tenho pouca experiência com programação web, pretendo relembrar algumas coisas com uma linguagem que já mexi. Pois alguns termos, simplesmente não os conheço e terei que ter muito tempo para aprender também. Então, aprender uma ferramenta nova no momento em que estou é inviável. Além de que em Java eu posso tirar dúvidas com conhecidos próximos. 



Escopo MVP
Os modelos MVP em relação ao não MVP



MVP
FORA DO MVP
Criar, editar e excluir escudos 
Compartilhar escudos publicamente 
Criar, editar e excluir contêineres 
Edição simultânea entre usuários 
Mover contêineres e salvar suas posições 
Colaboração em tempo real  
Personalizar manualmente os elementos do escudo 
Importação automática de PDFs 
Criar e editar documentos de texto 
Processamento automático de PDFs 
Criar uma ordem de iniciativa simples 
Sistema avançado/inteligente de iniciativa 
Gerar nomes aleatórios 
Associar automaticamente nomes a NPCs e documentos 
Criar tabelas de consulta e rolagem 
Executar automaticamente resultados complexos das tabelas 
Criar e editar fichas de NPCs 
Gerar fichas de NPCs utilizando IA 




BackLog Inicial



Prio
História
Critérios de Aceitação
Sprint
P1
Como mestre, quero criar um novo escudo para poder organizá-lo. 
CRUD de escudos; escudo associado ao usuário; listagem dos escudos criados 
1
P1
Como mestre, quero adicionar contêineres ao meu escudo para organizar informações  
Criação e remoção de contêineres; contêiner associado ao escudo; alterações persistidas 
1
P1
Como mestre, quero mudar as posições dos contêineres do meu escudo e salvar a mudança 
Contêiner arrastável; posição X/Y persistida; posição restaurada ao carregar o escudo  
1
P1


Como mestre, quero editar as informações presentes em um contêiner 
Conteúdo editável; atualização persistida; conteúdo recuperado ao carregar o escudo 
1
P1
Como mestre, quero abrir meus escudos salvos para continuar editando-os 
Listagem dos escudos do usuário; carregamento dos contêineres, conteúdos e posições 
1


A divisão entre o serviço principal e os microsserviços Go


Serviço Principal
Microsserviço GO
Entidades e domínios e suas regras
Sistema de atualização de conteúdos do escudo
Gerenciamento de informações do mestre
Sorteio de nome aleatório em nome de banco
Autenticação e autorização 
Integrações externas
Orquestração dos casos de uso 


































Lista de Classes




Exemplo de interface de usuário




Inspirações


