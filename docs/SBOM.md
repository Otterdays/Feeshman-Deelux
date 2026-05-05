<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# SBOM — Software Bill of Materials

Authoritative inventory for **Feeshman Deelux**. **Sources of truth:** `build.gradle`, `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties`.

## This mod

| | |
|:---|:---|
| **mod_id** | `feeshmandeelux` |
| **Version** | 1.4.0 |
| **Maven group** | `com.yourname.feeshmandeelux` |
| **Artifact base** | `feeshmandeelux` |

## Dependencies (`build.gradle` / `gradle.properties`)

| Package | Version | Purpose |
|---------|---------|---------|
| com.mojang:minecraft | 26.1.2 | Game |
| net.fabricmc:fabric-loader | 0.19.2 | Loader |
| net.fabricmc.fabric-api:fabric-api | 0.148.0+26.1.2 | Fabric API |
| com.terraformersmc:modmenu | 18.0.0-alpha.8 | Config screen |
| eu.pb4:placeholder-api | 3.0.0+26.1 | ModMenu dependency |
| org.xerial:sqlite-jdbc | 3.46.1.3 | Stats DB driver (jar-in-jar) |

## Gradle plugins

| ID | Version |
|----|---------|
| `net.fabricmc.fabric-loom` | 1.16.1 |
| `maven-publish` | (core) |

## Toolchain

| Item | Value |
|------|--------|
| Java | 26 (Gradle `java { toolchain { languageVersion = 26 } }` in `build.gradle`) |
| Gradle (wrapper) | 9.6.0-20260503004846+0000 (nightly snapshot) |
| Fabric Loom | 1.16.1 |

[AMENDED 2026-05-05]: Java row was **25** when docs assumed `--release 25`; build now uses **JDK 26 toolchain** from `build.gradle`.

[AMENDED 2026-05-05 — shipping bytecode]: `build.gradle` now uses **`languageVersion = 25`** and **`JavaCompile.options.release = 25`** so the mod JAR emits **class file version 69** (Java 25). This avoids `UnsupportedClassVersionError` on typical Fabric setups (e.g. PrismLauncher with **Java 25**), which cannot load **v70** (Java 26) bytecode.

## Extra repositories

`mavenCentral`, `https://maven.fabricmc.net/`, `https://maven.terraformersmc.com/`, `https://maven.nucleoid.xyz/` — see `build.gradle`.

## Last updated

2026-05-04 — Updated to Minecraft 26.1.2 / mod 1.4.0. Source of truth: `gradle.properties` + `build.gradle`.
2026-05-05 — Mojang source port complete; Java toolchain **26**; `FeeshmanDatabase` `ThrowingFunction` for DB lambdas.
