# Booking

This is a web application based on the [Jmix](https://www.jmix.io) framework.

## Getting Started

The newly created project requires Java 21 and uses the embedded HSQL database.

Use the following resources to learn more about Jmix:
* [Jmix Documentation](https://docs.jmix.io)
* [Online Demo Applications](https://www.jmix.io/live-demo)
* [Jmix AI Assistant](https://ai-assistant.jmix.io) (also available in the **Jmix AI** tool window of IntelliJ IDEA)

## Development

- [Setup](https://docs.jmix.io/jmix/setup.html) your development environment.
- [Open](https://docs.jmix.io/jmix/studio/project.html#opening-existing-project) the project in the IDE.
- If you want to use AI agents to develop the application, check out the [Jmix AI Agent Guidelines](https://github.com/jmix-framework/jmix-agent-guidelines) repository.

## Running

To start the application, use the **Booking Jmix Application** run configuration in your IDE, or run the following command in the project root directory:

```bash
./gradlew bootRun
```

The application will be available at <http://localhost:8080>.

The default user credentials are:
* Login: `admin`
* Password: `admin`

**WARNING**: Change admin password and remove `ui.login.defaultUsername` and `ui.login.defaultPassword` application properties when deploying the application to production.

## Deployment

Profiles:

* `dev` (default when no profile is set): embedded HSQLDB in `.jmix/hsqldb`, demo data on an empty database, login form prefilled with `admin`.
* `prod`: PostgreSQL configured by the environment variables `DB_URL`, `DB_USER`, `DB_PASSWORD` (required, no defaults), no demo data, quieter logs.

Build the production JAR (the `vaadin { productionMode = false }` block in `build.gradle` stays for development):

```shell
./gradlew -Pvaadin.productionMode=true bootJar
```

Run it with Docker Compose (app + PostgreSQL 17):

```shell
cp .env.example .env   # then set real passwords in .env
docker compose up -d --build
```

The app listens on `http://localhost:8080` (or `APP_PORT` from `.env`). Stop it with `docker compose down`; add `-v` only if you want to delete the volumes: `booking-pgdata` (database) and `booking-files` (uploaded floor plans, stored at `/app/filestorage` in the container, overridable with `FILE_STORAGE_DIR`).

**First step after the first production start:** the database is seeded with the user `admin` / password `admin`. Log in and change that password immediately.
