# Changelog

## 0.1.1

### Fixed

- Remove the undeclared Guava runtime dependency from SSRF URL handling, which caused `NoClassDefFoundError` in applications without Guava.

## 0.1.0

### Added

- Add `SecurityUtil.trimCmd` for command-argument character trimming, preserving ASCII letters/digits, `_`, `-`, `,`, `.`, and Chinese characters in U+4E00–U+9FBB.

### Changed

- Isolate deserialization initialization, cache the default filter, make default whitelist collections immutable, and exclude static/transient fields from recursive allowlisting while retaining explicit collection serialization support.
- Reject malformed or ambiguous redirect targets and require HTTP(S) or a safe rooted relative path.
- Pin Java 8-compatible Maven Compiler 3.16.0, JAR 3.5.1, and Surefire 3.6.1 plugin versions.
- Prefer Maven toolchains for Java 8 and Java 11+ compilation, retain independent explicit JDK-path overrides, and keep test execution on Maven's runtime JDK.
- Document one-time toolchain configuration, common build commands, runtime-matrix testing, and failure diagnostics.
- Normalize domain-pattern case, protect character-mask state, and remove constructor-time overridable pattern parsing.
- Replace the Oracle descriptor parser with an independently implemented parser and add regression coverage.
- Consolidate decorators under `com.alibaba.seckit.decorator`, improve diagnostics, and clean up naming, comments, and test fixtures.
- Escape CTRL-Z (`0x1a`) as `\Z` without changing other SQL escape mappings.
- Remove runs of two or more hyphens after SQL character trimming, including runs formed by filtering other characters.
- Make English the default README, retain a Chinese translation, fix duplicate example variables and SQL-section formatting, and add security and contribution guidance.

## 0.0.2

### Added

- SQL input escaping and character-trimming helpers.
- HTML and JavaScript escaping for XSS protection.
- Whitelist-based deserialization with pattern and class overloads.
- Redirect-target validation with allowed and excluded domain patterns.
