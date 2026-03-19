# Adapters

Repositorio de librerias Java (Maven) para integraciones.

Este proyecto esta pensado para:
- Publicar artefactos versionados en GitHub Packages.
- Reutilizar cada modulo como dependencia en otros proyectos.
- Mantener varias versiones al mismo tiempo (por ejemplo `1.0.0` y `2.0.0`).

## Contenido del repositorio

| Modulo | ArtifactId | Descripcion |
|---|---|---|
| `parent` | `parent` | Libreria base reutilizable para integraciones (web, validacion, integracion, utilidades compartidas). |
| `rabbitmq-client` | `rabbitmq-client` | Integracion con RabbitMQ (AMQP + Spring Integration). |
| `redis-client` | `redis-client` | Integracion con Redis. |
| `aws/amazon-s3-client` | `amazon-s3-client` | Cliente para operaciones con AWS S3. |
| `aws/amazon-ses-client` | `amazon-ses-client` | Integracion para envio de correos (SES/Mail). |
| `aws/amazon-sqs-client` | `amazon-sqs-client` | Integracion con AWS SQS. |

Coordenadas Maven base:
- `groupId`: `co.com.clients.parent`
- Version inicial observada en modulos: `1.0.0`

## Requisitos

- Java 21
- Maven 3.9+
- Cuenta GitHub con acceso al repo `GustavoA198/adapters`
- Token GitHub para paquetes (si publicas/consumes desde local)

## Flujo general

1. Desarrollas cambios en un modulo.
2. Actualizas version del modulo (`MAJOR.MINOR.PATCH`).
3. Ejecutas `mvn clean deploy`.
4. Maven publica el artefacto en GitHub Packages.
5. Proyectos consumidores actualizan su `version` cuando quieran migrar.

> GitHub Packages conserva versiones anteriores, por lo tanto `1.0.0` no se pierde al publicar `2.0.0`.

## 1) Configurar credenciales localmente (seguro)

No guardes tokens en `pom.xml` ni en codigo fuente.

### 1.1 Variables de entorno (Windows PowerShell)

```powershell
[Environment]::SetEnvironmentVariable("GITHUB_ACTOR", "GustavoA198", "User")
[Environment]::SetEnvironmentVariable("GITHUB_TOKEN", "TU_TOKEN_GITHUB", "User")
```

Cierra y abre terminal para recargar variables.

### 1.2 Configurar `%USERPROFILE%\\.m2\\settings.xml`

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>${env.GITHUB_ACTOR}</username>
      <password>${env.GITHUB_TOKEN}</password>
    </server>
  </servers>
</settings>
```

## 2) Construir y validar modulos

Desde la raiz del repo (`adapters`):

```powershell
mvn -B -ntp -f parent/pom.xml validate
mvn -B -ntp -f redis-client/pom.xml validate
mvn -B -ntp -f rabbitmq-client/pom.xml validate
mvn -B -ntp -f aws/amazon-s3-client/pom.xml validate
mvn -B -ntp -f aws/amazon-ses-client/pom.xml validate
mvn -B -ntp -f aws/amazon-sqs-client/pom.xml validate
```

## 3) Publicar artefactos en GitHub Packages

Publica primero `parent` y luego clientes que lo usan.

```powershell
mvn -B -ntp -f parent/pom.xml clean deploy
mvn -B -ntp -f rabbitmq-client/pom.xml clean deploy
mvn -B -ntp -f redis-client/pom.xml clean deploy
mvn -B -ntp -f aws/amazon-s3-client/pom.xml clean deploy
mvn -B -ntp -f aws/amazon-ses-client/pom.xml clean deploy
mvn -B -ntp -f aws/amazon-sqs-client/pom.xml clean deploy
```

Repositorio Maven remoto:
- `https://maven.pkg.github.com/GustavoA198/adapters`

## 4) Usar una libreria en otro proyecto

### 4.1 Agregar repositorio GitHub Packages en el consumidor

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/GustavoA198/adapters</url>
  </repository>
</repositories>
```

### 4.2 Agregar dependencia

Ejemplo (`rabbitmq-client`):

```xml
<dependency>
  <groupId>co.com.clients.parent</groupId>
  <artifactId>rabbitmq-client</artifactId>
  <version>1.0.0</version>
</dependency>
```

## 5) Versionado recomendado

Usa Semantic Versioning:
- `MAJOR`: cambios incompatibles (ejemplo `1.0.0 -> 2.0.0`)
- `MINOR`: nuevas funcionalidades compatibles
- `PATCH`: correcciones de bug compatibles

Ejemplo de release:

```powershell
git add .
git commit -m "release(parent): 2.0.0"
git tag parent-2.0.0
git push
git push origin parent-2.0.0
```

## 6) Buenas practicas de seguridad

- No subas `settings.xml` al repo.
- No publiques tokens en README, scripts o logs.
- Si un token se expone, revocalo y genera uno nuevo.
- Usa permisos minimos para tokens (`read:packages`, `write:packages`, y `repo` solo si aplica).

## 7) Problemas comunes

### Error `403` al hacer `deploy`

Causas frecuentes:
- `GITHUB_TOKEN` no esta definido en la terminal actual.
- Token sin permisos de paquetes.
- `server.id` en `settings.xml` no coincide con `<id>` de `distributionManagement` (`github`).
- El token no tiene acceso al repo correcto.

### Error de resolucion en proyectos consumidores

- Falta configurar `<repositories>` en el proyecto consumidor.
- Falta `settings.xml` con credenciales para leer paquetes privados.

## 8) Estructura esperada

```text
adapters/
  aws/
    amazon-s3-client/
    amazon-ses-client/
    amazon-sqs-client/
  parent/
  rabbitmq-client/
  redis-client/
```

---

Si quieres automatizar publicaciones por tags, el siguiente paso recomendado es agregar un workflow en `.github/workflows/publish-packages.yml`.

