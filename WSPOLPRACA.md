# Przewodnik Współpracy przy Użyciu Git i GitHub

Ten przewodnik opisuje, jak efektywnie współpracować nad projektem, dzieląc pracę na backend i frontend. Użyjemy do tego gałęzi (branches) w systemie kontroli wersji Git.

## Konfiguracja Repozytorium

### 1. Inicjalizacja Repozytorium Git

Jeśli jeszcze tego nie zrobiliście, pierwsza osoba (np. Ty, jako osoba od backendu) powinna zainicjalizować repozytorium Git w głównym katalogu projektu.

```bash
git init
git add .
git commit -m "Initial commit: Konfiguracja projektu i struktura aplikacji"
```

### 2. Stworzenie Repozytorium na GitHubie

1.  Zaloguj się na swoje konto na [GitHubie](https://github.com).
2.  Stwórz nowe, **prywatne** repozytorium (chyba że chcecie, aby kod był publiczny). **Nie inicjalizuj go z plikami `README` ani `.gitignore`**, ponieważ mamy już je lokalnie.
3.  Po stworzeniu repozytorium, GitHub wyświetli Ci adres URL. Użyj go w poniższych komendach, aby połączyć lokalne repozytorium ze zdalnym.

```bash
# Zastąp <URL_REPOZYTORIUM> adresem URL skopiowanym z GitHuba
git remote add origin <URL_REPOZYTORIUM>
git branch -M main
git push -u origin main
```

### 3. Dodanie Współpracownika

Na stronie repozytorium na GitHubie przejdź do `Settings` > `Collaborators` i dodaj swojego kolegę jako współpracownika. Otrzyma on zaproszenie, które musi zaakceptować.

## Model Pracy z Gałęziami

Będziemy używać prostego modelu, gdzie gałąź `main` zawiera stabilną, działającą wersję aplikacji. Cała praca rozwojowa będzie odbywać się na osobnych gałęziach.

### 1. Stworzenie Gałęzi Deweloperskich

Stwórzcie dwie główne gałęzie dla waszych dziedzin:

*   `backend-dev` - dla Ciebie (praca nad logiką aplikacji, Javą, Springiem)
*   `frontend-dev` - dla Twojego kolegi (praca nad HTML, CSS, JavaScript)

```bash
# Tworzenie i przełączanie się na gałąź backend-dev
git checkout -b backend-dev
git push --set-upstream origin backend-dev

# Tworzenie i przełączanie się na gałąź frontend-dev
git checkout -b frontend-dev
git push --set-upstream origin frontend-dev
```
**Ważne:** Każdy z Was powinien na początku wykonać te komendy, aby mieć obie gałęzie u siebie lokalnie.

### 2. Codzienna Praca

**Ty (Backend Developer):**

1.  Zawsze upewnij się, że pracujesz na właściwej gałęzi:
    ```bash
    git checkout backend-dev
    ```
2.  Przed rozpoczęciem pracy, pobierz najnowsze zmiany z gałęzi `main`, aby mieć aktualną bazę:
    ```bash
    git pull origin main
    ```
3.  Wprowadzaj zmiany w plikach backendowych (np. w katalogu `src/main/java/`).
4.  Gdy ukończysz jakąś część pracy (np. dodasz nową funkcjonalność), dodaj zmiany do "poczekalni" i stwórz commita z opisem zmian:
    ```bash
    # Dodaje wszystkie zmienione pliki
    git add . 
    # Tworzy commit
    git commit -m "Backend: Dodano logowanie użytkowników" 
    ```
5.  Wyślij swoje zmiany na zdalną gałąź na GitHubie:
    ```bash
    git push
    ```

**Twój Kolega (Frontend Developer):**

1.  Zawsze upewnij się, że pracuje na właściwej gałęzi:
    ```bash
    git checkout frontend-dev
    ```
2.  Przed rozpoczęciem pracy, pobierz najnowsze zmiany z gałęzi `main`:
    ```bash
    git pull origin main
    ```
3.  Wprowadza zmiany w plikach frontendowych (np. w `src/main/resources/templates/` lub `src/main/resources/static/`).
4.  Gdy ukończy jakąś część pracy, tworzy commita:
    ```bash
    git add .
    git commit -m "Frontend: Poprawiono wygląd strony logowania"
    ```
5.  Wysyła swoje zmiany na zdalną gałąź na GitHubie:
    ```bash
    git push
    ```

### 3. Łączenie Zmian (Pull Requests)

Gdy któraś z funkcji (backendowa lub frontendowa) jest gotowa i przetestowana, możecie połączyć ją z główną gałęzią `main` za pomocą **Pull Request** na GitHubie.

1.  Przejdź na stronę repozytorium na GitHubie.
2.  GitHub powinien automatycznie wykryć, że Twoja gałąź (`backend-dev` lub `frontend-dev`) ma nowe zmiany i zaproponuje stworzenie Pull Requesta.
3.  Stwórz Pull Request, opisując, co zostało zrobione. Druga osoba może przejrzeć zmiany (code review) i je zatwierdzić.
4.  Po zatwierdzeniu, zmiany są łączone (merge) z gałęzią `main`.

Dzięki takiemu podejściu Wasza praca jest odizolowana, a gałąź `main` zawsze zawiera działającą wersję aplikacji. Unikacie w ten sposób chaosu i konfliktów.