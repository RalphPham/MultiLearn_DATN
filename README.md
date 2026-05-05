# EduStar Marketplace - Frontend

Vue 3 + Vite frontend cho he thong EduStar Marketplace.

## Yeu cau

- Node.js 18+
- npm hoac yarn

## Cau hinh

```bash
cd frontend
cp .env.example .env
```

Sua file `.env`:

| Bien                  | Y nghia                                       |
|-----------------------|-----------------------------------------------|
| `VITE_GOOGLE_CLIENT_ID` | Google OAuth Client ID (https://console.cloud.google.com/) |
| `VITE_API_BASE_URL`   | URL backend Spring Boot, mac dinh `http://localhost:8080/api` |

## Cai dat

```bash
cd frontend
npm install
```

## Chay dev

```bash
npm run dev
```

Frontend chay tai http://localhost:5173

## Build production

```bash
npm run build
```

## Backend

Backend Spring Boot nam o **branch `backend`** cua repo nay:

```bash
git checkout backend
# Xem README.md trong branch do
```

Phai chay backend truoc o port 8080.
