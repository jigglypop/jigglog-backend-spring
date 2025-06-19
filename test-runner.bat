@echo off
echo ===============================================
echo    Jigglog 백엔드 테스트 실행 시작
echo ===============================================
echo.

echo [1/3] 클린 빌드 시작...
gradlew.bat clean

echo.
echo [2/3] 컴파일 시작...
gradlew.bat compileKotlin compileTestKotlin

echo.
echo [3/3] 테스트 실행 시작...
echo 시작 시간: %time%

gradlew.bat test --continue --info

echo.
echo 종료 시간: %time%
echo.

echo ===============================================
echo    테스트 결과 요약
echo ===============================================

if exist build\reports\tests\test\index.html (
    echo ✅ HTML 테스트 리포트 생성됨: build\reports\tests\test\index.html
    start build\reports\tests\test\index.html
) else (
    echo ❌ HTML 테스트 리포트 생성 실패
)

if exist build\test-results\test (
    echo ✅ XML 테스트 결과 생성됨: build\test-results\test\
    dir build\test-results\test\*.xml
) else (
    echo ❌ XML 테스트 결과 생성 실패
)

echo.
echo 테스트 실행 완료!
pause 