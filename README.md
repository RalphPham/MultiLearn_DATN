# EduStar Marketplace - Backend

Spring Boot backend cho he thong EduStar Marketplace (do an tot nghiep).

## Cong nghe

- Java 17+, Spring Boot 3.x, Spring Data JPA
- SQL Server
- MinIO (luu tru file)
- Gemini AI
- VNPay (sandbox)
- JWT auth
- Gmail SMTP

## Yeu cau truoc khi chay

1. **Java 17** tro len
2. **SQL Server** (co the dung Docker hoac SSMS)
3. **MinIO** (chay bang docker-compose hoac binary)
4. **Maven** (hoac dung mvnw)

## Cau hinh

Tat ca cau hinh nam o `src/main/resources/application.properties`. Cac gia tri co dang `YOUR_..._HERE` la cac chuoi placeholder, **ban can thay bang gia tri that** truoc khi chay:

| Bien                 | Y nghia                                     | Cach lay |
|----------------------|---------------------------------------------|----------|
| `DB_USERNAME` / `DB_PASSWORD` | Tai khoan SQL Server                | Tu cau hinh khi cai SQL Server |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Gmail + App Password            | https://myaccount.google.com/apppasswords |
| `GEMINI_API_KEY`     | Google Gemini AI key                        | https://aistudio.google.com/apikey |
| `JWT_SECRET`         | Chuoi base64 ngau nhien (>= 256 bit)         | `openssl rand -base64 64` |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | Tai khoan MinIO          | MinIO console |

Co the set qua bien moi truong (khuyen nghi) hoac sua truc tiep trong file. Vi du tren Windows PowerShell:

```powershell
$env:GEMINI_API_KEY = "AIza..."
$env:JWT_SECRET = "your-base64-secret"
.\mvnw spring-boot:run
```

## Khoi tao database

File backup nam o **branch `database`** cua repo nay. Checkout sang branch do de tai:

```bash
git checkout database
# se thay file EduMarketplace.bak + huong dan restore o README.md
```

Hoac vao GitHub → doi branch sang `database` → tai file `.bak`.

JPA chay che do `validate` → schema phai khop voi entity → **bat buoc restore tu file .bak** truoc khi chay backend.

## Khoi tao MinIO

### Buoc 1: Chay MinIO

`docker-compose.yml` da co san service `edustar-minio`. Chay:

```bash
docker-compose up -d
```

MinIO se chay tai:
- API S3: http://127.0.0.1:9000
- Console: http://127.0.0.1:9001 (login `minioadmin` / `minioadmin`)

Hoac neu khong dung Docker, tai MinIO binary tai https://min.io/download → chay:
```powershell
.\minio.exe server C:\minio\data --console-address ":9001"
```

### Buoc 2: Khoi phuc du lieu (video, anh khoa hoc)

Du lieu media (video bai giang, anh khoa hoc, certificate...) **khong di kem** trong repo do dung luong lon (~2.7 GB).

**Tai du lieu tai:** https://drive.google.com/drive/folders/1fvqwssXc0nU3w4H0LaLm2tvb23MYd2fc?usp=drive_link

Sau khi tai ve duoc file `minio-uploads.zip`:
- **Neu dung Docker**: giai nen `uploads/` vao thu muc `minio-data/` ben canh `docker-compose.yml`. Cau truc: `minio-data/uploads/<cac file>`
- **Neu dung MinIO binary**: giai nen vao `<minio-data-dir>/uploads/`

Restart MinIO sau khi copy file vao. Vao console http://127.0.0.1:9001 kiem tra bucket `uploads` co data chua.

### Buoc 3: Neu khong can du lieu mau

Neu chi can demo schema/code, bo qua Buoc 2 va tu tao bucket trong:
1. Vao http://127.0.0.1:9001 → **Buckets** → **Create Bucket** → ten `uploads` → **Create**
2. Backend van chay duoc, nhung khoa hoc cu se khong xem video.

## Chay backend

```bash
./mvnw spring-boot:run
```

Backend chay tai http://localhost:8080. Swagger UI: http://localhost:8080/swagger-ui.html

## Frontend

Phan frontend nam o **branch `frontend`** cua repo nay:

```bash
git checkout frontend
# Xem README.md trong branch do
```

## VNPay

Code dang dung **sandbox** test merchant cua tac gia. Neu muon dung tai khoan rieng, sua `vnp_TmnCode` va `vnp_HashSecret` trong `ConfigVNPay.java`.

## Luu y bao mat

- File `application.properties` da duoc don sach key that. Truoc khi push len repo cong khai luc nao, hay kiem tra lai bang `git diff` xem co lo key khong.
- File `.gitignore` da loai tru `.env`, `target/`, `uploads/`, `logs/`.
