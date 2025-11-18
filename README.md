# Minecraft View Model Customizer

A lightweight client-side mod for tweaking how your first-person hands and held items look in Minecraft.

## Features
- Open the view model menu with **V** (rebindable) to adjust scale, position (X/Y/Z), and rotation (Yaw/Pitch/Roll).
- Animation options to disable vanilla swing or scale the swing animation for finer control.
- Instant feedback while editing so you can see tweaks in-game as you slide or toggle.

## Profile system
- The mod keeps multiple named profiles stored as JSON files in `config/viewmodel/configs/` with the active profile tracked in `config/viewmodel/active.txt`.
- A protected **Default** profile is regenerated if missing and cannot be deleted or renamed.
- Every change made in the UI auto-saves to the active profile, preserving your adjustments without extra clicks.
- Create new profiles, rename them, or delete unused ones directly from the config card; new profiles are created without switching away from your current active profile.
- Use the dropdown in the left config card to pick any saved profile instantly.

## Usage
1. Launch the game and press **V** to open the View Model screen.
2. Use the profile dropdown to select a saved configuration.
3. Rename the active profile or create/delete extra profiles as needed.
4. Drag sliders to tune scale, position, and rotation; toggle animation options for swing behavior.
5. Hit **Reset All** if you want to revert the current profile to defaults.

## Building
From the repository root, run:
```bash
./gradlew build --console=plain --no-daemon
```
