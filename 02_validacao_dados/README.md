# 🧪 Módulo 2: Validação e Enriquecimento

Este módulo atua como Quality Assurance (QA). Ele consome os dados brutos, valida documentos e gera métricas estatísticas.

## 🛠️ Tecnologias
* **Linguagem:** Java 21
* **Algoritmos:** Módulo 11 (Matemática de validação de CNPJ)

## 🧠 Lógica de Negócio
1.  **Sanitização:** Remove caracteres especiais de CNPJs.
2.  **Validação Matemática:** Verifica dígitos verificadores de cada CNPJ das operadoras.
3.  **Flagging:** Não exclui registros inválidos, apenas marca como `flag_valido=false` para auditoria.
4.  **Estatística:** Calcula Média e Desvio Padrão das despesas por operadora.

## ▶️ Como Rodar
```bash
# Na pasta 02_validacao_dados
mvn clean install
java -cp target/classes:target/dependency/* br.com.seu.Main