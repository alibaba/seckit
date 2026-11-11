# Changelog

## 0.1.1

### Fixed

- Remove the undeclared Guava runtime dependency from SSRF URL handling, which caused `NoClassDefFoundError` in applications without Guava.

## 0.1.0

### Added

- SSRF: `SecurityUtil.initialize()` installs the SSRF network hooks. Call it once during application startup before using SSRF protection.
- SQL: `SecurityUtil.escapeSql` escapes user input for SQL string literals, including CTRL-Z (`0x1a`) as `\Z`.
- SQL: `SecurityUtil.trimSql` and `SecurityUtil.trimSql4OrderBy` keep only an allowlist of characters for query conditions and `ORDER BY` columns, and remove runs of two or more hyphens.
- XSS: `SecurityUtil.escapeHtml` and `SecurityUtil.escapeJavascript` escape data for HTML and JavaScript contexts.
- Deserialization: `SecurityUtil.newObjectInputStream` overloads create allowlist-filtered `ObjectInputStream`s from basic JDK classes, class-name patterns, and/or classes. Class-based allowlisting follows superclasses and non-static, non-transient fields. On Java 9+ the multi-release JAR uses `java.io.ObjectInputFilter`.
- Redirects: `SecurityUtil.isRedirectUrlAllowed` checks redirect targets against allowed and excluded domains (`*.example.com` also matches `example.com`; matching is case-insensitive). Malformed or ambiguous URLs are rejected; only HTTP(S) URLs and rooted relative paths are accepted.
- Commands: `SecurityUtil.trimCmd` trims command arguments to ASCII letters/digits, `_`, `-`, `,`, `.`, and Chinese characters in U+4E00–U+9FBB.
- New runtime dependency: `org.unbescape:unbescape` 1.1.6.RELEASE.

### Changed

- SSRF: also block IPv6 unique local addresses (`fc00::/7`), IPv4 shared address space (`100.64.0.0/10`), benchmarking addresses (`198.18.0.0/15`), and this host's network interface addresses. NetHook now rejects unspecified destinations such as `[::]`, which the JDK rewrites to a local address before connecting.
- SSRF: on JDK 16+, the required JVM options are now `--add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED`.
- SSRF: hook installation failures are logged through SLF4J instead of being printed to standard error.
- JDBC: Oracle TNS descriptors are parsed by an independently implemented, bounded parser. Filtering removes each rejected element once and only keeps `PROTOCOL` values `TCP`/`TCPS`.
- Provided client versions used for compilation and tests: Apache HttpClient 4.4 → 4.5.14, HttpClient 5 5.2.1 → 5.6.3. The duplicate `commons-lang3` declaration was removed.
- Build: the artifact is a multi-release JAR. Building requires JDK 8u121+ and JDK 11+ (Maven toolchains preferred); Maven plugin versions are pinned.
- Documentation: English is the default README with a Chinese translation; security and contribution guides were added.

### Fixed

- JDBC: Snowflake URLs are rebuilt correctly instead of producing an invalid URL with a duplicated `jdbc:snowflake:` prefix.

### Removed

- `com.alibaba.seckit.Decorator` and `com.alibaba.seckit.AbstractDecorator`; use `com.alibaba.seckit.decorator.Decorator` and `com.alibaba.seckit.decorator.AbstractDecorator`.

## 0.0.1

### Added

- Initial release: JDBC connection string filtering, SSRF protection for Apache HttpClient 4.x/5.x and OkHttp 2/3 plus NetHook-based checks, and XXE protection for common XML parsers.
