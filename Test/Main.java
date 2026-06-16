powershell -NoProfile -ExecutionPolicy Bypass -Command "Unblock-File -LiteralPath 'C:\db_check\db-relationship-extractor2_handoff.ps1'"


powershell -NoProfile -ExecutionPolicy Bypass -Command "& 'C:\db_check\db-relationship-extractor2_handoff.ps1' -OutputDir 'C:\db_check\db-relationship-extractor2' -Force"
powershell -NoProfile -ExecutionPolicy Bypass -Command "& 'C:\db_check\db-relationship-extractor2_handoff.ps1' -OutputDir 'C:\db_check\db-relationship-extractor2' -VerifyOnly"
