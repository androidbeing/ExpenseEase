@echo off
echo ============================================
echo ExpenseEase Build Script
echo ============================================
echo.

echo Step 1: Stopping Gradle Daemons...
call gradlew --stop
echo.

echo Step 2: Clearing environment variables...
set http_proxy=
set https_proxy=
set HTTP_PROXY=
set HTTPS_PROXY=
set no_proxy=*.google.com,*.maven.org,*.gradle.org,localhost,127.*
echo.

echo Step 3: Building project...
call gradlew clean build --refresh-dependencies
echo.

echo ============================================
echo Build Complete!
echo ============================================
echo.
echo Next steps:
echo 1. Open Android Studio
echo 2. Sync Project with Gradle Files
echo 3. Run the app
echo 4. Test the auto-sync feature
echo.
pause

