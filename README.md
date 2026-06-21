# Create: Turbine

Privates, lokales Minecraft-Mod (NeoForge 1.21.1) als Create-Addon.
Fügt einen Block **Wasserturbine** (`createturbine:water_turbine`) hinzu, der sich wie
ein Create-Generator ins Rotations-/Stress-Netzwerk einklinkt. Seine Leistung skaliert
mit dem **Wasserdruck** = Höhe der zusammenhängenden Wassersäule über/schräg über dem Block.

- Mehr Höhe → höhere Drehzahl (RPM) **und** höhere Stress-Kapazität (SU/RPM).
  Effektive Leistung (≈ Kapazität × Drehzahl) wächst dadurch stark mit der Tiefe.
- Die Welle kommt aus der **Blickrichtung** beim Platzieren (dort Welle/Zahnrad anschließen).
- Druck wird alle ~1 s neu berechnet (Flood-Fill nach oben, gedeckelt gegen Lag).

## Entwicklung / Test

Voraussetzung: Internet (lädt JDK 21 als Gradle-Toolchain automatisch). Es muss **kein**
JDK 21 vorinstalliert sein; Gradle läuft auch unter JDK 17.

```bash
./gradlew compileJava      # nur kompilieren
./gradlew runClient        # Minecraft-Dev-Client mit Create + diesem Mod starten
./gradlew build            # fertiges Jar nach build/libs/ bauen
```

### Im Spiel testen
1. Kreativ-Welt, Wasserturbine platzieren, Wasser darüber stapeln.
2. Welle/Zahnrad an die Blickrichtungs-Seite anschließen.
3. Mit Ingenieursbrille (Goggles) / Stressometer RPM & SU ablesen:
   - 1 Block Wasser → niedrig, hohe Säule → deutlich mehr RPM **und** SU, kein Wasser → 0.

## Einbau in die echte (CurseForge-)Welt
1. `./gradlew build` → Jar aus `build/libs/`.
2. Jar in den `mods`-Ordner der CurseForge-Instanz kopieren.
3. **Wichtig:** Create- und NeoForge-Version der Instanz müssen passen
   (gebaut gegen Create `6.0.10-280`, NeoForge `21.1.234`, MC `1.21.1`;
   akzeptierter Create-Bereich: `[6.0,6.1)`). Bei Abweichung Versionen in
   `gradle.properties` anpassen und neu bauen.

## Balancing
Alle Tunables stehen als Konstanten in
`src/main/java/moritz/createturbine/content/WaterTurbineBlockEntity.java`
(`BASE_RPM`, `RPM_PER_BLOCK`, `MAX_RPM`, `BASE_CAPACITY`, `CAP_PER_BLOCK`, `RECALC_RATE`).

## Spätere Ausbaustufen
- Volumen-Term aktivieren (`PressureResult.volume` wird bereits mitberechnet) in
  `getGeneratedSpeed` / `calculateAddedStressCapacity`.
- Eigenes Modell/Textur + rotierende Welle (BlockEntityRenderer), ggf. Ponder-Szene.
- Tunables in eine NeoForge-Config auslagern.
