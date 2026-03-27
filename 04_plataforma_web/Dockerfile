# Usa Python 3.10
FROM python:3.10-slim

# Define o diretório
WORKDIR /app

# Copia apenas o arquivo de dependências
COPY requirements.txt .

# Instala as dependências
RUN pip install --no-cache-dir -r requirements.txt

# Copia o resto do código
COPY . .

# Rodar na porta 8000
EXPOSE 8000

# O comando que vai rodar quando iniciar
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]