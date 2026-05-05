# Database Backup

File `EduMarketplace.bak` la full backup cua database SQL Server `EduMarketplace`.

## Cach restore

### Cach 1: Dung SSMS (de nhat)

1. Mo **SQL Server Management Studio (SSMS)**
2. Ket noi vao SQL Server cua ban (vi du `localhost` hoac `.\SQLEXPRESS`)
3. Chuot phai vao **Databases** → **Restore Database...**
4. Chon **Device** → bam `...` → **Add** → tro toi file `EduMarketplace.bak`
5. Bam **OK** → **OK**
6. Neu bao loi "file in use" thi tick **Options** → **Close existing connections to destination database**

### Cach 2: Dung sqlcmd (CLI)

```powershell
# Copy file .bak vao thu muc ma SQL Server co quyen doc, vi du C:\Temp
Copy-Item .\EduMarketplace.bak C:\Temp\

# Restore
sqlcmd -S localhost -U sa -P <password> -C -Q "RESTORE DATABASE [EduMarketplace] FROM DISK = N'C:\Temp\EduMarketplace.bak' WITH REPLACE"
```

Neu loi `logical file name`, chay truoc lenh nay de xem ten file logical:
```sql
RESTORE FILELISTONLY FROM DISK = N'C:\Temp\EduMarketplace.bak'
```

## Sau khi restore

Kiem tra:
```sql
USE EduMarketplace;
SELECT TOP 10 name FROM sys.tables;
```

## Thong tin DB

- **SQL Server version**: SQL Server 2022 (Express)
- **Backup taken**: 2026-05-05
- **Size**: ~15 MB
