# EduStar Marketplace - Frontend

Vue 3 + Vite frontend cho hệ thống EduStar Marketplace.

## Yêu cầu

- Node.js 18+
- npm hoặc yarn

## Cấu hình

```bash
cp .env.example .env
```

Sửa file `.env`:

| Biến                  | Ý nghĩa                                       |
|-----------------------|-----------------------------------------------|
| `VITE_GOOGLE_CLIENT_ID` | Google OAuth Client ID (https://console.cloud.google.com/) |
| `VITE_API_BASE_URL`   | URL backend Spring Boot, mặc định `http://localhost:8080/api` |

## Cài đặt

```bash
npm install
```

## Chạy dev

```bash
npm run dev
```

Frontend chạy tại http://localhost:5173

## Build production

```bash
npm run build
```

## Backend

Backend Spring Boot nằm ở **branch `backend`** của repo này:

```bash
git checkout backend
# Xem README.md trong branch đó
```

Phải chạy backend trước ở port 8080.
