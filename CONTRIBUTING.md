# Contributing to OneBox

OneBox is maintained by one person, so the fastest way to get a change merged is to keep it small and focused. This file explains what is useful, how to build, and what will not be merged.

## Ways to contribute

Code is not the only useful contribution:

| Contribution | Why it matters |
| --- | --- |
| **Bug reports** | Include app version, Android version, device model and steps to reproduce. `adb logcat` output is gold. |
| **Translations** | The app and the Play listing ship in 13 locales. A wrong translation in your language is a bug nobody else here can see. See `fastlane/metadata/android/<locale>/` and `res/values-<locale>/strings.xml`. |
| **Device testing** | OneBox targets Android 7.0+ across a lot of OEM ROMs. "Works on my Pixel" is not "works on HyperOS". |
| **Tool ideas** | 90+ tools already exist. The interesting gap is *which tool the agent should drive next*, not another settings screen. |
| **Documentation** | `docs/modules.md` and this file are the map. Anything unclear to you is unclear to the next person. |
| **Code** | See below. |

## Before you start

- Issues labelled [`good first issue`](https://github.com/wangzhishou/OneBox/labels/good%20first%20issue) are scoped to be doable without deep context.
- [`help wanted`](https://github.com/wangzhishou/OneBox/labels/help%20wanted) means a PR is actively wanted.
- **Comment on the issue before you start**, so two people do not write the same patch. There is no formal assignment — saying you are on it is enough.
- Found a security problem? Do not open a public issue. See [SECURITY.md](SECURITY.md).

## Development setup

### Requirements

- **JDK 17**
- Android SDK with **compileSdk 37**, available through `ANDROID_HOME` or `local.properties`
- Around 10 GB of free disk and some patience: the first build compiles 127 modules
- **No API keys, keystores or backend access are required**

`gradle/libs.versions.toml` and `gradle/wrapper/gradle-wrapper.properties` are the source of truth for every version. Ignore version numbers quoted in prose anywhere else, including the README.

### Clone

The repository carries a lot of binary assets, so a full clone is large. For building, a shallow clone is enough:

```bash
git clone --depth=1 https://github.com/wangzhishou/OneBox.git
cd OneBox
git remote add upstream https://github.com/wangzhishou/OneBox.git
```

### Build

```bash
./gradlew :app:assembleFossUniversalDebug                 # the variant CI checks
./gradlew :app:assembleGoogleUniversalDebug               # recommended local build (Google / Play channel)
./gradlew :feature:ai:compileGoogleUniversalDebugKotlin   # one module, much faster
```

`google` is the overseas / Play flavor and the default local build target; swap `Google` for `Onebox`, `Xiaomi`, `Huawei`, … when you need a domestic channel.

`foss` is the fully-FOSS flavor: no Google Play Services, no Firebase, no WeChat or Alipay SDKs, and it needs no keystore. It is the cheapest variant that still exercises the whole module graph, which is why pull requests are verified against it.

If you hit `InjectProcessingStep was unable to process ... could not be resolved`, that is a KSP/Hilt cache problem, not your code:

```bash
./gradlew clean :feature:app:kspGoogleUniversalDebugKotlin
```

### Keys are optional

The project syncs and builds with no key files at all. Release signing falls back to placeholders, debug builds use AGP's default debug keystore, and third-party service keys are empty so those features stay disabled.

To build a fully configured variant, copy `keystore.properties.template` to `keystore.properties` and fill it in. **Never commit it** — it is gitignored for a reason.

### Tests

Be aware before you write any: this project has almost no test culture, deliberately. There is one JVM test module (`feature:xiangqi`) and no instrumentation or Compose UI tests. **Do not add a test suite as a drive-by contribution** — a focused fix that you built and ran is what is expected. If you fix a runtime bug, say in the PR how you verified it. "It compiles" is not verification.

`detekt` runs with the default rule set on every module as part of the build. There is no ktlint or spotless.

## Project conventions

- **Architecture**: Decompose + Hilt. A screen is a `*Component` with a `Factory` interface plus `onGoBack` / `onNavigate` callbacks.
- **Dependency direction**: `UI / Tool / Component → Service / UseCase / Repository interface → implementation`. UI code must not touch DAOs, Retrofit, Room or the filesystem directly.
- **Adding a screen** touches several files (`Screen.kt`, `ChildProvider.kt`, `NavigationChild.kt`, `settings.gradle.kts`, plus the shell files if it is a top-level screen). Find an existing screen of the same shape and copy that instead of inventing a pattern.
- **Agent tools**: implement `AgentTool` and register it with Hilt via `@Provides @IntoMap @StringKey("tool_name")`. Tools are thin adapters — the logic belongs in the layer below.
- **User-visible strings** must live in string resources. Never hardcode them.
- **Package naming**: new code prefers `com.wanbaohe.*`. `com.t8rin.*` is inherited from upstream ImageToolbox and is deliberately left alone so the diff against upstream stays reviewable.
- **Comments**: Chinese in files that are already Chinese-dense, English elsewhere. Identifiers and commit messages are English either way.
- **Commit messages** follow Conventional Commits: `feat(scope):`, `fix(scope):`, `docs:`, `build(deps):`.

## Pull request process

1. Branch from `main`. Name the branch after what you are fixing.
2. Keep the PR to one concern. A rename plus a behaviour change plus a formatting pass is three PRs.
3. Describe what changed and **how you verified it**.
4. CI builds `:app:assembleFossUniversalDebug` on every PR. A red PR will not be reviewed until it is green.
5. The maintainer is one person in UTC+8. Expect a first response within a few days. If a week passes with no reply, a ping is welcome, not rude.
6. PRs are squash-merged, so do not worry about cleaning up branch history.

Being the first person to touch a file is normal here. Ask questions in the PR rather than guessing.

## What we do not merge

- **Wholesale reformatting or cleanup PRs** that touch hundreds of unrelated lines.
- **Dependency bumps that were not built and run locally.** Dependabot does not get a free pass either.
- **Machine-generated PRs that no human has read.** Using AI tools to write code is fine. Submitting a diff you cannot explain is not.
- **New test suites, CI pipelines or build-system migrations** proposed without discussing them in an issue first.
- **Changes that break the upstream fork relationship** by rewriting inherited `com.t8rin.*` code without a stated reason.
- Code copied from projects whose licence is incompatible with Apache-2.0.

## Database migrations

Group schema changes by app release, not by feature. Starting with release 140, the release's `versionCode` is the target database version. Until 140 ships, append changes to `Release140Migrations` instead of adding another version step. Preserve all published upgrade paths and schemas.

See the [database migration guidelines](docs/database-migrations.en.md) (also available [in Chinese](docs/database-migrations.md)) for baselines and how to verify a migration.

Happy hacking.
