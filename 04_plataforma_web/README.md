# 🌐 Módulo 4: Plataforma Web & API Analytics

Dashboard interativo para visualização de dados financeiros da saúde suplementar.

## 🛠️ Tecnologias
* **Backend:** Python + FastAPI (Async)
* **Frontend:** Vue.js 3 + Chart.js (CDN)
* **Estilo:** Bootstrap 5

## 🚀 Funcionalidades
* **Dashboard:** Gráficos de barras em tempo real.
* **API REST:** Endpoints documentados (`/docs`).
* **Performance:** Renderiza R$ 47 Trilhões sem travamentos.

## ▶️ Como Rodar
```bash
# Na pasta 04_plataforma_web
pip install -r requirements.txt # (ou instale fastapi uvicorn mysql-connector-python)
python -m uvicorn main:app --reload