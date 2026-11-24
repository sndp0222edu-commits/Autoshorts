# AntiGem 2D - Antigravity Mobile App (Clone)

Ek lightweight native Android app jo AntiGem ke sleek design aur limited features (**2D Animation using Python** aur **Antigravity Physics**) par focused hai.

---

## 🚀 Features

- **AntiGem Dark Native UI:** Sleek minimalist interface matching AntiGem (`#0E1117`).
- **Antigravity & Zero-G Physics:**
  - Real-time zero-gravity floating particles and rigid bodies.
  - Interactive touch repulsion (Anti-G) and attraction (Gravity Vortex).
  - Phone accelerometer sensor support: Device ko tilt karne se physical gravity shift hoti hai.
- **Python 2D Animation Engine (Offline):**
  - Live Python script execution console.
  - Classic `import antigravity` XKCD easter egg (flying stickman animation).
  - Support for mathematical particle waves, orbital vortices, and supernova bursts.
- **Android Haptic Feedback:** Physical vibration effects on touch and particle interaction.

---

## 📦 How to Build the APK (1-Click via GitHub Actions)

Is project me **Automated GitHub Actions Workflow** pehle se set up hai:

1. **GitHub par naya repository banayein** (e.g. `antigem-mini`).
2. Isi folder ko GitHub par push kar dein:
   ```bash
   cd /data/user/0/ai.antigem/files/home/.gemini/antigravity-cli/scratch/antigem-mini
   git init
   git add .
   git commit -m "Initial AntiGem 2D App"
   git remote add origin https://github.com/YOUR_USERNAME/antigem-mini.git
   git branch -M main
   git push -u origin main
   ```
3. **Download APK:**
   - GitHub repo open karein -> **Actions** tab par jayein.
   - `Build AntiGem 2D APK` workflow run hoga.
   - 2-3 minute me **Artifacts** section me `AntiGem-Mini-2D-Debug-APK` mil jayega.
   - Download karke apne phone me direct install kar lijiye!

---

## 📁 Project Structure

```
antigem-mini/
├── .github/workflows/
│   └── build-apk.yml            # Automated GitHub Actions APK Compiler
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml  # Native App Manifest & Permissions
│   │   ├── java/com/antigem/mini/
│   │   │   ├── MainActivity.java    # Native Android WebView & Tilt Sensor
│   │   │   └── WebAppInterface.java # Android Haptics & System Bridge
│   │   └── assets/
│   │       ├── index.html       # AntiGem Interface
│   │       ├── app.js           # 2D Antigravity Canvas & Physics Loop
│   │       ├── python-engine.js # Python 2D Animation Runtime
│   │       └── style.css        # AntiGem Dark Theme Styling
│   └── build.gradle             # Android Module Configuration
├── build.gradle                 # Project Gradle Build Script
├── settings.gradle
└── gradlew                      # Gradle Wrapper Script
```
