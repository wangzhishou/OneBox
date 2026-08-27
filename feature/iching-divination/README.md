# I Ching Divination

Independent Compose/Decompose feature implementing the casting flow shown in the product prototypes.

## Flow

1. Enter an optional question (up to 100 characters).
2. Tap the action or shake the device to cast six lines with the three-coin method.
3. Open the result directly and review the primary hexagram, changing lines, and changed hexagram.
4. Request a reference interpretation through the app's configured AI engine.

The feature uses the shared Glass components for inputs, controls, cards, history, and hexagram surfaces. Every completed cast is stored in MMKV (newest first, up to 100 records), including the question, bottom-to-top line values, primary/changed hexagram summary, timestamp, and generated AI interpretation. Records can be restored, removed individually, or cleared together.

Lines use values 6–9 and are stored bottom-to-top. AI text is explicitly presented as cultural reference rather than deterministic advice.

## Verification

```bash
./gradlew :feature:iching-divination:testOneboxUniversalDebugUnitTest
./gradlew :feature:app:compileOneboxUniversalDebugKotlin
```

