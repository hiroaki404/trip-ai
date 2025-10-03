# CLAUDE.md

このファイルは、このリポジトリ内のコードを扱う際のClaude Code (claude.ai/code)へのガイダンスを提供します。

## プロジェクト概要

Kotlin Multiplatformプロジェクトで、Compose Multiplatformを使用してAndroidとDesktop (JVM)をターゲットにしています。このアプリケーションは、共通のUIとビジネスロジックが`commonMain`に存在し、プラットフォーム固有の実装がexpect/actualパターンを使用する、共有コードベースアーキテクチャを使用しています。

## 必須コマンド

### ビルド
```bash
# プロジェクト全体のビルド
./gradlew :composeApp:build

# Android Debug APK
./gradlew :composeApp:assembleDebug

# Desktop (JVM) JAR
./gradlew :composeApp:jvmJar
```

### 実行
```bash
# Desktopアプリケーションの実行
./gradlew :composeApp:run

# Androidデバイス/エミュレーターへのインストール
./gradlew :composeApp:installDebug
```

### テスト
```bash
# 全プラットフォームでの全テストの実行
./gradlew :composeApp:allTests

# チェックの実行（テストを含む）
./gradlew :composeApp:check

# JVMテストのみの実行
./gradlew :composeApp:jvmTest
```

### クリーン
```bash
./gradlew :composeApp:clean
```

## アーキテクチャ

### Multiplatform構造
- **composeApp/src/commonMain**: 全プラットフォーム向けの共有コード
  - Material 3を使用したCompose UI定義
  - ビジネスロジックとデータモデル
  - `composeResources/`内の共有リソース

- **composeApp/src/androidMain**: Android固有の実装

- **composeApp/src/jvmMain**: Desktop固有の実装
  - エントリーポイント: `main.kt`、メインクラスは`MainKt`

### Platform Abstractionパターン
プラットフォーム固有の機能にはexpect/actualパターンを使用します。
- `commonMain/Platform.kt`で`expect`宣言を定義します
- プラットフォーム固有のモジュール（例：`Platform.jvm.kt`）で`actual`宣言を実装します

### 依存関係管理
すべてのライブラリバージョンは、Gradle Version Catalogsを使用して`gradle/libs.versions.toml`で管理されます。ビルドファイルで`libs.*`表記を使用して依存関係を参照します。

### 主要テクノロジー
- Kotlin 2.2.20 (JVM Target: 11)
- Compose Multiplatform 1.9.0
- Material 3（デザインシステム用）
- Kotlinx Coroutines 1.10.2

## コーディング規約

### Compose UI
- すべてのComposable関数は`@Composable`アノテーションを使用します
- プレビュー可能なコンポーネントには`@Preview`を使用します
- ルートComposableを`MaterialTheme`でラップします
- セーフエリアの処理には`safeContentPadding()`修飾子を適用します

### 状態管理
`remember { mutableStateOf() }`パターンを`by`デリゲートで使用します。
```kotlin
var state by remember { mutableStateOf(initialValue) }
```

### リソース
生成されたコードを介して共有リソースにアクセスします。
```kotlin
import trip_ai.composeapp.generated.resources.Res
import trip_ai.composeapp.generated.resources.resource_name
```

## 開発ワークフロー

新機能を実装する場合:
1. `commonMain`に共有ロジック/UIを追加します
2. プラットフォーム固有のAPIにはexpect/actualを使用します
3. 互換性を検証するために両方のプラットフォームでビルドします
4. テストを実行します: `./gradlew :composeApp:allTests`
5. 両方のプラットフォームをテストします: `./gradlew :composeApp:run`とAndroidデバイス/エミュレーター
