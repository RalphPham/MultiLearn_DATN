# Database Backup

File `EduMarketplace.bak` là full backup của database SQL Server `EduMarketplace`.

## Cách restore

### Cách 1: Dùng SSMS (dễ nhất)

1. Mở **SQL Server Management Studio (SSMS)**
2. Kết nối vào SQL Server của bạn (ví dụ `localhost` hoặc `.\SQLEXPRESS`)
3. Chuột phải vào **Databases** → **Restore Database...**
4. Chọn **Device** → bấm `...` → **Add** → trỏ tới file `EduMarketplace.bak`
5. Bấm **OK** → **OK**
6. Nếu báo lỗi "file in use" thì tick **Options** → **Close existing connections to destination database**

### Cách 2: Dùng sqlcmd (CLI)

```powershell
# Copy file .bak vào thư mục mà SQL Server có quyền đọc, ví dụ C:\Temp
Copy-Item .\EduMarketplace.bak C:\Temp\

# Restore
sqlcmd -S localhost -U sa -P <password> -C -Q "RESTORE DATABASE [EduMarketplace] FROM DISK = N'C:\Temp\EduMarketplace.bak' WITH REPLACE"
```

Nếu lỗi `logical file name`, chạy trước lệnh này để xem tên file logical:
```sql
RESTORE FILELISTONLY FROM DISK = N'C:\Temp\EduMarketplace.bak'
```

## Sau khi restore

Kiểm tra:
```sql
USE EduMarketplace;
SELECT TOP 10 name FROM sys.tables;
```

## Thông tin DB

- **SQL Server version**: SQL Server 2022 (Express)
- **Backup taken**: 2026-05-05
- **Size**: ~15 MB
