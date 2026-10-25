# Developer Notes

This project was written and reviewed for structural correctness (package
names, class names, import resolution, brace/paren balance, and consistent
method signatures across the GUI <-> ApiClient <-> Controller <-> Service
<-> Repository chain) without a live Gradle/MySQL environment available at
generation time. It has not been compiled with an actual JDK + Gradle +
MySQL yet.

If you hit a build or runtime error the first time you run it, that's
expected for a project this size on a first attempt - see the
Troubleshooting table in README.md, and note down the exact error (the
first `[ERROR]` / `Caused by:` line) so it can be fixed quickly.

## Design decisions worth knowing for your viva

- **Auth model**: rather than pulling in the full Spring Security starter
  (which auto-secures every endpoint and expects a login form), this
  project uses a small custom token store (`util/SessionManager.java`) and
  a per-controller header check (`util/AuthHelper.java`). Passwords
  themselves are still properly hashed with BCrypt
  (`spring-security-crypto` dependency only). This keeps the security
  model simple and easy to explain end-to-end, appropriate for a
  single-user local desktop app.
- **Swing <-> backend communication**: `gui/ApiClient.java` is the only
  class that ever makes an HTTP call. Every screen calls it and gets back
  plain Java objects (the same DTO classes the backend uses), or a
  readable `ApiClientException` shown via `JOptionPane`.
- **One process, one command**: `UniSwapApplication.main()` starts Spring
  Boot, waits for it to be ready, then opens the Swing window - there's
  no separate "start the backend" and "start the frontend" step.
- **Reserved SQL words**: `condition` and `year` are mapped to
  `item_condition` / `study_year` at the database column level from the
  start, avoiding the MySQL reserved-word error hit earlier in the SwapUni
  web project.
