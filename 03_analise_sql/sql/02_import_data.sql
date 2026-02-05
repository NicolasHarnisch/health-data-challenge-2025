USE ans_analytics;

-- 1. Importar Cadastro (Caminho absoluto corrigido)
LOAD DATA LOCAL INFILE 'C:/Users/nicol/Documentos/Arquivos - Estudo/Projetos/health-data-challenge-2025/data/temp/cadastro_operadoras.csv'
    INTO TABLE operadoras
    CHARACTER SET utf8mb4
    FIELDS TERMINATED BY ';'
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (registro_ans, cnpj, razao_social, modalidade, uf);

-- 2. Importar Despesas Consolidadas
LOAD DATA LOCAL INFILE 'C:/Users/nicol/Documentos/Arquivos - Estudo/Projetos/health-data-challenge-2025/data/processed/consolidado_despesas.csv'
    INTO TABLE demonstracoes_financeiras
    CHARACTER SET utf8mb4
    FIELDS TERMINATED BY ';'
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (registro_ans, @data_ref, @dummy, descricao_conta, @valor_br)
    SET
        data_referencia = STR_TO_DATE(@data_ref, '%d/%m/%Y'),
        valor = CAST(REPLACE(REPLACE(REPLACE(@valor_br, 'R$', ''), '.', ''), ',', '.') AS DECIMAL(18,2)),
        conta_contabil = 'DESPESA_GERAL';

-- 3. Importar Agregadas
LOAD DATA LOCAL INFILE 'C:/Users/nicol/Documentos/Arquivos - Estudo/Projetos/health-data-challenge-2025/data/processed/despesas_agregadas.csv'
    INTO TABLE despesas_agregadas
    CHARACTER SET utf8mb4
    FIELDS TERMINATED BY ';'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES;