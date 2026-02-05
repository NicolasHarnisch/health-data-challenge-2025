CREATE DATABASE IF NOT EXISTS ans_analytics;
USE ans_analytics;

CREATE TABLE operadoras (
                            registro_ans VARCHAR(10) NOT NULL,
                            cnpj VARCHAR(20),
                            razao_social VARCHAR(255),
                            modalidade VARCHAR(100),
                            uf CHAR(2),
                            PRIMARY KEY (registro_ans)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE demonstracoes_financeiras (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           registro_ans VARCHAR(10) NOT NULL,
                                           data_referencia DATE NOT NULL,
                                           ano INT GENERATED ALWAYS AS (YEAR(data_referencia)) STORED,
    trimestre INT GENERATED ALWAYS AS (QUARTER(data_referencia)) STORED,
    conta_contabil VARCHAR(50),
    descricao_conta VARCHAR(255),
    valor DECIMAL(18,2),

    INDEX idx_data (data_referencia),
    INDEX idx_ans (registro_ans),

    FOREIGN KEY (registro_ans) REFERENCES operadoras(registro_ans)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE despesas_agregadas (
                                    razao_social VARCHAR(255),
                                    uf CHAR(2),
                                    modalidade VARCHAR(100),
                                    valor_total DECIMAL(18,2),
                                    media_trimestral DECIMAL(18,2),
                                    desvio_padrao DECIMAL(18,2),
                                    cnpj_valido VARCHAR(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;