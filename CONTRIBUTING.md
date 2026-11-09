# Contributing

Bug reports and focused pull requests are welcome at [alibaba/seckit](https://github.com/alibaba/seckit). For vulnerabilities, follow [SECURITY.md](SECURITY.md); do not publish exploit details in public issues.

## Prerequisites

- Maven 3.6.3 or newer installed and available on `PATH`.
- JDK 8u121 or newer within the JDK 8 line, plus JDK 11 or newer. A newer JDK alone cannot compile the Java 8 implementation's internal APIs.
- `JAVA_HOME` selects the JDK running Maven and the tests. Compiler toolchains do not select the test runtime.

The multi-release JAR contains a Java 8 implementation and a Java 9 overlay. JDK 8 compiles the base and tests; JDK 11+ compiles the overlay with `--release 9`. Javadoc uses JDK 8. Keep shared APIs in `src/main/java` and `src/main/java9` synchronized.

## Configure Toolchains Once (Recommended)

Add both JDK installations to `~/.m2/toolchains.xml` (`%USERPROFILE%\.m2\toolchains.xml` on Windows). If the file already exists, merge these entries instead of replacing other toolchains:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
    <toolchain>
        <type>jdk</type>
        <provides>
            <version>1.8</version>
        </provides>
        <configuration>
            <jdkHome>/path/to/jdk8</jdkHome>
        </configuration>
    </toolchain>
    <toolchain>
        <type>jdk</type>
        <provides>
            <version>17</version>
        </provides>
        <configuration>
            <jdkHome>/path/to/jdk17</jdkHome>
        </configuration>
    </toolchain>
</toolchains>
```

Replace the paths with JDK homes, not their `bin` or `jre` directories. The version metadata must match the installed JDK: both `8` and `1.8` are accepted for Java 8; use, for example, `11`, `17`, `21`, or `25` for the newer installation. The build selects Java 8 and a JDK in `[11,)`; it does not require a particular vendor.

This configuration is machine-specific and must not be committed. Maven reads it from its default location automatically. Use `mvn -t "/path/to/toolchains.xml" clean package` when testing a separate configuration without changing your default file.

## Build and Test

Run these commands from the repository root after configuring toolchains:

| Command | Purpose |
| --- | --- |
| `mvn clean test` | Compile both implementations and run the full test suite. |
| `mvn clean package` | Run tests, build the multi-release JAR, and generate Javadoc. |
| `mvn -Dtest=SqlToolTest,UrlToolTest,DeserializeToolTest test` | Run focused regressions. |
| `mvn -DskipTests package` | Package without running tests; test sources are still compiled. |

Artifacts are generated under `target/`. Do not use `verify` or `deploy` for a routine local build: release signing and publishing are separate maintainer tasks.

### Verify the Runtime Matrix

Changing compiler toolchains alone does not test another Java runtime. Check `mvn --version`, then run tests with Maven itself launched under each supported runtime. For example, on a POSIX shell:

```sh
JAVA_HOME="/path/to/jdk8" mvn clean test
JAVA_HOME="/path/to/jdk17" mvn clean test
```

Both commands use the same toolchains file. Java 8 tests load the base implementation; Java 9+ tests load the overlay first. Use a fresh `clean` build when switching runtimes or compiler installations. On Windows, set `JAVA_HOME` in your shell before running Maven.

Add boundary and failure regressions relevant to your change. HTTP-adapter and XXE network fixtures use ephemeral loopback servers; TEST-NET-1 URLs are mapped locally rather than contacted externally. File-based XXE fixtures still read environment-dependent resources. The OkHttp 2.7.5 IPv6 interceptor case is explicitly skipped because that client fails before reaching the interceptor. Preserve these distinctions rather than treating transport failures or timeouts as successful security checks.

Maven may need network access to download dependencies and plugins. Use `-o` only after the necessary artifacts have been cached.

## Explicit JDK Paths (Override / CI)

For a one-off build or CI job without toolchains configuration, explicit paths remain available:

```sh
mvn -Djdk8.home="/path/to/jdk8" -Djdk9plus.home="/path/to/jdk17" clean package
```

Each property overrides only its corresponding compiler toolchain. Supplying one path still requires a configured toolchain for the other JDK. `JAVA_HOME` continues to select the Maven/test runtime; these properties do not switch it.

### Troubleshooting

- **No matching toolchain:** configure both version entries and check that Maven reads the intended file; `-t` selects a non-default file.
- **Missing compiler or internal Java 8 API:** check the actual `jdkHome` and Java 8 update level. Metadata alone does not install a JDK.
- **Unexpected test runtime:** inspect `mvn --version`; toolchains and `-Djdk*.home` control compilation, not which JVM runs the tests. Surefire's warning that toolchains are ignored because `jvm` is set is intentional: tests follow Maven's runtime rather than the last selected compiler JDK.
- **Stale classes after changing JDKs:** rerun `mvn clean test` or `mvn clean package`.

## Pull Requests

- Keep changes scoped and preserve public behavior unless a behavior change has been agreed upon.
- Follow existing formatting and write comments and Javadoc in English, explaining non-obvious constraints rather than restating code.
- Add or update focused tests and relevant documentation. Keep `README.md` and `README_CN.md` aligned.
- Describe the reason for the change, the tests run, the JDKs used, and any tests skipped. Note any network or platform assumptions.
- Do not include credentials, private URLs, personal data, or unrelated generated artifacts.
