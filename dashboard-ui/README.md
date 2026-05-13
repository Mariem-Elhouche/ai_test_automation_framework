# Dashboard UI (React)

Frontend React pour visualiser les endpoints Dashboard exposes par `ai-engine`.

## Prerequis

- Node.js 20+
- API FastAPI active sur `http://127.0.0.1:8080`

## Lancer en local

```powershell
cd dashboard-ui
npm install
npm run dev
```

Puis ouvrir: `http://127.0.0.1:5173`

## Utilisation

- `API URL`: base URL de l'API FastAPI
- Login: email/password definis dans `ai-engine/.env`
- `Run ID`: filtre optionnel

Le frontend:

- appelle `POST /auth/login`
- stocke le token JWT
- interroge `/api/dashboard` avec `Authorization: Bearer <token>`
- rafraichit automatiquement toutes les 20 secondes
