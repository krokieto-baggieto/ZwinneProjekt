# Instrukcja Podziału Aplikacji na Commity

Ten przewodnik wyjaśnia, jak użyć skryptu `podziel_i_pushuj.bat`, aby "pokroić" gotową aplikację na dwa osobne commity (backend i frontend) i wysłać je na nowe repozytorium GitHub.

**WAŻNE:** Ten proces jest przeznaczony do jednorazowego użytku na **nowym, pustym repozytorium GitHub**. Nie uruchamiaj go na repozytorium, które już zawiera jakąś historię.

## Krok 1: Przygotowanie

### 1. Utwórz nowe repozytorium na GitHubie
1.  Zaloguj się na swoje konto na [GitHubie](https://github.com).
2.  Stwórz **nowe, puste repozytorium**. Nie dodawaj do niego plików `README`, `.gitignore` ani licencji.
3.  Po utworzeniu repozytorium, skopiuj jego adres URL. Będzie on wyglądał mniej więcej tak: `https://github.com/twoja-nazwa/twoje-repo.git`.

### 2. Skonfiguruj skrypt
1.  Otwórz plik [`podziel_i_pushuj.bat`](podziel_i_pushuj.bat) w edytorze tekstu.
2.  W sekcji `--- KONFIGURACJA ---` zmień dane na Wasze prawdziwe imiona i adresy e-mail, których używacie na GitHubie. Jest to kluczowe, aby commity zostały poprawnie przypisane do Waszych profili.

    ```batch
    SET BACKEND_USER_NAME="Twoje Imie i Nazwisko"
    SET BACKEND_USER_EMAIL="twoj-email@example.com"
    SET FRONTEND_USER_NAME="Imie i Nazwisko Kolegi"
    SET FRONTEND_USER_EMAIL="email-kolegi@example.com"
    ```

### 3. Usuń istniejącą historię Git (jeśli istnieje)
Jeśli kiedykolwiek uruchomiłeś `git init` w tym folderze, musisz usunąć starą historię, aby skrypt mógł zacząć od zera. **Upewnij się, że jesteś w głównym katalogu projektu** i jeśli istnieje tam podkatalog `.git`, usuń go.

## Krok 2: Uruchomienie skryptu

1.  Otwórz terminal (wiersz poleceń) w głównym katalogu projektu.
2.  Uruchom skrypt, podając jako argument skopiowany wcześniej adres URL repozytorium GitHub.

    ```bash
    .\podziel_i_pushuj.bat https://github.com/twoja-nazwa/twoje-repo.git
    ```

3.  Skrypt wykona automatycznie następujące operacje:
    *   Zainicjalizuje nowe repozytorium Git.
    *   Ustawi Ciebie jako autora i stworzy pierwszy commit z plikami backendowymi.
    *   Ustawi Twojego kolegę jako autora i stworzy drugi commit z plikami frontendowymi.
    *   Połączy się ze zdalnym repozytorium na GitHubie.
    *   Wyśle oba commity na gałąź `main`.

## Krok 3: Weryfikacja

Po zakończeniu działania skryptu, wejdź na stronę swojego repozytorium na GitHubie. W historii commitów powinieneś zobaczyć dwa wpisy: jeden od Ciebie (backend) i jeden od Twojego kolegi (frontend).

Od tego momentu możecie kontynuować pracę normalnie, zgodnie z przewodnikiem opisanym w pliku `WSPOLPRACA.md`.