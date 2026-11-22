# TropaTur API

CRUD de pontos turísticos para a atividade de Programação Orientada a Objetos. A API e uma interface demonstrativa são servidas pelo mesmo Spring Boot. O banco padrão é H2 **em arquivo**, não em memória: os cadastros sobrevivem ao reinício. Não é necessário instalar PostgreSQL para essa configuração.

## Executar nesta máquina pelo PowerShell

```powershell
cd 'C:\Meus Projetos\fatec\socorro\tropatur-api'
.\rodar.ps1
```

O script usa o JDK 21 e Maven baixados em `..\.tools`. Se a política local bloquear scripts, execute somente para esse processo:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\rodar.ps1
```

Aguarde `Started TropaturApplication`. Abra:

- Interface: http://localhost:8080/
- Swagger: http://localhost:8080/swagger-ui.html
- Contrato JSON OpenAPI: http://localhost:8080/v3/api-docs

Encerre com Ctrl+C. Não execute simultaneamente pelo terminal e Spring Tools: ambos usariam a mesma porta e arquivo de banco. O servidor escuta apenas em `127.0.0.1`, para apresentação local.

## Importar e executar no Spring Tools

1. Abra o atalho **Spring Tools**. Use o workspace `C:\Meus Projetos\fatec\socorro\spring-workspace`.
2. Em **Window → Preferences → Java → Installed JREs → Add → Standard VM**, selecione a pasta do JDK em `C:\Meus Projetos\fatec\socorro\.tools\jdk-21.0.13.1+2`. Marque esse JDK como padrão. Em **Execution Environments → JavaSE-21**, associe o JDK 21. O Java que inicia a IDE é independente do Java usado pelo projeto.
3. **File → Import → Maven → Existing Maven Projects**. Em Root Directory, selecione `C:\Meus Projetos\fatec\socorro\tropatur-api`, marque o `pom.xml` e finalize. Não importe os documentos nem `.tools` como projetos.
4. Aguarde o download das dependências. Se necessário, botão direito no projeto → **Maven → Update Project**. A primeira importação exige internet; o Maven do terminal utiliza cache em `.tools\m2`, enquanto a IDE pode utilizar o cache padrão do usuário.
5. Abra `src/main/java/br/com/tropatur/TropaturApplication.java`, botão direito → **Run As → Spring Boot App**. Alternativamente, execute `TropaTur.launch` por Run As. Use como working directory a pasta `tropatur-api` para manter o banco sempre no mesmo local.
6. Abra a interface ou Swagger nos endereços acima. Para parar, use o botão vermelho **Terminate** na Console/Boot Dashboard.

Se houver erro `release version 21 not supported`, o projeto está usando Java antigo: ajuste **Properties → Java Build Path → JRE System Library** para JavaSE-21. Se aparecer `Port 8080 already in use`, encerre a outra execução ou configure `PORT=8081` nas variáveis da execução. Se aparecer banco bloqueado, pare a outra instância da aplicação.

## Outra máquina e Maven Wrapper

Instale JDK 21, configure `JAVA_HOME` para a pasta do JDK e use:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
.\mvnw.cmd package
java -jar .\target\tropatur-api-0.0.1-SNAPSHOT.jar
```

O último comando também exige Java 21. O Java 8 previamente no PATH desta máquina não executa o projeto. Não é necessário instalar Maven globalmente: o wrapper baixa a distribuição no primeiro uso. Não envie `.tools`, `target`, `data` nem `spring-workspace` como código-fonte; inclua os arquivos ocultos `.mvn` e `mvnw*` ao copiar o projeto.

## API

| Método | Endpoint | Sucesso | Erros de entrada/registro |
|---|---|---|---|
| POST | `/api/pontos-turisticos` | 201 + JSON e Location | 400 |
| GET | `/api/pontos-turisticos` | 200 + lista (inclusive vazia) | — |
| GET | `/api/pontos-turisticos/{id}` | 200 + JSON | 400, 404 |
| PUT | `/api/pontos-turisticos/{id}` | 200 + JSON | 400, 404 |
| DELETE | `/api/pontos-turisticos/{id}` | 204 sem corpo | 400, 404 |

PUT substitui todos os campos editáveis e não cria registros inexistentes. O id é gerado pelo banco e não deve ser enviado no JSON. Nome (até 200), descrição (até 10000) e endereço (até 300) são obrigatórios, não aceitam texto em branco e têm espaços externos removidos. Coordenadas são obrigatórias, com até 7 casas decimais: latitude [-90, 90] e longitude [-180, 180]. IDs devem ser inteiros positivos. JSON desconhecido ou malformado retorna 400. Erros tratados incluem `timestamp`, `status`, `mensagem`, `caminho` e `campos`.

Exemplo **fictício**, sem afirmação histórica e sem localização validada:

```powershell
$base = 'http://localhost:8080/api/pontos-turisticos'
$json = @{
  nome = 'Ponto fictício para demonstração'
  descricao = 'Registro de teste; não é conteúdo histórico verificado.'
  endereco = 'Endereço fictício'
  latitude = -23.59
  longitude = -48.05
} | ConvertTo-Json
$ponto = Invoke-RestMethod $base -Method Post -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($json))
Invoke-RestMethod $base
Invoke-RestMethod "$base/$($ponto.id)"
$json = $json.Replace('Ponto fictício para demonstração', 'Ponto fictício atualizado')
Invoke-RestMethod "$base/$($ponto.id)" -Method Put -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($json))
Invoke-RestMethod "$base/$($ponto.id)" -Method Delete
```

## Banco e correspondência com o documento

Foi lido o escopo e SQL de `BD_TrapaTur2.docx` antes da implementação. A entrega reduz `ponto_turistico` aos campos necessários: `id_ponto` (Long/identity, exposto como `id`), `nome VARCHAR(200)`, `descricao TEXT`, `endereco VARCHAR(300)`, `latitude DECIMAL(10,7)` e `longitude DECIMAL(10,7)`. Descrição, endereço e coordenadas passam a ser obrigatórios neste recorte. Categoria, horários, preços, imagens e demais relacionamentos não foram implementados. Não execute o script completo do documento sobre este banco.

Hibernate cria e atualiza a tabela com `ddl-auto=update`, opção simples para a demonstração. Para evolução/produção, adotar migrações versionadas e autenticação. O arquivo padrão é `data/tropatur.mv.db`, relativo ao diretório de execução. Os testes usam um H2 separado em memória, sem apagar os cadastros de demonstração. Nenhum dado ilustrativo do documento é carregado automaticamente.

### Perfil PostgreSQL opcional

Não foram encontrados serviço PostgreSQL nem instalação na pasta padrão durante a verificação. O perfil foi preparado, mas precisa de um servidor instalado e de validação própria. H2 não comprova compatibilidade de execução PostgreSQL.

Após instalar PostgreSQL e conectar como administrador pelo pgAdmin/psql, crie um usuário e um banco novos (não reaproveite banco com a modelagem completa):

```sql
CREATE USER tropatur WITH PASSWORD 'SUBSTITUA_POR_SENHA_LOCAL';
CREATE DATABASE tropatur OWNER tropatur;
```

Defina no terminal (ou em **Run Configurations → Environment** no Spring Tools):

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/tropatur'
$env:DB_USERNAME = 'tropatur'
$env:DB_PASSWORD = 'SUA_SENHA_LOCAL'
.\rodar.ps1 -Postgres
```

Na IDE, acrescente `--spring.profiles.active=postgres` em **Program arguments**. Não coloque senhas reais nos arquivos do projeto. Sem esse perfil, a aplicação continua usando H2.

## Tecnologias e organização

Java 21, Maven, Spring Boot 3.5.16, Spring Web MVC, Spring Data JPA/Hibernate, Jakarta Validation, H2, driver PostgreSQL, JUnit 5 e MockMvc. Frontend HTML/CSS/JavaScript sem ferramentas adicionais, usando `fetch` para chamar a API. Sem CORS: interface e API têm a mesma origem.

`controller → service → repository → banco`, com entidade em `model`, contratos em `dto`, tratamento de erros em `exception` e metadados em `config`. A entidade encapsula os dados, e as dependências das camadas são recebidas por construtor. `@Transactional` delimita alterações persistentes.

O enunciado cita `springdoc-openapi-ui`, da linha 1.x. Para Spring Boot 3 e Jakarta, o equivalente é **`springdoc-openapi-starter-webmvc-ui` 2.x** (aqui 2.8.17). Metadados estão em `OpenApiConfig`, operações/respostas em `PontoTuristicoController`, validações e exemplos no DTO. Referência: https://springdoc.org/v2/ e https://springdoc.org/#migrating-from-springdoc-v1

## Testar e apresentar

```powershell
.\rodar.ps1 -Testar
.\rodar.ps1 -Empacotar
```

Testes automatizados em `PontoTuristicoIntegrationTest`: CRUD com banco real H2 de teste, limites de coordenadas, obrigatoriedade, formatos/precisão, atualização inválida sem modificar o dado, IDs inexistentes/inválidos, Swagger e contrato das cinco operações. Relatórios em `target/surefire-reports`.

Roteiro de apresentação: cadastrar um exemplo explicitamente fictício, listar, consultar, editar, reiniciar o servidor para mostrar persistência, excluir, consultar o id excluído (404) e enviar dados inválidos pelo Swagger (400). Consulte `../docs/PROGRESSO.md` para o que efetivamente foi verificado nesta máquina.

## Limitações e Engenharia de Software

A arquitetura implementada é navegador → API REST → JPA → banco. O cadastro mínimo cobre parcialmente RF-06 (informações práticas) e RF-08 (manutenção de pontos) do documento. Não cumpre o requisito amplo de administração protegida: autenticação está fora desta primeira entrega. O servidor é destinado a demonstração local.

Não há login, mapas, rotas, comentários, upload, referências bibliográficas estruturadas nem recursos offline. Os textos exigem revisão histórica externa. A exportação do Google Stitch não estava disponível; a interface aqui é demonstrativa e sua integração futura depende de inspeção do código exportado. O relatório final deve separar este recorte das funcionalidades planejadas e registrar somente os testes realmente executados. Grupo e prazo da atividade permanecem conforme o enunciado: três/quatro integrantes e 27/11/2026.

## Integração contínua
O workflow .github/workflows/testes.yml executa Maven verify com Java 21 a cada push e pull request. A suíte inclui 16 testes de integração e três testes de unidade do serviço. Os testes de unidade verificam atualização de registro existente e impedem gravação/exclusão quando o identificador não existe. Consulte a aba Actions do repositório para o resultado de cada execução.
