English | [简体中文](README_CN.md)
# Alibaba Security SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.alibaba/seckit.svg)](https://search.maven.org/artifact/com.alibaba/seckit)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

`seckit` provides multiple security protections to help developers prevent common vulnerabilities. The SDK exposes a unified entry via the `SecurityUtil` class and supports core protections such as JDBC connection string filtering, SSRF prevention, and XXE prevention.

## Features

### [JDBC Connection String Filtering](#1-jdbc-connection-string-filtering)
- Filters dangerous parameters in JDBC URLs, removing unsafe options like `allowLocalInfile`, `autoDeserialize`, etc.
- Prevents deserialization vulnerabilities and SQL injection attacks
- Supports multiple database types: MySQL, PostgreSQL, Oracle, SQL Server, ClickHouse, etc.

### [SSRF Protection](#2-ssrf-protection)
- Detects and prevents Server-Side Request Forgery attacks
- Supports multiple HTTP clients: Apache HttpClient 4.x/5.x, OkHttp 2/3
- Provides NetHook-based thread-level SSRF checks

### [XXE Protection](#3-xxe-protection)
- Adds XXE protection to XML parsers
- Supports multiple XML parsing frameworks: SAXReader, DocumentBuilderFactory, SAXParserFactory, etc.
- Automatically disables external entity references and DTD processing
- Prevents XML External Entity injection

### [SQL Escape](#4-sql-escape)
- User input escaping and SQL character trimming

### [Frontend Security](#5-frontend-security)
- HTML escape
- JavaScript escape

### [Deserialization Safety](#6-deserialization-safety)
- `readObject` with whitelist

### [Command Argument Trimming](#7-command-argument-trimming)
- Retain an explicit character allowlist, including Chinese characters

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>seckit</artifactId>
    <version>0.1.0</version>
    <scope>compile</scope>
</dependency>
```

### Gradle Dependency

```gradle
implementation 'com.alibaba:seckit:0.1.0'
```

## Usage Guide

### 1. JDBC Connection String Filtering

#### Supported Databases

| Database | URL Prefixes |
| -------- | ----------- |
| **MySQL** | `jdbc:mysql:`, `jdbc:mariadb:`, `jdbc:gbase:`, `jdbc:oceanbase:`, `jdbc:mysql2:`, `jdbc:mysql+srv:`, `mysqlx:`, `mysqlx+srv:` |
| **PostgreSQL** | `jdbc:postgresql:`, `jdbc:kingbase8:`, `jdbc:polardb:`, `jdbc:opengauss:` |
| **Oracle** | `jdbc:oracle:thin:`, `jdbc:oracle:oci:`, `jdbc:oracle:kprb:` |
| **SQL Server** | `jdbc:sqlserver:` |
| **ClickHouse** | `jdbc:clickhouse:`, `jdbc:ch:` |
| **MongoDB** | `jdbc:mongodb:`, `jdbc:mongodb+srv:`, `mongodb:`, `mongodb+srv:` |
| **Redis** | `jdbc:redis:` |
| **Hive** | `jdbc:hive:` |
| **Hive2** | `jdbc:hive2:` |
| **BigQuery** | `jdbc:bigquery:` |
| **Elasticsearch** | `jdbc:es:`, `jdbc:elasticsearch:` |
| **OpenSearch** | `jdbc:opensearch:` |
| **DB2** | `jdbc:db2:` |
| **Teradata** | `jdbc:teradata:` |
| **AS400** | `jdbc:as400:` |
| **ArrowFlightSQL** | `jdbc:arrow-flight-sql:`, `jdbc:arrow-flight:` |
| **TDEngine** | `jdbc:taos:`, `jdbc:TAOS:`, `jdbc:taos-rs:` |
| **Lindorm** | `jdbc:lindorm:table:`, `jdbc:lindorm:tsdb:` |
| **Redshift** | `jdbc:redshift:`, `jdbc:redshift:iam:` |
| **Presto** | `jdbc:presto:` |
| **Trino** | `jdbc:trino:` |
| **Greenplum** | `jdbc:pivotal:greenplum:` |
| **Sybase** | `jdbc:sybase:Tds:` |
| **Informix** | `jdbc:informix-sqli:` |
| **NetSuite** | `jdbc:ns:` |
| **OTS** | `jdbc:ots:http:`, `jdbc:ots:https:` |
| **ODPS** | `jdbc:odps:http:`, `jdbc:odps:https:` |
| **Phoenix** | `jdbc:phoenix:thin:` |
| **Impala** | `jdbc:impala:` |
| **Kylin** | `jdbc:kylin:` |
| **Snowflake** | `jdbc:snowflake:` |
| **Vertica** | `jdbc:vertica:` |
| **SAP** | `jdbc:sap:` |
| **DM** | `jdbc:dm:` |

Use `SecurityUtil.filterJdbcConnectionSource()` to filter dangerous parameters in JDBC URLs:

```java
import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.jdbc.JdbcURLException;

public class JdbcExample {
    public void filterJdbcUrl() {
        try {
            // Original URL contains dangerous parameters
            String originalUrl = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&allowLocalInfile=true&autoDeserialize=true&sessionVariables=abc&foo=bar";

            // Filtered safe URL
            String safeUrl = SecurityUtil.filterJdbcConnectionSource(originalUrl);
            System.out.println("Safe URL: " + safeUrl);
            // Output: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&allowLocalInfile=false&allowLoadLocalInfile=false&sessionVariables=abc

        } catch (JdbcURLException e) {
            System.err.println("Failed to parse JDBC URL: " + e.getMessage());
        }
    }
}
```

### 2. SSRF Protection

> 1. Call `SecurityUtil.initialize()` during application startup to enable SSRF protection initialization.
>
> 2. This feature does not work on Windows.
>
> 3. For JDK 16 and above, since JDK disables reflective access to internal protected/private members by default, and the protections need such access, add JVM options: `--add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED`

#### Supported HTTP Clients

| HTTP Client | Supported Versions | Adapter Class Paths |
|-----------|---------|-----------|
| **Apache HttpClient** | 4.x/5.x | `org.apache.http.impl.client.HttpClientBuilder` `org.apache.hc.client5.http.impl.classic.HttpClientBuilder` |
| **Apache HttpAsyncClient** | 4.x/5.x | `org.apache.http.impl.nio.client.HttpAsyncClientBuilder` `org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder` |
| **OkHttp 2** | 2.7.5+ | `com.squareup.okhttp.OkHttpClient` |
| **OkHttp 3** | 3.4.9+ | `okhttp3.OkHttpClient$Builder` |

#### 2.1 SSRF Checks with Apache HTTP Client

Add SSRF checks for HTTP clients:

```java
import com.alibaba.seckit.SecurityUtil;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;

public class SSRFExample {
    public void httpClientWithSSRF() throws Exception {
        // Add SSRF checks to HttpClient
        HttpClient client = SecurityUtil.withSSRFChecking(HttpClients.custom()).build();

        // Safe external request
        HttpResponse response = client.execute(new HttpGet("https://www.example.com"));
        System.out.println("Status: " + response.getStatusLine().getStatusCode());

        // Dangerous intranet requests will be blocked
        try {
            client.execute(new HttpGet("http://127.0.0.1:8080"));
        } catch (SecurityException e) {
            System.out.println("SSRF blocked: " + e.getMessage());
        }
    }
}
```

#### 2.2 SSRF Checks with OkHttp

```java
import com.alibaba.seckit.SecurityUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpSSRFExample {
    public void okHttpWithSSRF() throws Exception {
        // Add SSRF checks to OkHttp client
        OkHttpClient client = SecurityUtil.withSSRFChecking(new OkHttpClient.Builder()).build();

        // Safe request
        Request request = new Request.Builder()
                .url("https://www.example.com")
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Response code: " + response.code());
        }
    }
}
```

### 3. XXE Protection

#### Supported XML Parsers

| XML Parser | Framework | Adapter Class Paths |
|----------|---------|---------------------------------------------------------------|
| **SAXReader** | Dom4j | `org.dom4j.io.SAXReader` |
| **DocumentBuilderFactory** | JAXP | `javax.xml.parsers.DocumentBuilderFactory` |
| **SAXParserFactory** | JAXP | `javax.xml.parsers.SAXParserFactory` |
| **XMLInputFactory** | StAX | `javax.xml.stream.XMLInputFactory` |
| **SAXBuilder** | JDOM | `org.jdom2.input.SAXBuilder` |
| **Validator** | JAXP | `javax.xml.validation.Validator` |
| **XMLReader** | SAX | `org.xml.sax.XMLReader` `jdk.internal.org.xml.sax.XMLReader` |
| **TransformerFactory** | JAXP | `javax.xml.transform.TransformerFactory` |
| **SchemaFactory** | JAXP | `javax.xml.validation.SchemaFactory` |

Add XXE protection for XML parsers:

```java
import com.alibaba.seckit.SecurityUtil;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;

import java.io.InputStream;

public class XXEExample {
    public void parseXmlSafely() throws Exception {
        // Add XXE protection to SAXReader
        SAXReader reader = SecurityUtil.withXxeProtection(new SAXReader());

        // Safely parse XML
        InputStream xmlStream = getClass().getResourceAsStream("/safe.xml");
        Document document = reader.read(xmlStream);

        System.out.println("XML parsed: " + document.getRootElement().getName());
    }
}
```

### 4. SQL Escape

Provides safe filtering for user inputs, suitable for legacy code that concatenates SQL strings directly (PreparedStatement is still recommended).

```java
import com.alibaba.seckit.SecurityUtil;

// Escape user input safely before concatenating into SQL
String keyword = SecurityUtil.escapeSql(userInput);
String sql = "SELECT * FROM products WHERE name LIKE '%" + keyword + "%'";

// Strictly trim to allow only safe characters (for column names, order by, etc.)
String orderBy = SecurityUtil.trimSql4OrderBy(userProvidedColumn);
String sql2 = "SELECT * FROM products ORDER BY " + orderBy + " DESC";
```

### 5. Frontend Security

#### 5.1 HTML and JavaScript Escape
Escape user data before outputting to HTML or JavaScript contexts to avoid XSS.

```java
import com.alibaba.seckit.SecurityUtil;

String htmlText = SecurityUtil.escapeHtml(userInput);
String javascriptText = SecurityUtil.escapeJavascript(userInput);
```

#### 5.2 Redirect Validation

Allow rooted relative paths such as `/login`, or HTTP(S) destinations whose host matches the allowlist. Network-path references (`//example.com/path`) also require an allowed host. Invalid URLs, backslashes, unencoded whitespace/control characters, and userinfo are rejected; exclusions take precedence.

```java
import com.alibaba.seckit.SecurityUtil;

boolean allowed = SecurityUtil.isRedirectUrlAllowed(redirectUrl, "*.example.com");

// Support exclude list
boolean allowed2 = SecurityUtil.isRedirectUrlAllowed(redirectUrl,
        new String[]{"*.example.com"},
        new String[]{"evil.example.com"});
```

### 6. Deserialization Safety

Use an `ObjectInputStream` with a whitelist to restrict the classes that can be deserialized, reducing deserialization risks. Class-based allowlisting recursively follows superclass and non-static, non-transient instance-field types. Custom `writeObject`/`readObject` or `Externalizable` implementations may serialize additional types that must be allowed explicitly.

```java
import com.alibaba.seckit.SecurityUtil;
import java.io.ObjectInputStream;
import java.io.InputStream;

// Allow only basic JDK classes (default whitelist)
ObjectInputStream ois1 = SecurityUtil.newObjectInputStream(in);

// Whitelist via string patterns
ObjectInputStream ois2 = SecurityUtil.newObjectInputStream(in,
        "com.myapp.safe.**", "org.example.model.*");

// Whitelist via specific classes
ObjectInputStream ois3 = SecurityUtil.newObjectInputStream(in,
        com.myapp.safe.Model.class, com.myapp.safe.Event.class);
```

### 7. Command Argument Trimming

`SecurityUtil.trimCmd` retains ASCII letters/digits, `_`, `-`, `,`, `.`, and Chinese characters in `U+4E00–U+9FBB`. Other characters, including whitespace and shell metacharacters, are removed. Null input returns null; empty input returns an empty string.

```java
String argument = SecurityUtil.trimCmd("中文 文件-01.txt"); // 中文文件-01.txt
String commaArgument = SecurityUtil.trimCmd("日志, 报表"); // 日志,报表
```

This is character trimming for an individual argument, not shell escaping or validation of a complete command. Use separate arguments with `ProcessBuilder`, and validate executable names and options separately; leading hyphens are retained.

## Advanced Usage

### NetHook-based SSRF Checking

Use NetHook to perform SSRF checks on all TCP connections:

```java
import com.alibaba.seckit.SecurityUtil;
import exception.ssrf.com.alibaba.seckit.SSRFUnsafeConnectionException;

import java.net.URL;

public class NetHookSSRFExample {
    public void netHookSSRF() {
        try {
            // Start SSRF checks
            SecurityUtil.startSSRFNetHookChecking();

            // Perform network request
            URL url = new URL("https://www.example.com");
            url.openConnection().getInputStream();

        } catch (SSRFUnsafeConnectionException e) {
            System.err.println("SSRF attack detected: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Request failed: " + e.getMessage());
        } finally {
            // Stop SSRF checks
            SecurityUtil.stopSSRFNetHookChecking();
        }
    }
}
```

## Building from Source

Install Maven 3.6.3+, JDK 8u121+ within the Java 8 line, and JDK 11+. **Configure both JDKs once in `~/.m2/toolchains.xml`** using the [toolchains example](CONTRIBUTING.md#configure-toolchains-once-recommended), then build from the repository root:

```sh
mvn clean package
```

This runs tests and builds the multi-release JAR containing Java 8 and Java 9+ implementations. No JDK-path `-D` arguments are needed when toolchains are configured.

```sh
mvn clean test
mvn -Dtest=SqlToolTest,UrlToolTest,DeserializeToolTest test
mvn -DskipTests package
```

`JAVA_HOME` selects the Maven/test runtime, independently of the compiler toolchains. Repeat tests under JDK 8 and a newer supported JDK when changing multi-release code. For one-off builds without toolchains, `-Djdk8.home` and `-Djdk9plus.home` remain available as [explicit overrides](CONTRIBUTING.md#explicit-jdk-paths-override--ci). See [CONTRIBUTING.md](CONTRIBUTING.md) for configuration, runtime-matrix examples, and test-fixture precautions.

## License

This project is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

## Contributing

Issues and Pull Requests are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) and [CHANGELOG.md](CHANGELOG.md).

For security concerns, follow [SECURITY.md](SECURITY.md) instead of publishing exploit details in public issues.

## Contact

- Project Home: [GitHub Repository](https://github.com/alibaba/seckit)
- Issue Tracker: [Issues](https://github.com/alibaba/seckit/issues)
