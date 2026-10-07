# I Ching Divination

Independent Compose/Decompose feature implementing the casting flow shown in the product prototypes.

## Flow

1. Enter an optional question (up to 100 characters).
2. Tap the action or shake the device to cast six lines with the three-coin method.
3. Open the result directly and review the primary hexagram, changing lines (marked with ○/×), the built-in judgment and changing-line texts, and the changed hexagram diagram.
4. Request a reference interpretation through the app's configured AI engine; the estimated points cost is shown next to the entry button.

The feature uses the shared Glass components for inputs, controls, cards, history, and hexagram surfaces. Every completed cast is stored in MMKV (newest first, up to 100 records), including the question, bottom-to-top line values, primary/changed hexagram numbers, timestamp, and generated AI interpretation. Hexagram names, judgments, and line texts are not persisted — they are resolved at display time from `res/raw/iching_texts.json` (English) / `res/raw-zh-rCN/iching_texts.json` (Chinese) via `IChingTextLibrary`, so both the UI and the AI prompt follow the current locale. Records can be restored, removed individually, or cleared together.

Lines use values 6–9 and are stored bottom-to-top. AI text is explicitly presented as cultural reference rather than deterministic advice. Markdown is rendered via the shared `StreamingMarkdownContent` (core/base), which stays in Markdown during streaming: parsing runs off the main thread with `collectLatest` backpressure, and unchanged top-level blocks reuse their previous AST node references by content signature so only the growing tail block recomposes.

## Verification

```bash
./gradlew :feature:iching-divination:testOneboxArm64DebugUnitTest
./gradlew :feature:app:compileOneboxArm64DebugKotlin
```

## Data source

`res/raw*/iching_texts.json` — the classical Chinese judgment/line texts are public domain; the structured source was cross-checked against the MIT-licensed [freizl/yijing](https://github.com/freizl/yijing) dataset. English texts are original faithful translations.

