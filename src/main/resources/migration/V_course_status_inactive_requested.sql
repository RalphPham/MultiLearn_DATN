-- T-xx: Dong bo CHECK constraint trang thai khoa hoc
-- Muc tieu: cho phep trang thai INACTIVE_REQUESTED (yeu cau tam dung ban)
-- Chay script nay 1 lan tren SQL Server (an toan, co the chay lai)

SET NOCOUNT ON;

DECLARE @currentDefinition NVARCHAR(MAX);
SELECT @currentDefinition = cc.definition
FROM sys.check_constraints cc
WHERE cc.name = 'CK_Course_Status'
  AND OBJECT_NAME(cc.parent_object_id) = 'courses';

IF @currentDefinition IS NULL
BEGIN
    ALTER TABLE dbo.courses
    ADD CONSTRAINT CK_Course_Status
    CHECK ([status] IN (
        'BLOCKED',
        'INACTIVE_REQUESTED',
        'INACTIVE',
        'REJECTED',
        'PUBLISHED',
        'PENDING_APPROVAL',
        'PENDING',
        'DRAFT',
        'ARCHIVED'
    ));
END
ELSE IF @currentDefinition NOT LIKE '%INACTIVE_REQUESTED%'
BEGIN
    ALTER TABLE dbo.courses DROP CONSTRAINT CK_Course_Status;

    ALTER TABLE dbo.courses
    ADD CONSTRAINT CK_Course_Status
    CHECK ([status] IN (
        'BLOCKED',
        'INACTIVE_REQUESTED',
        'INACTIVE',
        'REJECTED',
        'PUBLISHED',
        'PENDING_APPROVAL',
        'PENDING',
        'DRAFT',
        'ARCHIVED'
    ));
END;
