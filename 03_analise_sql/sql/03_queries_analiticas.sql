USE ans_analytics;

WITH limites_temporais AS (
    SELECT MIN(data_referencia) as inicio, MAX(data_referencia) as fim
    FROM demonstracoes_financeiras
),
     despesas_inicio AS (
         SELECT registro_ans, SUM(valor) as valor_inicial
         FROM demonstracoes_financeiras, limites_temporais
         WHERE data_referencia = limites_temporais.inicio
         GROUP BY registro_ans
     ),
     despesas_fim AS (
         SELECT registro_ans, SUM(valor) as valor_final
         FROM demonstracoes_financeiras, limites_temporais
         WHERE data_referencia = limites_temporais.fim
         GROUP BY registro_ans
     )
SELECT
    o.razao_social,
    i.valor_inicial,
    f.valor_final,
    ROUND(((f.valor_final - i.valor_inicial) / i.valor_inicial) * 100, 2) as crescimento_pct
FROM despesas_inicio i
         INNER JOIN despesas_fim f ON i.registro_ans = f.registro_ans
         INNER JOIN operadoras o ON i.registro_ans = o.registro_ans
WHERE i.valor_inicial > 0 -- Evitar divisão por zero
ORDER BY crescimento_pct DESC
    LIMIT 5;

SELECT
    o.uf,
    SUM(d.valor) as despesa_total_estado,
    COUNT(DISTINCT d.registro_ans) as qtd_operadoras,
    ROUND(AVG(d.valor), 2) as media_por_lancamento,
    ROUND(SUM(d.valor) / COUNT(DISTINCT d.registro_ans), 2) as media_por_operadora
FROM demonstracoes_financeiras d
         JOIN operadoras o ON d.registro_ans = o.registro_ans
WHERE o.uf != 'ND'
GROUP BY o.uf
ORDER BY despesa_total_estado DESC
    LIMIT 5;

WITH metricas_trimestrais AS (
    SELECT
        registro_ans,
        data_referencia,
        SUM(valor) as despesa_operadora,
        AVG(SUM(valor)) OVER(PARTITION BY data_referencia) as media_mercado_trimestre
    FROM demonstracoes_financeiras
    GROUP BY registro_ans, data_referencia
),
     performance_operadora AS (
         SELECT
             registro_ans,
             data_referencia,
             IF(despesa_operadora > media_mercado_trimestre, 1, 0) as acima_da_media
         FROM metricas_trimestrais
     )
SELECT
    COUNT(*) as qtd_operadoras_consistentes
FROM (
         SELECT registro_ans
         FROM performance_operadora
         GROUP BY registro_ans
         HAVING SUM(acima_da_media) >= 2
     ) as operadoras_top;