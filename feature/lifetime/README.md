# Lifetime Feature Module

This module implements the "时光里程碑 (LifeTime)" feature - a sophisticated life time calculator that visualizes your life journey.

## Features

### Tab A: 回望 (Past)
- Displays time elapsed since birth in years, months, days, hours, minutes, and seconds
- Shows count of important festivals celebrated (Spring Festival, Mid-Autumn, Christmas)
- Animated rolling numbers for dynamic data visualization

### Tab B: 只争朝夕 (Seize the Day)
- Countdown of remaining lifetime (assuming 100-year lifespan by default)
- Battery-style circular progress bar showing life progress percentage
- Displays remaining Spring Festivals and sunrises
- Creates urgency atmosphere with error-tinted colors

## Architecture

```
feature/lifetime/
├── component/          # Decompose component (business logic)
├── data/              # Repository layer (DataStore)
├── domain/            # Domain logic (Calculator)
├── screen/            # UI screens
└── ui/                # Reusable UI components
```

## Key Components

- **LifeTimeCalculator**: Core calculation logic for time differences and festival counts
- **LifeTimeRepository**: DataStore-based persistence for birth date
- **LifeTimeComponent**: Decompose component managing state and real-time updates
- **TimeCard**: Animated time display component with rolling number effect
- **LifeProgressBar**: Circular battery-style progress indicator

## Technical Highlights

- ✅ Inherits from `BaseScreen`
- ✅ Uses `AppTheme.colors` for all colors (no hardcoded values)
- ✅ Implements `rememberLocalEssentials()` for toasts
- ✅ Real-time countdown with 1-second refresh rate
- ✅ Smooth tab switching animations
- ✅ Immutable data classes for Compose optimization
- ✅ Clean Architecture with domain/data/presentation layers

