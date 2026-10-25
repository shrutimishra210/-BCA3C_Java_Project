# SwapUni

**A secure campus marketplace desktop application for college students.**

Buy, sell, exchange, borrow, and donate pre-owned items — books, electronics,
stationery, lab equipment, calculators, and other useful college items —
with fellow students on your campus.

SwapUni is a **Java Swing desktop application** backed by a **Spring Boot**
REST API and a **local MySQL** database. There is no browser involved
anywhere in this project — it opens and runs as a normal Windows/Mac/Linux
desktop program.

---

## 1. Features

### Module 1 — User Authentication & Profile
Sign up, log in, log out, view and edit your profile. Passwords are hashed
with BCrypt and are never stored or transmitted in plain text. Email and
username are enforced unique.

### Module 2 — Marketplace / Listings
Browse all available items, search by keyword, filter by category, view
full item details, and add/edit/delete your own listings. Every listing has
a name, description, category, price (if selling), condition, and
transaction type (Sell / Exchange / Borrow / Donate). Only logged-in users
can create listings.

### Module 3 — Buy, Exchange & Borrow Requests
Send a request on any listing (buy, exchange, or borrow). Track request
status (`PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`, `COMPLETED`). The
listing owner sees incoming requests and can accept/reject; the requester
sees their own outgoing requests and can cancel a pending one. Accepting a
request automatically records a transaction and updates the listing's
status.

### Module 4 — Favourites & Wishlist
Save any listing to your favourites, remove it later, and jump straight to
its details from your Favourites screen. Duplicate favourites are
prevented.

### Module 5 — My Listings & Transactions
A dedicated screen for everything you've listed — active, sold, exchanged,
borrowed, or donated — plus edit/delete/mark-as-sold actions and a full
transaction history showing what happened to each item.

### Module 6 — Dashboard & Search
A personalized dashboard after login: welcome message, active listing
count, favourite count, pending request count, recent marketplace items,
popular categories, and quick-navigation buttons. The Marketplace screen's
search bar and filters (category, transaction type) work together.

---

## 2. Technology Stack

```
Java 17
Java Swing            - the desktop GUI
Spring Boot 3.2.5     - backend REST API + business logic
Spring Data JPA       - database access
Gradle                - build tool (NOT Maven)
MySQL 8                - local database
BCrypt (spring-security-crypto) - password hashing
```

### Architecture

```
Java Swing Desktop GUI  (com.uniswap.gui)
          |
          v   (HTTP calls to http://localhost:8080/api/...)
Spring Boot Backend      (com.uniswap.controller)
          |
          v
Service Layer            (com.uniswap.service)
          |
          v
Repository Layer         (com.uniswap.repository)
          |
          v
Spring Data JPA
          |
          v
Local MySQL Database (uniswap_db)
```

Both the backend and the desktop window run **in the same process** -
`SwapUniApplication.main()` starts the Spring Boot backend first, then
opens the Swing window once the backend is ready. There's only one thing
to run, not two separate servers to juggle.

---

## 3. System Requirements

- **Java JDK 17 or higher**
- **MySQL 8** (running locally)
- **Gradle 7.6+** (see the note in Section 7 below - important)
- An IDE such as IntelliJ IDEA or VS Code (with the Java extensions) is
  recommended but not required
- Any reasonably modern Windows, macOS, or Linux machine - no special
  hardware needed

---

## 4. MySQL Setup

```
Install MySQL
      |
      v
Make sure the MySQL80 service is running
      |
      v
That's it - the app creates the database and tables for you
```

You do **not** need to manually create the database or run the SQL script.
`spring.datasource.url` in `application.properties` includes
`createDatabaseIfNotExist=true`, and `spring.jpa.hibernate.ddl-auto=update`
creates every table automatically the first time you run the app.

The `database/uniswap_db.sql` script is included as a **reference** (e.g.
for your project report, or if you'd rather set the schema up by hand) -
it matches the auto-generated schema exactly.

Set your MySQL credentials as environment variables before running the app
(see Section 8) rather than editing `application.properties` directly, so
you never accidentally commit or share your password.

---

## 5. Gradle Setup

**This project intentionally uses Gradle, not Maven** - there is no
`pom.xml` anywhere in this project, only `build.gradle` and
`settings.gradle`.

### Important note about the Gradle wrapper

Normally a Gradle project ships with a `gradlew` / `gradlew.bat` wrapper
script plus a small `gradle-wrapper.jar`, so you never need Gradle
installed globally. That wrapper jar is a downloaded binary file, so it
isn't included in this project bundle. You have two easy options:

**Option A (recommended) — Open it in IntelliJ IDEA.**
IntelliJ (Community Edition is free) auto-detects `build.gradle`, offers
to import the project, and will generate the wrapper and download every
dependency for you automatically. This is the path of least resistance.

**Option B — Install Gradle yourself.**
1. Go to `gradle.org/install` and follow the Windows install instructions
   (very similar to how you installed Maven previously - download, extract,
   add the `bin` folder to your PATH)
2. Confirm with `gradle -version` in a fresh terminal
3. From the project folder, you can optionally generate the wrapper
   yourself so future runs don't need Gradle installed globally:
   ```
   gradle wrapper --gradle-version 8.7
   ```
   After this, `gradlew.bat` (Windows) / `./gradlew` (Mac/Linux) will work
   exactly like `mvnw` did in a Maven project.

### Building the project

```
gradle build
```
(or `gradlew build` / `./gradlew build` once you've generated the wrapper)

This downloads all dependencies and compiles the code. Look for:
```
BUILD SUCCESSFUL
```

---

## 6. Running the Application

Because the Swing GUI and the Spring Boot backend start from the same
`main()` method, there is only **one** command - it starts the backend,
waits for it to be ready, and then opens the desktop window automatically.

In the project's terminal (PowerShell on Windows), set your MySQL
credentials for this session, then run the app:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YOUR_MYSQL_PASSWORD"
gradle bootRun
```

(Using Command Prompt instead of PowerShell? Use `set DB_USERNAME=root` /
`set DB_PASSWORD=...` instead of `$env:...`.)

Wait for the console to show:
```
Started SwapUniApplication in X.XXX seconds
SwapUni sample data loaded successfully!
Sample login: amit.sharma@cgc.edu.in / Password@123
```

A moment later, the **SwapUni splash screen** should appear, followed by
the **Login window** - this is the actual desktop application opening, not
a browser tab. Log in with the sample account above, or click "Create an
Account" to sign up as yourself.

To stop the app, simply close the Swing window, or press `Ctrl+C` in the
terminal.

---

## 7. Project Structure

```
SwapUni/
├── build.gradle, settings.gradle     - Gradle build config (no pom.xml)
├── database/uniswap_db.sql           - reference SQL schema
├── README.md
└── src/main/
    ├── resources/application.properties
    └── java/com/uniswap/
        ├── SwapUniApplication.java   - starts backend, then opens Swing GUI
        ├── model/                    - JPA entities (User, Listing, Category,
        │                               Favourite, Request, Transaction) + enums
        ├── repository/               - Spring Data JPA repositories
        ├── service/                  - business logic (one service per module)
        ├── controller/                - REST endpoints the Swing GUI calls
        ├── dto/                      - request/response objects
        ├── config/                   - password hashing bean, startup data seeding
        ├── exception/                - clean error handling (ApiException, handler)
        ├── util/                     - session/token management, auth helper
        └── gui/                      - every Swing screen (14 screens total)
```

---

## 8. Database Structure

| Table          | Purpose                                              |
|----------------|-------------------------------------------------------|
| `users`        | Accounts. Email + username unique. Password is a BCrypt hash. |
| `categories`   | Books, Electronics, Stationery, College Supplies, Lab Equipment, Other. |
| `listings`     | Every item posted. Belongs to a `user` (owner) and a `category`. |
| `favourites`   | Wishlist entries. Belongs to a `user` and a `listing`. Unique per pair. |
| `requests`     | Buy/exchange/borrow/donate requests on a listing, from a requester. |
| `transactions` | Completed swaps - created automatically when a request is accepted. |

```
User
 |
 |-- Listings        (one user, many listings)
 |-- Favourites       (one user, many favourites)
 |-- Requests          (as requester)
 |-- Transactions      (as requester or owner)
```

> **Note for anyone extending this project:** `condition` and `year` are
> reserved/troublesome words in MySQL, so the corresponding columns are
> named `item_condition` and `study_year` at the database level (the Java
> field names stay `condition` / `year` - only the `@Column(name = ...)`
> mapping changes). If you add new fields later, avoid other SQL keywords
> the same way.

---

## 9. Application Workflow

```
START
  |
  v
Splash Screen
  |
  v
Login / Sign Up
  |
  v
Dashboard
  |
  v
Marketplace -> Search / Filter -> View Item
  |
  v
Buy / Exchange / Borrow / Donate -> Send Request
  |
  v
Owner receives request in "Requests" (Incoming tab)
  |
  v
Accept / Reject
  |
  v
Transaction recorded automatically -> visible in "Transactions"
  |
  v
Logout (with confirmation)
```

---

## 10. Common Errors

| Symptom | What it means / What to do |
|---|---|
| `Communications link failure` / `Connection refused` | MySQL isn't running. Check the `MySQL80` Windows service is started. |
| `Access denied for user 'root'` | Your `DB_PASSWORD` doesn't match your real MySQL root password, or wasn't set in the same terminal session before running. |
| `Public Key Retrieval is not allowed` | Already handled - `allowPublicKeyRetrieval=true` is set in `application.properties`. If you see it anyway, confirm you didn't overwrite that file. |
| `...syntax to use near 'condition,...'` | A reserved MySQL word was used unescaped as a column name. Already avoided in this project (see Section 8 note) - if you add new entity fields, check they're not SQL keywords. |
| `'gradle' is not recognized` | Gradle isn't installed or not on PATH - see Section 5, or open the project in IntelliJ instead. |
| Port 8080 already in use | Something else is using that port. Run `$env:SERVER_PORT="8081"` before `gradle bootRun`. |
| Swing window never appears, only backend logs show | Give it a few extra seconds after "Started SwapUniApplication" - the splash screen shows briefly first. If it truly never appears, check for a Swing/AWT error printed in the terminal. |
| "Can't reach the SwapUni backend. Is it still running?" (shown inside the app) | The backend process stopped or crashed after the GUI opened. Check the terminal for errors and restart with `gradle bootRun`. |
| `BUILD FAILED` during `gradle build` | Scroll up to the first line starting with `* What went wrong:` or `error:` and note it down - it usually points to one specific file. |

---

## 11. Testing Checklist

**Authentication:** sign up, duplicate email rejected, duplicate username
rejected, login with correct/incorrect password, logout.

**Marketplace:** add listing, view listing, edit listing, delete listing,
search by keyword, filter by category, filter by transaction type.

**Requests:** send request, accept request, reject request, cancel
request, status updates correctly on both the incoming and outgoing side.

**Favourites:** add favourite, remove favourite, duplicate favourite
rejected with a clear message.

**Transactions:** accepting a request creates a transaction and updates
the listing's status (Sold/Exchanged/Borrowed/Donated); transaction
history displays correctly for both parties.

**Database:** insert (sign up / add listing), read (marketplace search),
update (edit listing / edit profile), delete (delete listing).

---

*SwapUni — built for a BCA mini-project. Not intended for production use
as-is (in particular, the session/auth model is intentionally simple for a
single-user local desktop app, and would need hardening for any real
multi-user deployment).*
