-- T-16 update: add video timestamp for each note
-- Run this script if table notes already exists but no timestamp column yet.

IF COL_LENGTH('notes', 'timestamp_seconds') IS NULL
BEGIN
    ALTER TABLE notes
        ADD timestamp_seconds INT NULL;
END
