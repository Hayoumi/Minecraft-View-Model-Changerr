# ViewModel Customizer


A minimalist Open Source Fabric mod for Minecraft 1.21.4+ that gives you complete control over your first-person viewmodel.

I'll make a mod for other versions too (prob)!

New config + autosave system soon 😱

## ✨ Features

### 🎮 Full Customization
- **Position** - Move your viewmodel on X, Y, and Z axes
- **Rotation** - Adjust Yaw, Pitch, and Roll independently
- **Scale** - Resize your held items from tiny to massive
- **Precise Control** - All sliders support 0.05 step increments

### 🎯 Animation Control
- **No Swing** - Prevents your item from visually swinging (keeps rotation animations)
- **Scale Swing** - Scale swing animations with your custom size
- **No Equip Animation** - Removing by default the pulling-out animation when switching items

### 🎨 Clean UI
- Minimalist black & white design
- Per-setting reset buttons (⟲)
- Reset all button for convenience
- Real-time preview while adjusting

## 🎮 Usage

- Press `V` to open the ViewModel editor (configurable in keybinds)
- Adjust sliders to customize your viewmodel
- Click ⟲ to reset individual settings
- Click "Reset All" to restore defaults
- Settings save automatically on close

## 🔧 Configuration

All settings are stored in `.minecraft/config/viewmodel-customizer.json` and persist between sessions(soon).

## 📸 Preview

![Viewmodel Customizer 1.0.0](https://cdn.modrinth.com/data/cached_images/e10d380bdc8361d6abece53d593e7e52a32c330d_0.webp)

## 🛠️ Building from Source

```bash
git clone https://github.com/yourusername/viewmodel-customizer.git
cd viewmodel-customizer
./gradlew build
