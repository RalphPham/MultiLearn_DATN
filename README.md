# EduStar Marketplace - Backend

Backend Spring Boot cho hệ thống EduStar Marketplace (đồ án tốt nghiệp).

## Công nghệ

- Java 17+, Spring Boot 3.x, Spring Data JPA
- SQL Server
- MinIO (lưu trữ file)
- Gemini AI
- VNPay (sandbox)
- JWT auth
- Gmail SMTP

## Yêu cầu trước khi chạy

1. **Java 17** trở lên
2. **SQL Server** (có thể dùng Docker hoặc cài SSMS)
3. **MinIO** (chạy bằng docker-compose hoặc binary)
4. **Maven** (hoặc dùng `mvnw` đi kèm)

## Cấu hình

Tất cả cấu hình nằm ở `src/main/resources/application.properties`. Các giá trị có dạng `YOUR_..._HERE` là chuỗi placeholder — **bạn cần thay bằng giá trị thật** trước khi chạy:

| Biến                 | Ý nghĩa                                     | Cách lấy |
|----------------------|---------------------------------------------|----------|
| `DB_USERNAME` / `DB_PASSWORD` | Tài khoản SQL Server                | Tự cấu hình khi cài SQL Server |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Gmail + App Password            | https://myaccount.google.com/apppasswords |
| `GEMINI_API_KEY`     | Google Gemini AI key                        | https://aistudio.google.com/apikey |
| `JWT_SECRET`         | Chuỗi base64 ngẫu nhiên (≥ 256 bit)         | `openssl rand -base64 64` |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | Tài khoản MinIO          | MinIO console |

Có thể set qua biến môi trường (khuyến nghị) hoặc sửa trực tiếp trong file. Ví dụ trên Windows PowerShell:

```powershell
$env:GEMINI_API_KEY = "AIza..."
$env:JWT_SECRET = "your-base64-secret"
.\mvnw spring-boot:run
```

## Khởi tạo database

File backup nằm ở **branch `database`** của repo này. Checkout sang branch đó để tải:

```bash
git checkout database
# sẽ thấy file EduMarketplace.bak + hướng dẫn restore ở README.md
```

Hoặc vào GitHub → đổi branch sang `database` → tải file `.bak`.

JPA chạy chế độ `validate` → schema phải khớp với entity → **bắt buộc restore từ file .bak** trước khi chạy backend.

## Khởi tạo MinIO

### Bước 1: Chạy MinIO

`docker-compose.yml` đã có sẵn service `edustar-minio`. Chạy:

```bash
docker-compose up -d
```

MinIO sẽ chạy tại:
- API S3: http://127.0.0.1:9000
- Console: http://127.0.0.1:9001 (login `minioadmin` / `minioadmin`)

Hoặc nếu không dùng Docker, tải MinIO binary tại https://min.io/download → chạy:
```powershell
.\minio.exe server C:\minio\data --console-address ":9001"
```

### Bước 2: Khôi phục dữ liệu (video, ảnh khoá học)

Dữ liệu media (video bài giảng, ảnh khoá học, certificate...) **không đi kèm** trong repo do dung lượng lớn (~2.7 GB).

**Tải dữ liệu tại:** https://drive.google.com/drive/folders/1fvqwssXc0nU3w4H0LaLm2tvb23MYd2fc?usp=drive_link

Sau khi tải về được file `minio-uploads.zip`:
- **Nếu dùng Docker**: giải nén `uploads/` vào thư mục `minio-data/` bên cạnh `docker-compose.yml`. Cấu trúc: `minio-data/uploads/<các file>`
- **Nếu dùng MinIO binary**: giải nén vào `<minio-data-dir>/uploads/`

Restart MinIO sau khi copy file vào. Vào console http://127.0.0.1:9001 kiểm tra bucket `uploads` có data chưa.

### Bước 3: Nếu không cần dữ liệu mẫu

Nếu chỉ cần demo schema/code, bỏ qua Bước 2 và tự tạo bucket trống:
1. Vào http://127.0.0.1:9001 → **Buckets** → **Create Bucket** → tên `uploads` → **Create**
2. Backend vẫn chạy được, nhưng các khoá học cũ sẽ không xem được video.

## Chạy backend

```bash
./mvnw spring-boot:run
```

Backend chạy tại http://localhost:8080. Swagger UI: http://localhost:8080/swagger-ui.html

## Frontend

Phần frontend nằm ở **branch `frontend`** của repo này:

```bash
git checkout frontend
# Xem README.md trong branch đó
```

Phải chạy backend trước ở port 8080.

## VNPay

Code đang dùng **sandbox** test merchant của tác giả. Nếu muốn dùng tài khoản riêng, sửa `vnp_TmnCode` và `vnp_HashSecret` trong `ConfigVNPay.java`.

## Lưu ý bảo mật

- File `application.properties` đã được dọn sạch các key thật. Trước khi push lên repo công khai bất cứ lúc nào, hãy kiểm tra lại bằng `git diff` xem có lộ key không.
- File `.gitignore` đã loại trừ `.env`, `target/`, `uploads/`, `logs/`, `database/`, `images/`.
