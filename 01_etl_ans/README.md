# 🚀 Módulo 1: ETL de Demonstrações Contábeis

Este módulo é o coração da ingestão de dados. Ele automatiza a coleta de demonstrações contábeis (CSV) do servidor FTP da ANS, resolve inconsistências de formato e consolida os dados.

## 🛠️ Tecnologias
* **Linguagem:** Java 21 (LTS)
* **Build:** Maven
* **Testes:** JUnit 5

## ⚙️ O que ele faz (Pipeline)
1.  **Crawler:** Varre o servidor FTP da ANS e baixa os 3 últimos trimestres.
2.  **Descompactador:** Extrai ZIPs e corrige encoding (ISO-8859-1 para UTF-8).
3.  **Parser:** Lê CSVs, detecta colunas dinamicamente e converte moeda (PT-BR) para `BigDecimal`.
4.  **Consolidador:** Gera um único arquivo `consolidado_despesas.csv`.

## ▶️ Como Rodar
```bash
# Na pasta 01_etl_ans
mvn clean install
java -cp target/classes:target/dependency/* br.com.seu.Main