-- Add permissions column for granular admin authorization
-- Run this script before starting Spring Boot (ddl-auto=validate)

IF COL_LENGTH('admins', 'permissions') IS NULL
BEGIN
    ALTER TABLE admins
        ADD permissions NVARCHAR(4000) NULL;
END
GO

UPDATE admins
SET permissions = 'DASHBOARD_VIEW,STATS_VIEW,COURSE_VIEW,COURSE_APPROVE,COURSE_REJECT,COURSE_BULK_APPROVAL'
WHERE admin_role = 'STAFF'
  AND (permissions IS NULL OR LTRIM(RTRIM(permissions)) = '');
GO
