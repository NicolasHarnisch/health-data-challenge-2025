
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

**Autor:** Nicolas Harnisch

