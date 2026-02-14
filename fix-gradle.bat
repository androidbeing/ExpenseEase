@echo off
echo Fixing Gradle proxy issues...

REM Stop all Gradle daemons
gradlew --stop

REM Clear Gradle cache
rmdir /s /q "%USERPROFILE%\.gradle\caches" 2>nul

REM Set environment variables to disable proxy
set http_proxy=
set https_proxy=
set HTTP_PROXY=
set HTTPS_PROXY=
set no_proxy=*.google.com,*.maven.org,*.gradle.org,localhost,127.*

REM Run Gradle with clean dependencies
gradlew clean --refresh-dependencies

echo Done!
pause
