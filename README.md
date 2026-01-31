
# Health Data Pipeline & Analytics Platform

> **Desafio Técnico** > Solução Full Stack para Engenharia de Dados na Saúde Suplementar.

Este repositório contém uma solução completa para o ciclo de vida de dados da ANS (Agência Nacional de Saúde Suplementar), abrangendo desde a extração automatizada (ETL) até a visualização em dashboard web.

---

## 🗂️ Estrutura Modular do Projeto

O projeto foi arquitetado em 4 módulos independentes, facilitando a manutenção, escalabilidade e separação de responsabilidades:

```text
/
├── 01_etl_ans/           # [Java 21] Pipeline de Extração, Transformação e Carga (ETL)
├── 02_validacao_dados/   # [Python] Scripts de validação de CNPJ e enriquecimento
├── 03_analise_sql/       # [SQL] Modelagem Dimensional e Queries Analíticas
├── 04_plataforma_web/    # [Vue.js + Python] Dashboard e API REST
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


**Autor:** Nicolas Harnisch


# Health Data Pipeline & Analytics Platform

> **Desafio Técnico** > Solução Full Stack para Engenharia de Dados na Saúde Suplementar.

Este repositório contém uma solução completa para o ciclo de vida de dados da ANS (Agência Nacional de Saúde Suplementar), abrangendo desde a extração automatizada (ETL) até a visualização em dashboard web.

---

## 🗂️ Estrutura Modular do Projeto

O projeto foi arquitetado em 4 módulos independentes, facilitando a manutenção, escalabilidade e separação de responsabilidades:

```text
/
├── 01_etl_ans/           # [Java 21] Pipeline de Extração, Transformação e Carga (ETL)
├── 02_validacao_dados/   # [Python] Scripts de validação de CNPJ e enriquecimento
├── 03_analise_sql/       # [SQL] Modelagem Dimensional e Queries Analíticas
├── 04_plataforma_web/    # [Vue.js + Python] Dashboard e API REST
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


**Autor:** Nicolas Harnisch

