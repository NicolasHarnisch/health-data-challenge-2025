# Health Data Pipeline & Analytics Platform

> **Desafio Técnico** > Solução Full Stack para Engenharia de Dados na Saúde Suplementar.

Este repositório contém uma solução completa para o ciclo de vida de dados da ANS (Agência Nacional de Saúde Suplementar), abrangendo desde a extração automatizada (ETL) até a visualização em dashboard web.

---

## 🗂️ Estrutura Modular do Projeto

O projeto foi arquitetado em 4 módulos independentes, facilitando a manutenção, escalabilidade e separação de responsabilidades:

```text
/
├── 01_etl_ans/           # [Java 21] Pipeline de Extração, Transformação e Carga (ETL)
├── 02_validacao_dados/   # [Java 21] Validação de CNPJ e Enriquecimento Estatístico
├── 03_analise_sql/       # [SQL] Modelagem Dimensional e Queries Analíticas
├── 04_plataforma_web/    # [Python + Vue.js] API REST (FastAPI) e Dashboard
└── data/                 # Data Lake local (Raw e Processed)

```

---

## 🚀 Módulo 1: ETL de Demonstrações Contábeis (Java)

**Localização:** [`./01_etl_ans`](https://www.google.com/search?q=./01_etl_ans)

**Tecnologia:** Java 21 (LTS), Maven, JUnit 5.

Este módulo é o coração da ingestão de dados. Ele automatiza a coleta de demonstrações contábeis trimestrais, resolve inconsistências de formato e consolida os dados para análise posterior.

### 🛠️ Arquitetura da Solução

O pipeline executa 4 estágios sequenciais e atômicos:

1. **Crawler Inteligente (`CrawlerANS`):**
* Varre recursivamente o servidor FTP da ANS.
* Identifica dinamicamente os 3 trimestres mais recentes disponíveis (via Regex).
* Realiza o download resiliente, detectando automaticamente se o alvo é um arquivo `.zip` direto ou uma estrutura de diretórios.


2. **Descompactação Segura (`Descompactador`):**
* Extrai os arquivos CSV para uma área de *staging* temporária.
* Força o encoding **ISO-8859-1** para garantir a leitura correta de caracteres acentuados (padrão legado governamental).


3. **Parsing & Normalização (`LeitorCSV`):**
* Detecta automaticamente variações de schema (ex: colunas `VL_SALDO_FINAL` vs `Valor`).
* Converte formatação monetária brasileira (PT-BR) para `BigDecimal`.
* Enriquece os dados extraindo Ano e Trimestre diretamente do nome do arquivo (Fonte de verdade).


4. **Consolidação (`Consolidador`):**
* Aplica regras de negócio (filtragem de dados inconsistentes).
* Gera o arquivo final unificado `consolidado_despesas.csv` e o compacta em ZIP.



---

## 🧠 Decisões Técnicas e Trade-offs (Análise Crítica)

Conforme os critérios de avaliação do desafio, abaixo estão as justificativas para as decisões de engenharia adotadas:

### 1. Estratégia de Processamento: *In-Memory* vs *Streaming*

* **Decisão:** Processamento em Memória (Listas).
* **Contexto:** O volume de dados dos últimos 3 trimestres (~2.1 milhões de registros) ocupa aproximadamente 600MB na Heap da JVM.
* **Justificativa:** Optou-se pela abordagem *In-Memory* para reduzir a complexidade acidental do código (*KISS - Keep It Simple*) e permitir operações rápidas sem o overhead de I/O constante.
* **Performance:** O tempo total de execução (~11 segundos) valida que a memória não é um gargalo para este volume de dados. Caso o requisito mudasse para "Histórico de 10 anos", a arquitetura seria refatorada para *Streaming*.

### 2. Tratamento de Inconsistências (Qualidade de Dados)

* **Remoção de Valores <= 0:** Em análises de despesas assistenciais para BI, valores negativos (geralmente estornos contábeis) distorcem as métricas de agregação. A limpeza removeu cerca de 40% de "ruído" do dataset, aumentando a precisão analítica.
* **Datas via Metadados:** As colunas de data dentro dos arquivos CSV originais apresentaram instabilidade de formato. A extração do período via Regex no nome do arquivo (ex: `1T2025.csv`) garantiu consistência temporal absoluta.

### 3. Stack Tecnológica (Java 21)

* A escolha do **Java 21** permitiu o uso de *Text Blocks* e métodos modernos de Coleções (`List.getFirst()`), resultando em um código mais limpo, legível e seguro comparado a versões legadas (Java 8/11).

---

## ✅ Diferenciais Implementados

Este projeto implementa requisitos de **Qualidade de Software** listados como diferenciais no descritivo da vaga:

* **🧪 Testes Automatizados:**
* Implementação de testes unitários com **JUnit 5** e **AssertJ**.
* Cobertura crítica da classe `LeitorCSV`, validando: conversão monetária, encoding ISO-8859-1 e extração de datas.


* **⚡ Performance e Resiliência:**
* Uso de `User-Agent` rotativo no Crawler para evitar bloqueios (HTTP 403).
* Parsing otimizado com `Apache Commons CSV`.


* **🏗️ Organização e Boas Práticas:**
* Separação clara de responsabilidades (SRP) em pacotes (`etl`, `model`, `parser`).
* Logs profissionais (`java.util.logging`) para rastreabilidade de execução.



---

## ▶️ Como Executar

### Pré-requisitos

* Java JDK 21+
* Maven 3.8+

### Passo a Passo

1. Acesse o diretório do módulo:
```bash
cd 01_etl_ans
```


2. **Execute os testes unitários** (Para validar a integridade do código):
```bash
mvn test
```


3. **Execute o Pipeline ETL** (Para gerar os dados):
```bash
mvn clean install
java -cp target/classes:target/dependency/* br.com.seu.Main
```



**Resultado:** O arquivo processado final estará disponível em:
`data/processed/consolidado_despesas.zip`

---

## 🧪 Módulo 2: Validação e Enriquecimento de Dados (Java)

**Localização:** [`./02_validacao_dados`](./02_validacao_dados)

**Tecnologia:** Java 21 (LTS), Apache Commons CSV, JUnit 5.

Este módulo atua como a camada de *Quality Assurance* (QA) e Enriquecimento. Ele consome os dados brutos gerados pelo ETL, aplica validações matemáticas (CNPJ), cruza com bases externas da ANS e gera métricas estatísticas para suporte à decisão.

### 🛠️ Arquitetura da Solução

O pipeline executa 4 estágios sequenciais e atômicos:

1. **Coleta de Referência (`BaixadorCadastro`):**
    * Conecta-se à API de Dados Abertos da ANS para baixar o cadastro atualizado de operadoras.
    * Implementa um cliente HTTP resiliente (simulando *Browser User-Agent*) para evitar bloqueios de segurança (Erro 403).

2. **Parsing Resiliente (`CsvUtil`):**
    * Lê arquivos CSV ignorando BOM (*Byte Order Mark*) e variações de encoding (UTF-8 vs ISO-8859-1).
    * Aplica sanitização de dados: remoção de caracteres não numéricos e normalização de nomes.

3. **Validação Matemática (`ValidadorCNPJ`):**
    * Implementa o algoritmo **Módulo 11** para verificar a autenticidade dos dígitos verificadores dos CNPJs.
    * Classifica os registros sem descartá-los (estratégia de *Soft Validation*).

4. **Enriquecimento & Analytics (`ProcessadorJoin`):**
    * Realiza o cruzamento de dados (*Join*) entre as Despesas Financeiras e o Cadastro da ANS.
    * Calcula métricas agregadas por operadora: Soma Total, Média Trimestral e **Desvio Padrão Amostral**.

---

## 🧠 Decisões Técnicas e Trade-offs (Análise Crítica)

Conforme os critérios de avaliação, abaixo estão as justificativas para as decisões de engenharia adotadas neste módulo:

### 1. Estratégia de Join: *Hash Map* vs *Nested Loop*

* **Decisão:** *In-Memory Hash Join*.
* **Contexto:** O cadastro de operadoras possui apenas ~1.200 registros, cabendo confortavelmente na memória.
* **Justificativa:** Carregar o cadastro em um `HashMap<String, Operadora>` permite acesso com complexidade **O(1)**. Isso torna o cruzamento com as milhares de linhas de despesas exponencialmente mais rápido do que uma busca linear ou laços aninhados (O(N*M)).

### 2. Validação de Dados: *Flagging* vs *Dropping*

* **Decisão:** *Flagging* (Marcar com `CNPJ_Valido = false`).
* **Contexto:** Registros financeiros contêm valores monetários que compõem o balanço total.
* **Justificativa:** Em sistemas financeiros, descartar uma linha devido a um erro de digitação no cadastro (typo) altera o montante final ("furo no caixa"). A estratégia de marcar o registro permite auditoria posterior sem perda de integridade contábil.

### 3. Resolução de Chaves (Análise de Qualidade)

* **Problema:** O uso inicial do **CNPJ** como chave de ligação resultou em 100% de falha (0 matches) devido a inconsistências de formatação na fonte.
* **Solução:** Alteração da chave primária de cruzamento para o **Número de Registro na ANS**.
* **Resultado:** A taxa de sucesso subiu para **~99%**, restando apenas operadoras inativas ou canceladas, que foram tratadas como "NAO ENCONTRADA" para manter a rastreabilidade.

---

## ✅ Diferenciais Implementados

* **🧪 Testes Unitários Matemáticos:**
    * Cobertura de testes na classe `ValidadorCNPJ` garantindo a precisão do algoritmo Módulo 11.
    * Validação de casos de borda (CNPJs com dígitos iguais, nulos ou formato incorreto).

* **📊 Estatística Descritiva:**
    * Implementação manual do cálculo de **Desvio Padrão** (`EstatisticaService`) para identificar volatilidade nas despesas, sem dependência de bibliotecas pesadas de Data Science.

---

## ▶️ Como Executar

### Passo a Passo

1. Acesse o diretório do módulo:
```bash
cd 02_validacao_dados
```

2. **Execute os testes unitários** (Para validar a matemática do CNPJ):
```bash
mvn test
```


3. **Execute o Processamento**:
```bash
mvn clean install
java -cp target/classes:target/dependency/* br.com.seu.Main
```

**Resultado:** O arquivo enriquecido final estará disponível em:
`data/processed/despesas_agregadas.csv`

---

## 🗄️ Módulo 3: Banco de Dados e Análise SQL

**Localização:** [`./03_analise_sql/sql`](https://www.google.com/search?q=./03_analise_sql/sql)

**Tecnologia:** MySQL 8.0 (Compatível com PostgreSQL), SQL ANSI/ISO.

Este módulo atua como a camada de *Data Warehousing* e Inteligência. Ele estrutura o esquema do banco de dados, orquestra a carga massiva dos dados processados (ETL) e executa queries analíticas complexas para extração de insights de negócio.

### 🛠️ Arquitetura da Solução

O pipeline de dados no banco segue 3 estágios lógicos:

1. **Modelagem Relacional (DDL):**
* Criação de tabelas normalizadas (`operadoras` vs `demonstracoes_financeiras`) com chaves estrangeiras e índices otimizados para leitura temporal.
* Definição rigorosa de tipagem (`DECIMAL` para valores monetários, `DATE` para referências temporais).


2. **Ingestão de Alta Performance (ETL):**
* Utilização do comando `LOAD DATA LOCAL INFILE` para *Bulk Insert*, garantindo carga de milhões de registros em segundos.
* **Sanitização *On-the-Fly*:** Scripts SQL que tratam formatação de moeda (`R$`) e datas (`DD/MM/YYYY` -> ISO) durante o processo de carga, sem necessidade de pré-processamento externo.


3. **Análise de Negócio (DQL):**
* Execução de queries analíticas utilizando recursos avançados do SQL moderno, como *Common Table Expressions* (CTEs) e *Window Functions*.



---

## 🧠 Decisões Técnicas e Trade-offs (Análise Crítica)

Conforme os requisitos do desafio, abaixo estão as justificativas para as decisões de modelagem e consulta adotadas:

### 1. Modelagem: Normalização (Opção B) vs Desnormalização

* **Decisão:** Tabelas Relacionais Normalizadas.
* **Contexto:** O dataset contém dados cadastrais repetitivos associados a milhões de transações financeiras.
* **Justificativa:** Separar dados cadastrais (Razão Social, UF) das transações economiza gigabytes de armazenamento e garante integridade referencial. Alterações cadastrais são refletidas instantaneamente sem a necessidade de *updates* massivos na tabela de fatos, facilitando a manutenção.

### 2. Tipos de Dados: `DECIMAL` vs `FLOAT`

* **Decisão:** Uso de `DECIMAL(18,2)`.
* **Contexto:** Armazenamento de valores contábeis de demonstrações financeiras.
* **Justificativa:** Tipos de ponto flutuante (`FLOAT`, `DOUBLE`) introduzem erros de arredondamento em operações de soma massiva. O tipo `DECIMAL` garante precisão exata dos centavos, requisito obrigatório para sistemas financeiros.

### 3. Estratégia de Query: *Window Functions* vs *Subqueries*

* **Decisão:** Uso de `AVG() OVER(PARTITION BY...)`.
* **Contexto:** Cálculo de operadoras consistentemente acima da média do mercado (Query 3).
* **Justificativa:** Subqueries correlacionadas possuem complexidade quadrática O(N²), tornando a consulta lenta conforme o banco cresce. *Window Functions* permitem calcular a média do mercado e comparar com a operadora em uma única passada de leitura (*Table Scan*), garantindo escalabilidade e performance superior.

---

## ✅ Diferenciais Implementados

* **🚀 Carga Otimizada (Bulk Insert):**
* A estratégia de `LOAD DATA` é aproximadamente **20x mais rápida** que inserções linha a linha (`INSERT INTO`), viabilizando a reimportação frequente do banco.


* **🛡️ Tratamento de Casos de Borda (Crescimento):**
* Na análise de crescimento percentual (Query 1), o script utiliza `INNER JOIN` entre trimestres extremos para excluir matematicamente operadoras que não existiam no início ou fim do período, evitando divisões por zero ou resultados tendenciosos.

---

### ▶️ Como Executar

#### Passo a Passo

1.  **Acesse o diretório dos scripts:**
    ```bash
    cd 03_analise_sql/sql
    ```


2.  **Crie a Estrutura (DDL):**
    No seu cliente SQL (IntelliJ/Workbench), execute o arquivo:
    `01_ddl_create_tables.sql`



3.  **Execute a Carga (Import):**
    Primeiro, habilite a importação local no console do banco:
    ```sql
    SET GLOBAL local_infile = 1;
    ```
    Em seguida, ajuste o caminho do CSV no script e execute:
    `02_import_data.sql`


4.  **Gere os Relatórios:**
    Execute o script de análise:
    `03_queries_analiticas.sql`

**Resultado:** As tabelas do banco `ans_analytics` serão populadas e as queries retornarão os rankings de crescimento, distribuição por UF e consistência financeira.

---

## 🌐 Módulo 4: Plataforma Web & API Analytics

**Localização:** [`./04_plataforma_web`](./04_plataforma_web)

**Tecnologia:** Python 3.10+ (FastAPI), Vue.js 3, Chart.js, Bootstrap 5, CSS Customizado, Docker.

Este módulo é a camada de apresentação e distribuição da solução. Ele consiste em uma API REST de alta performance, conteinerizada com Docker, que expõe os dados do *Data Warehouse* e um Dashboard Interativo *Single Page Application* (SPA) com visual *Enterprise* para visualização de métricas financeiras em tempo real.

### 🛠️ Arquitetura da Solução

O sistema opera em uma arquitetura desacoplada (Client-Server):

1. **Backend Assíncrono (`main.py`):**
* Desenvolvido com **FastAPI**, utilizando `Uvicorn` como servidor ASGI.
* Gerencia conexões com o MySQL via *Connection Pooling* para suportar múltiplas requisições simultâneas.
* Expõe endpoints RESTful com suporte a paginação, ordenação e filtros dinâmicos (ex: `/operadoras?uf=SP&sort=nome`), com documentação automática (Swagger UI).
* Ambiente de execução totalmente isolado e padronizado através de **Docker**.

2. **Frontend Reativo (`index.html`, `app.js`, `style.css`):**
* Construído com **Vue.js 3** (via CDN) utilizando a *Composition API* para reatividade sem necessidade de *build steps* complexos.
* Layout moderno baseado em *CSS Grid/Flexbox* (Sidebar + Main Content), simulando uma experiência SaaS.
* Renderiza gráficos vetoriais interativos com **Chart.js** e tabelas dinâmicas.

---

## 🧠 Decisões Técnicas e Trade-offs (Análise Crítica)

Conforme os critérios de avaliação, abaixo estão as justificativas para as decisões de engenharia de software adotadas:

### 1. Framework Backend: FastAPI vs Flask/Django
* **Decisão:** FastAPI.
* **Contexto:** Necessidade de servir dados agregados de milhões de linhas com baixa latência.
* **Justificativa:** Diferente do Flask (síncrono por padrão) ou Django (monolítico e pesado), o FastAPI oferece suporte nativo a concorrência (`async/await`). Isso permite que o servidor processe novas requisições enquanto aguarda o I/O do banco de dados, maximizando o *throughput* sem bloquear a thread principal.

### 2. Arquitetura Frontend: *No-Build* (CDN) vs *Bundler* (Vite/Webpack)
* **Decisão:** Abordagem *No-Build* com arquivos separados.
* **Contexto:** O projeto precisa ser avaliado rapidamente por recrutadores/revisores, mantendo um código limpo e escalável.
* **Justificativa:** Elimina a necessidade de instalar Node.js ou gerenciar `node_modules`. O avaliador precisa apenas abrir o arquivo `index.html` no navegador. A separação lógica em `app.js` e `style.css` prepara o terreno caso o projeto migre para um *bundler* futuramente.

### 3. Design System e UX: Custom UI vs Padrão Bootstrap
* **Decisão:** Criação de um Design System próprio (*Soft UI*).
* **Contexto:** Dashboards com Bootstrap puro costumam ter a aparência de sistemas engessados.
* **Justificativa:** A interface foi reescrita utilizando uma *Sidebar* estrutural e conteúdo em *cards* modernos, elevando a percepção visual do sistema para um produto B2B atual, mantendo o Bootstrap apenas para utilitários (`d-flex`, `m-4`) e a Grid.

### 4. Infraestrutura: Docker vs Execução Local
* **Decisão:** Conteinerização da API.
* **Contexto:** Evitar o problema de "na minha máquina funciona" devido a configurações de sistema operacional.
* **Justificativa:** O Docker empacota o Python, o framework e as dependências em uma imagem enxuta baseada em `slim`. Isso garante paridade absoluta entre os ambientes de desenvolvimento, teste e produção.

---

## ✅ Diferenciais Implementados

* **⚡ Otimização de UX (Debounce):**
* Implementação de lógica de *Debounce* na barra de busca. A requisição ao backend só é disparada após o usuário parar de digitar por 500ms, prevenindo inundações de requisições (*Flood*) no banco de dados.

* **📈 Gráficos Interativos (Drill-down):**
* O Chart.js está intrinsecamente ligado ao estado reativo do Vue. Clicar em uma barra específica de Estado (UF) no gráfico aplica automaticamente um filtro cruzado na tabela de operadoras abaixo.

* **👻 Skeleton Loaders (Percepção de Performance):**
* Substituição dos clássicos *spinners* de carregamento por "esqueletos" animados. Essa técnica melhora a **Performance Percebida** da aplicação durante chamadas de rede.

* **📊 Visualização de Big Data:**
* O dashboard foi calibrado para renderizar e formatar corretamente volumes financeiros na casa dos **Trilhões**, tratando precisão de ponto flutuante e localização monetária (PT-BR) no Frontend.

---

## ▶️ Como Executar

### Opção A: Via Docker (Recomendado)

**Pré-requisitos:** Docker Desktop instalado e rodando. Banco de Dados MySQL populado (Módulo 3) ativo na sua máquina.

1. Acesse o diretório do módulo:
```bash
cd 04_plataforma_web
```

2. Construa a imagem da API:
```bash
docker build -t ans-api .
```

3. Inicie o container (O parâmetro `host.docker.internal` conecta o container ao seu MySQL local):
```bash
docker run -d -p 8000:8000 --name backend-ans \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=3306 \
  -e DB_USER=root \
  -e DB_PASSWORD=1234 \
  -e DB_NAME=ans_analytics \
  ans-api
```

> **Nota:** Ajuste `DB_PASSWORD=1234` se o seu MySQL local utilizar outra senha.

4. Acesse o dashboard:
   - **API:** `http://localhost:8000`
   - **Swagger:** `http://localhost:8000/docs`
   - **Frontend:** abra `index.html` no navegador (clique duplo ou extensão Live Server)

---

### Opção B: Execução Manual (Sem Docker)

**Pré-requisitos:** Python 3.10+, pip, MySQL ativo.

1. Acesse o diretório do módulo:
```bash
cd 04_plataforma_web
```

2. Instale as dependências:
```bash
pip install -r requirements.txt
```

3. Inicie o servidor backend:
```bash
python -m uvicorn main:app --reload
```

   Você verá no terminal: `Uvicorn running on http://127.0.0.1:8000`

4. Abra o arquivo `index.html` no navegador para acessar o frontend.