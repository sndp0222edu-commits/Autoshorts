# AutoShorts AI - Android Mobile App (Embedded Core Engine)

AutoShorts AI - Complete native Android application featuring embedded Termux runtime, autonomous 2D animation rendering pipeline, Antigravity physics, and Claude skill plugin marketplace.

---

## 🚀 Key Features

1. **Embedded Termux Core Engine (Port 4500):**
   - Built directly into the Android application process via `EmbeddedServer.kt`.
   - **No external Termux required!** The app runs autonomously without needing external scripts.
   - Interactive bash shell console with command execution (`uptime`, `python`, `ls`, `free`, `clear`).
   - Live system metrics chips (`Node: v26 (Embedded)`, `FFmpeg: Ready`, `Disk: Storage OK`).

2. **Autonomous 2D Animation & Antigravity Pipeline:**
   - Step-by-step pipeline tracker: `Script -> Voice -> 2D Canvas Animation -> Antigravity Physics -> Render MP4`.
   - Real-time terminal log stream during video generation.
   - AI Auto-fix and retry capabilities.

3. **Templates & 62 Plugin Marketplace:**
   - **Antigravity 2D Animation** (Zero-G Floating Physics & Python XKCD).
   - **Paper Atlas** (Cut Paper History, Ancient Battles, Empires & Maps).
   - **Neon Line 2D** (Continuous Cybernetic Glowing Line Art).
   - **Tall Tale 3D**, **Deep Dive 3D**, **Inside Out**, and 56+ Over-The-Air Plugins.
   - 10 Curated viral trending topics per style with 1-click script building.

4. **Script Writing Studio & AI Narration:**
   - AI-powered viral 50-second short script generator with offline fallback.
   - Editable script studio with duration, word count and coin deduction.

---

## 🛠️ Build & Architecture

- **Platform:** Native Android (Kotlin + Jetpack Compose)
- **Embedded Engine:** HTTP Core Server on `127.0.0.1:4500`
- **Build System:** Gradle with Automated GitHub Actions Workflow
