@echo off
SETLOCAL

REM ============================================================================
REM Skrypt do podzialu gotowej aplikacji na commity backend/frontend
REM i wyslania ich na nowe repozytorium GitHub.
REM ============================================================================

REM --- KONFIGURACJA ---
REM Zmien ponizsze dane na Wasze.
SET BACKEND_USER_NAME="Michał Lewczyński Backend"
SET BACKEND_USER_EMAIL="michalos4321@gmail.com"
SET FRONTEND_USER_NAME="Paweł Hernik Frontend"
SET FRONTEND_USER_EMAIL="hernik.pawel@o2.pl"
REM --------------------

SET GITHUB_REPO_URL=%1

IF "%GITHUB_REPO_URL%"=="" (
    echo.
    echo Blad: Nie podano adresu URL repozytorium GitHub.
    echo.
    echo Uzycie:
    echo   .\podziel_i_pushuj.bat https://github.com/krokieto-baggieto/ZwinneProjekt.git
    echo.
    GOTO :EOF
)

REM Sprawdzenie, czy istnieje juz katalog .git
IF EXIST ".git" (
    echo.
    echo Wykryto istniejace repozytorium Git (.git).
    echo Aby uniknac konfliktow, ten skrypt powinien byc uruchomiony w "czystym" katalogu.
    echo Usun katalog .git i sprobuj ponownie.
    echo.
    GOTO :EOF
)

echo Inicjalizacja nowego repozytorium Git...
git init
git branch -M main

echo.
echo ============================================================================
echo KROK 1: Tworzenie commita dla BACKENDU
echo ============================================================================
echo Ustawianie autora na: %BACKEND_USER_NAME%
git config user.name %BACKEND_USER_NAME%
git config user.email %BACKEND_USER_EMAIL%

echo Dodawanie plikow backendu i konfiguracji do poczekalni...
git add ".gitignore"
git add "pom.xml"
git add "INSTRUKCJA.md"
git add "WSPOLPRACA.md"
git add "podziel_i_pushuj.bat"
git add "src/main/java"
git add "src/main/resources/application.properties"

echo Tworzenie commita...
git commit -m "feat(backend): Inicjalizacja projektu, konfiguracja i logika biznesowa"

echo.
echo ============================================================================
echo KROK 2: Tworzenie commita dla FRONTENDU
echo ============================================================================
echo Ustawianie autora na: %FRONTEND_USER_NAME%
git config user.name %FRONTEND_USER_NAME%
git config user.email %FRONTEND_USER_EMAIL%

echo Dodawanie plikow frontendu do poczekalni...
git add "src/main/resources/static"
git add "src/main/resources/templates"

echo Tworzenie commita...
git commit -m "feat(frontend): Implementacja interfejsu uzytkownika i widokow"

echo.
echo ============================================================================
echo KROK 3: Wysylanie na GitHub
echo ============================================================================
echo Laczenie ze zdalnym repozytorium: %GITHUB_REPO_URL%
git remote add origin %GITHUB_REPO_URL%

echo Wysylanie dwoch commitow na galaz 'main'...
git push -u origin main

echo.
echo Gotowe!
echo Historia projektu zostala podzielona na dwa commity i wyslana na GitHub.
echo Sprawdz swoje repozytorium online.
echo.

REM Czyszczenie lokalnej konfiguracji autora
git config --unset user.name
git config --unset user.email

ENDLOCAL