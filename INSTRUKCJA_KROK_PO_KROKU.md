# Instrukcja Ręcznego Podziału Aplikacji (Krok po Kroku)

Ta instrukcja zastępuje skrypt `podziel_i_pushuj.bat` i pozwala na ręczne wykonanie tych samych operacji. Jest to metoda bardziej niezawodna, ponieważ unikamy problemów z interpretacją skryptów przez terminal.

**WAŻNE:** Wykonaj poniższe kroki w terminalu **PowerShell**, otwartym w głównym katalogu projektu.

## Krok 0: Przygotowanie

### 1. Usuń stary katalog `.git`
Jeśli istnieje w Twoim projekcie katalog o nazwie `.git` (jest on ukryty), usuń go. To kluczowe, aby zacząć z czystą historią. Możesz to zrobić komendą w terminalu:

```powershell
if (Test-Path .git) { Remove-Item .git -Recurse -Force }
```

### 2. Przygotuj URL do repozytorium GitHub
Skopiuj adres URL do Twojego **nowego, pustego** repozytorium na GitHubie. Będzie potrzebny na końcu.

## Krok 1: Commit Backendowy (jako Ty)

Skopiuj i wklej do terminala **cały poniższy blok komend na raz** i naciśnij Enter.

```powershell
# Inicjalizacja repozytorium
git init
git branch -M main

# Ustawianie Twojej tożsamości
git config user.name "Michał Lewczyński Backend"
git config user.email "michalos4321@gmail.com"

# Dodawanie plików backendu
git add .gitignore
git add pom.xml
git add INSTRUKCJA.md
git add WSPOLPRACA.md
git add INSTRUKCJA_PODZIALU.md
git add podziel_i_pushuj.bat
git add INSTRUKCJA_KROK_PO_KROKU.md
git add "src/main/java"
git add "src/main/resources/application.properties"

# Tworzenie pierwszego commita
git commit -m "feat(backend): Inicjalizacja projektu, konfiguracja i logika biznesowa"

Write-Host "Commit backendowy został stworzony." -ForegroundColor Green
```

## Krok 2: Commit Frontendowy (jako Twój kolega)

Teraz skopiuj i wklej do terminala **cały drugi blok komend na raz** i naciśnij Enter.

```powershell
# Zmiana tożsamości na Twojego kolegę
git config user.name "Paweł Hernik Frontend"
git config user.email "hernik.pawel@o2.pl"

# Dodawanie plików frontendu
git add "src/main/resources/static"
git add "src/main/resources/templates"

# Tworzenie drugiego commita
git commit -m "feat(frontend): Implementacja interfejsu uzytkownika i widokow"

Write-Host "Commit frontendowy został stworzony." -ForegroundColor Green
```

## Krok 3: Publikacja na GitHubie

Teraz połączymy lokalne repozytorium ze zdalnym i wyślemy na nie przygotowane commity.

**Zastąp `<URL_REPOZYTORIUM>` adresem, który skopiowałeś w kroku 0.**

```powershell
git remote add origin <URL_REPOZYTORIUM>
git push -u origin main
```

Po wykonaniu tej komendy, Twoja podzielona historia projektu powinna być już widoczna na GitHubie.

## Krok 4: Czyszczenie

Na koniec wyczyścimy lokalną konfigurację autora, aby nie została na stałe w Twoim projekcie.

```powershell
git config --unset user.name
git config --unset user.email
```

To wszystko! Ta metoda powinna zadziałać bezbłędnie.