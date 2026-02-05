# 🗄️ Módulo 3: Data Warehousing (SQL)

Este módulo estrutura o banco de dados analítico e orquestra a carga massiva de dados (Big Data).

## 🛠️ Tecnologias
* **Banco:** MySQL 8.0
* **Engine:** InnoDB (Tabelas Relacionais)
* **Otimização:** Bulk Insert

## 🚀 Destaques Técnicos
* **Modelagem:** Star Schema simplificado (Fatos e Dimensões).
* **Performance:** Uso de `LOAD DATA LOCAL INFILE` para inserir **1.2 milhões de linhas em segundos**.
* **Tratamento On-the-fly:** Scripts SQL que limpam formatação de moeda (`R$`) e datas durante a ingestão.

## ⚙️ Ordem de Execução (SQL)
Execute os scripts na pasta `/sql` estritamente nesta ordem:

1.  `01_ddl_create_tables.sql` (Cria a estrutura)
2.  `02_import_data.sql` (Importa os CSVs processados)
3.  `03_queries_analiticas.sql` (Gera os relatórios de validação)