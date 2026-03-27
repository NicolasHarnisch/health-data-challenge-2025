import os
import logging
from typing import Optional

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
import mysql.connector

# Configuração de logs para ver o que acontece no Render
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("ans_api")

app = FastAPI(title="API Operadoras ANS", version="1.0.0")

# Configurações do Banco de Dados (Puxando das variáveis do Render)
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = int(os.getenv("DB_PORT", "3306"))
DB_USER = os.getenv("DB_USER", "root")
DB_PASSWORD = os.getenv("DB_PASSWORD", "1234")
DB_NAME = os.getenv("DB_NAME", "ans_analytics")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Permite que seu frontend acesse a API de qualquer lugar
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

def get_db_connection():
    try:
        # Adicionado ssl_disabled=False para garantir conexão segura com Aiven
        return mysql.connector.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            database=DB_NAME,
            autocommit=True,
            ssl_disabled=False 
        )
    except mysql.connector.Error as err:
        logger.error(f"Erro ao conectar no MySQL: {err}")
        raise

# --- ROTAS ---

@app.get("/")
def home():
    return {
        "status": "online",
        "message": "API Operadoras ANS - Use /docs para ver a documentação",
        "endpoints": ["/api/operadoras", "/api/estatisticas"]
    }

@app.get("/api/operadoras")
def list_operadoras(
        page: int = Query(1, gt=0),
        limit: int = Query(10, gt=0, le=100),
        search: Optional[str] = None,
        uf: Optional[str] = None,
        sort: Optional[str] = "nome"
):
    offset = (page - 1) * limit
    conn = None
    cursor = None

    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)

        base_query = "SELECT registro_ans, cnpj, razao_social, uf, modalidade FROM operadoras WHERE 1=1"
        count_query = "SELECT COUNT(*) as total FROM operadoras WHERE 1=1"
        params = []

        if search:
            like = f"%{search}%"
            clause = " AND (razao_social LIKE %s OR cnpj LIKE %s)"
            base_query += clause
            count_query += clause
            params.extend([like, like])

        if uf:
            clause = " AND uf = %s"
            base_query += clause
            count_query += clause
            params.append(uf)

        # Ordenação
        sort_options = {
            "nome": "razao_social ASC",
            "uf": "uf ASC, razao_social ASC",
            "registro": "registro_ans ASC"
        }
        order_by = sort_options.get(sort, "razao_social ASC")
        base_query += f" ORDER BY {order_by}"

        cursor.execute(count_query, tuple(params))
        total = cursor.fetchone()["total"]

        base_query += " LIMIT %s OFFSET %s"
        params.extend([limit, offset])

        cursor.execute(base_query, tuple(params))
        rows = cursor.fetchall()

        pages = (total // limit) + (1 if total % limit > 0 else 0)

        return {
            "data": rows,
            "meta": {"total": total, "page": page, "limit": limit, "pages": pages}
        }

    except Exception as e:
        logger.exception("Erro na consulta de operadoras")
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if cursor: cursor.close()
        if conn: conn.close()

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
                LIMIT %s
                """
        cursor.execute(query, (registro_ans, limit))
        return cursor.fetchall()
    except Exception as e:
        logger.exception("Erro ao buscar despesas")
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if cursor: cursor.close()
        if conn: conn.close()

@app.get("/api/estatisticas")
def get_estatisticas(top_n: int = Query(10, gt=0, le=50)):
    conn = None
    cursor = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)

        cursor.execute("SELECT SUM(valor) as total_geral FROM demonstracoes_financeiras")
        total = cursor.fetchone()["total_geral"] or 0

        cursor.execute("""
            SELECT o.uf, SUM(d.valor) as total
            FROM demonstracoes_financeiras d
            JOIN operadoras o ON d.registro_ans = o.registro_ans
            WHERE o.uf != 'ND'
            GROUP BY o.uf
            ORDER BY total DESC
            LIMIT %s
        """, (top_n,))
        top_ufs = cursor.fetchall()

        return {"total_despesas": total, "distribuicao_uf": top_ufs}
    except Exception as e:
        logger.exception("Erro nas estatísticas")
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if cursor: cursor.close()
        if conn: conn.close()

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", 8000))
    uvicorn.run("main:app", host="0.0.0.0", port=port)