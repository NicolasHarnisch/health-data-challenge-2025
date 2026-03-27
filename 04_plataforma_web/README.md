# Módulo 4 - Plataforma Web & API Analytics (Enterprise Dashboard)

> Local: `./04_plataforma_web`
> 
> Tecnologia: Python 3.10 (FastAPI), Vue.js 3 (Composition API), Chart.js, CSS Grid/Flexbox, Docker.

## Visão Geral

Este módulo fornece a camada de apresentação (Dashboard SPA) e API analítica para o projeto. Inclui:

- Backend FastAPI assíncrono com filtros SQL (WHERE / ORDER BY / LIMIT)
- Frontend Vue.js 3 (No-Build via CDN) com dashboard interativo
- Visualização de métricas financeiras do setor de saúde suplementar
- Design customizado em SaaS UI e UX de produto B2B

## Arquitetura

### Backend

- FastAPI (HTTP API assíncrona)
- Conexão ao MySQL do módulo 03
- Auto-documentação via Swagger em `/docs`
- Paginação e filtros para não onerar memória

### Frontend

- Vue 3 Composition API (sem compilação local)
- Chart.js para gráficos interativos
- Drill-down de dados (clique em barra Estado => tabela filtrada)
- Debounce em busca texto (500ms)
- Skeleton loaders ao invés de spinner

### Infraestrutura

- Docker container leve (Alpine/Slim)
- Paridade dev/prod com cruzamento de dados via host.docker.internal

## Funcionalidades-chave

- Gráficos de despesa por estado e modalidade
- Tabela de operadoras com filtro por CNPJ/Razão Social
- Debounced search para diminuir carga de requisições
- Skeleton animation para melhor percepção de carregamento

## Setup e execução (Docker)

### Pré-requisitos

- Docker / Docker Desktop
- Banco MySQL populado (`ans_analytics`) conforme módulo 03

### Passos

```bash
cd 04_plataforma_web

docker build -t ans-api .

docker run -d -p 8000:8000 --name backend-ans \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=3306 \
  -e DB_USER=root \
  -e DB_PASSWORD=1234 \
  -e DB_NAME=ans_analytics \
  ans-api
```

> Ajuste `DB_USER` e `DB_PASSWORD` conforme sua instalação local.

### Acessar

- API: `http://localhost:8000`
- Swagger: `http://localhost:8000/docs`
- Frontend: abra `index.html` no navegador (duplo clique ou Live Server)

## Estrutura do projeto

- `main.py`: entrada do FastAPI
- `app.js`: lógica frontend Vue + chamadas API
- `index.html`: interface SPA
- `style.css`: estilos customizados

## Observações

- Módulo 4 consome dados do módulo 03; garanta importação SQL pronta.
- Teste os endpoints no Swagger antes de abrir o frontend.

## Contato

- Repositório do desafio: `health-data-challenge-2025`
- Suporte: implemente melhorias e abra issue/PR conforme convém.
