# Mindustry OptiFine

A lighting and shadow rendering enhancement mod for [Mindustry](https://github.com/Anuken/Mindustry).

## Features

### Real-time Dynamic Shadows

A raycast-based real-time shadow system with configurable quality and visual effects.

- Block and building shadows (including connected wall shadows)
- Unit shadows (with smooth movement updates)
- Prop shadows (with adjustable scale)
- Contact shadows
- Day/night cycle (sun angle drives shadow direction and length)
- Adjustable shadow length, opacity, blur radius, tint, dark fade, etc.
- Three graphics quality presets (Low/Medium/High)
- Chunk-based shadow cache for optimized large-world performance

### Lighting & Effects

- `AltLightBatch` — replaces vanilla light batching with per-vertex glow intensity and liquid glow support
- `InstancedSpriteBatch` / `AltCacheSpriteBatch` — high-performance alternative batch processors
- Liquid glow mapping (auto-calculates glow intensity from liquid color)
- Option to hide vanilla lights, letting the mod's lighting pipeline take over
- Auto-detects glowing regions on blocks/units/weapons and builds replacement mappings

### Connect Walls

- Provides connected textures for walls (similar to Minecraft's connected textures), blending edges automatically when walls are adjacent
- Dynamically updates connection state at runtime
- Auto-packs `-tiled` and `-inner-tiled` textures via `packSprites`

### Building Glow Parts System

- `DrawGlowWrapper` — wraps vanilla DrawBlock with an additional glow render layer
- `GlowHeatPart` / `GlowPart` — adds independent heat/glow effects to turrets, engines, and other parts
- `GlowEngines` — replaces unit engines with emissive glow rendering

### Advanced Camera

- Adjustable zoom/pan sensitivity
- Smoother camera control

### Settings UI

- Comprehensive in-game settings panel covering all feature toggles and parameters
- Shader test dialog for real-time preview of each shader effect
- Mod info panel

## Build

```bash
./gradlew jar          # Desktop only
./gradlew deploy       # Desktop + Android
```

Dependencies: JDK 17, Mindustry v159.3, Arc.

## Architecture

```
MindustryOptiFine/
├── MindustryOptiFine.java  Main entry: glow mapping, texture replacement, settings registration
├── shadow/                 Dynamic shadow system
├── shaders/                Shader wrappers
├── graphics/               Rendering pipeline (batching, bloom, edge, static blocks, connect walls, glow mapping)
├── parts/                  Glow parts system
├── ui/                     Settings and mod UI
├── io/                     Configuration handling
├── gen/                    Generators
├── utils/                  Utilities
└── struct/                 Custom data structures
```
