# Instrukcja Uruchomienia Aplikacji Medycznej

Poniżej znajduje się instrukcja krok po kroku, jak zbudować i uruchomić aplikację.

## Wymagania Wstępne

Przed rozpoczęciem upewnij się, że masz zainstalowane następujące oprogramowanie:

1.  **Java Development Kit (JDK)** w wersji 17 lub nowszej.
2.  **Apache Maven** - narzędzie do automatyzacji budowy projektów Java.

Możesz sprawdzić wersje zainstalowanego oprogramowania, wykonując w terminalu poniższe komendy:
```bash
java -version
mvn -version
```

## Kroki Uruchomienia

### 1. Budowanie Aplikacji

Przejdź do głównego katalogu projektu (tam, gdzie znajduje się plik `pom.xml`), a następnie uruchom poniższą komendę, aby zbudować aplikację. Komenda ta pobierze wszystkie wymagane zależności i spakuje aplikację do pliku `.jar`.

```bash
mvn clean install
```

Po pomyślnym zakończeniu budowania, w katalogu `target/` powinien pojawić się plik `medical-app-1.0-SNAPSHOT.jar`.

### 2. Uruchomienie Aplikacji

Aplikację można uruchomić na dwa sposoby:

**Opcja A: Użycie pluginu Maven Spring Boot**

W głównym katalogu projektu wykonaj komendę:

```bash
mvn spring-boot:run
```

Aplikacja zostanie uruchomiona.

**Opcja B: Uruchomienie pliku JAR**

Alternatywnie, możesz uruchomić aplikację bezpośrednio z pliku JAR, który został utworzony w kroku 1.

```bash
java -jar target/medical-app-1.0-SNAPSHOT.jar
```

### 3. Dostęp do Aplikacji

Po uruchomieniu serwera, aplikacja będzie dostępna w przeglądarce internetowej pod adresem:

[http://localhost:8080](http://localhost:8080)

### 4. Dostęp do Konsoli Bazy Danych H2

Aplikacja korzysta z wbudowanej, pamięciowej bazy danych H2. Możesz uzyskać do niej dostęp, aby przeglądać dane.

1.  Przejdź pod adres: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
2.  W polu **JDBC URL** upewnij się, że wpisana jest wartość: `jdbc:h2:mem:medicaldb`
3.  Nazwa użytkownika to `sa`, a pole hasła pozostaw puste.
4.  Kliknij **Connect**.

---