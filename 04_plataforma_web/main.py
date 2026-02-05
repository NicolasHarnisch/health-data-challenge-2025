import os
import logging
from typing import Optional, List, Dict

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
import mysql.connector

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("ans_api")

app = FastAPI(title="API Operadoras ANS", version="1.0.0")

# Configurações do Banco de Dados
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = int(os.getenv("DB_PORT", "3306"))
DB_USER = os.getenv("DB_USER", "root")
DB_PASSWORD = os.getenv("DB_PASSWORD", "1234")
DB_NAME = os.getenv("DB_NAME", "ans_analytics")
ALLOW_ORIGINS = ["*"]

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOW_ORIGINS,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["*"],
)


def get_db_connection():
    return mysql.connector.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        autocommit=True
    )


MAX_LIMIT = 100


@app.get("/api/operadoras")
def list_operadoras(
        page: int = Query(1, gt=0),
        limit: int = Query(10, gt=0, le=MAX_LIMIT),
        search: Optional[str] = None
):
    offset = (page - 1) * limit
    conn = None
    cursor = None

    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)

        base_query = "SELECT registro_ans, cnpj, razao_social, uf, modalidade FROM operadoras"
        count_query = "SELECT COUNT(*) as total FROM operadoras"
        data_params: List = []
        count_params: List = []

        if search:
            like = f"%{search}%"
            filter_clause = " WHERE razao_social LIKE %s OR cnpj LIKE %s"
            base_query += filter_clause
            count_query += filter_clause
            data_params.extend([like, like])
            count_params.extend([like, like])

        # parâmetros para paginação
        base_query += " LIMIT %s OFFSET %s"
        data_params.extend([limit, offset])

        # Conta total com parâmetros adequados
        cursor.execute(count_query, tuple(count_params))
        total_row = cursor.fetchone()
        total = total_row["total"] if total_row else 0

        cursor.execute(base_query, tuple(data_params))
        rows = cursor.fetchall()

        pages = (total // limit) + (1 if total % limit > 0 else 0)

        return {
            "data": rows,
            "meta": {"total": total, "page": page, "limit": limit, "pages": pages}
        }

    except mysql.connector.Error as e:
        logger.exception("Erro ao consultar operadoras")
        raise HTTPException(status_code=500, detail="Erro interno ao acessar o banco de dados")
    finally:
        if cursor:
            try:
                cursor.close()
            except Exception:
                pass
        if conn:
            try:
                conn.close()
            except Exception:
                pass


@app.get("/api/operadoras/{registro_ans}/despesas")
def get_operadora_despesas(registro_ans: str, limit: int = Query(200, gt=0, le=1000)):
    conn = None
    cursor = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)

        query = """
                SELECT id, descricao_conta, valor, data_referencia, ano, trimestre
                FROM demonstracoes_financeiras
                WHERE registro_ans = %s
                ORDER BY data_referencia DESC
                    LIMIT %s \
                """
        cursor.execute(query, (registro_ans, limit))
        rows = cursor.fetchall()
        return rows
    except mysql.connector.Error:
        logger.exception("Erro ao buscar despesas da operadora %s", registro_ans)
        raise HTTPException(status_code=500, detail="Erro interno ao buscar despesas")
    finally:
        if cursor:
            try:
                cursor.close()
            except Exception:
                pass
        if conn:
            try:
                conn.close()
            except Exception:
                pass


@app.get("/api/estatisticas")
def get_estatisticas(top_n: int = Query(10, gt=0, le=50)):
    conn = None
    cursor = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)

        cursor.execute("SELECT SUM(valor) as total_geral FROM demonstracoes_financeiras")
        total_row = cursor.fetchone()
        total = total_row["total_geral"] if total_row and total_row["total_geral"] is not None else 0

        cursor.execute(
            """
            SELECT o.uf, SUM(d.valor) as total
            FROM demonstracoes_financeiras d
                     JOIN operadoras o ON d.registro_ans = o.registro_ans
            WHERE o.uf != 'ND'
            GROUP BY o.uf
            ORDER BY total DESC
                LIMIT %s
            """,
            (top_n,)
        )
        top_ufs = cursor.fetchall()

        return {"total_despesas": total, "distribuicao_uf": top_ufs}
    except mysql.connector.Error:
        logger.exception("Erro ao calcular estatísticas")
        raise HTTPException(status_code=500, detail="Erro interno ao calcular estatísticas")
    finally:
        if cursor:
            try:
                cursor.close()
            except Exception:
                pass
        if conn:
            try:
                conn.close()
            except Exception:
                pass


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=int(os.getenv("PORT", 8000)), reload=True)