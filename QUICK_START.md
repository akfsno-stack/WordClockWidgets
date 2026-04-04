curl -O https://download.swift.org/swiftly/linux/swiftly-$(uname -m).tar.gz && \
tar zxf swiftly-$(uname -m).tar.gz && \
./swiftly init --quiet-shell-followup && \
. "${SWIFTLY_HOME_DIR:-$HOME/.local/share/swiftly}/env.sh" && \
hash -r# ⚡ Quick Start - Download Your iOS App

## 🎯 Fastest Way to Get the App

### Step 1: Trigger Build (90 seconds)
```bash
# Go to your local repo
cd WordClockWidgets

# Push to GitHub
git push origin ios-port
```

### Step 2: Wait for Build (2-5 minutes)
Go to: **https://github.com/ideoma-last/WordClockWidgets/actions**

Watch the "Build iOS App" workflow complete.

### Step 3: Download Artifacts (30 seconds)
1. Click on the workflow run
2. Scroll to **Artifacts** section
3. Click **ios-build-artifacts** to download

### Step 4: Use on Mac (1 minute)
```bash
# Extract
unzip ios-build-artifacts.zip

# Open in Xcode
cd ios
open WordClockWidgets.xcworkspace

# Build and test
# - Select iPhone XS simulator
# - Press Cmd+B to build
# - Press Cmd+R to run
```

---

## 📦 Alternative: Direct Download from Main Branch

If you've pushed to main:

```bash
# Clone with iOS branch
git clone https://github.com/ideoma-last/WordClockWidgets.git
cd WordClockWidgets
git checkout ios-port

# Open in Xcode
cd ios
open WordClockWidgets.xcworkspace
```

Then:
- Cmd+B to build
- Cmd+R to run on simulator
- Connect device and run on iPhone XS

---

## 🚀 Create a Release

To create a downloadable Release:

### Via GitHub Web (Easiest):
1. Go to **Releases** page
2. Click **Create a new release**
3. Tag: `v1.0.0`
4. Title: `WordClockWidgets iOS v1.0.0`
5. Add description
6. **Attach files:**
   - Download ios-build-artifacts.zip from Actions
   - Upload to release
7. Publish

### Via Command Line:
```bash
# Create tag
git tag v1.0.0

# Push to GitHub
git push origin v1.0.0

# Done! Release auto-created on GitHub
```

---

## ✅ What's Ready

| Component | Status | Download |
|-----------|--------|----------|
| iOS App | ✅ Ready | Actions tab → artifacts.zip |
| Xcode Project | ✅ Ready | ios/WordClockWidgets.xcworkspace |
| Widget | ✅ Ready | Included in app bundle |
| Documentation | ✅ Ready | 📚 See iOS folder |
| Build Pipeline | ✅ Active | Auto-builds on push |

---

## 📱 To Install on iPhone XS

1. Download and extract artifacts
2. Open ios/WordClockWidgets.xcworkspace
3. Connect iPhone XS via USB
4. Select device
5. Press Cmd+R
6. Approve on device → Ready to use!

See [ios/DEVICE_INSTALLATION.md](ios/DEVICE_INSTALLATION.md) for details.

---

## 🔗 Quick Links

- **Automated Builds:** https://github.com/ideoma-last/WordClockWidgets/actions
- **Releases:** https://github.com/ideoma-last/WordClockWidgets/releases
- **Build Guide:** [ios/BUILD_GUIDE.md](ios/BUILD_GUIDE.md)
- **Device Setup:** [ios/DEVICE_INSTALLATION.md](ios/DEVICE_INSTALLATION.md)
- **Full Documentation:** [GITHUB_ACTIONS_GUIDE.md](GITHUB_ACTIONS_GUIDE.md)

---

✅ **Your iOS app is ready to download and install!**
