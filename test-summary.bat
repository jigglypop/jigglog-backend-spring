@echo off
echo ===============================================
echo        빠른 테스트 실행 (요약만)
echo ===============================================

gradlew.bat test --continue

echo.
echo ===============================================
echo              테스트 결과 요약
echo ===============================================

if exist build\reports\tests\test\index.html (
    echo ✅ 테스트 리포트: build\reports\tests\test\index.html
    echo 브라우저에서 열려면 아무 키나 누르세요...
    pause > nul
    start build\reports\tests\test\index.html
) else (
    echo ❌ 테스트 리포트 생성 실패
) 