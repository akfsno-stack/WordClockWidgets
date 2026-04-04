#!/bin/bash

# Build IPA file script for WordClockWidgets iOS
# Requires: macOS with Xcode installed
# Usage: ./build_ipa.sh

set -e

echo "╔════════════════════════════════════════════════════════╗"
echo "║     WordClockWidgets iOS - IPA Build Script           ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

# Check if running on macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo "❌ ERROR: This script must run on macOS"
    echo "   Current OS: $OSTYPE"
    exit 1
fi

echo "✓ Running on macOS"

# Check Xcode
if ! command -v xcodebuild &> /dev/null; then
    echo "❌ ERROR: Xcode is not installed"
    echo "   Install from: https://apps.apple.com/app/xcode/id497799835"
    exit 1
fi

XCODE_VERSION=$(xcodebuild -version | head -1)
echo "✓ $XCODE_VERSION"

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_NAME="WordClockWidgets"
WORKSPACE="${PROJECT_NAME}.xcworkspace"

# Build settings
BUILD_DIR="${SCRIPT_DIR}/build"
CONFIGURATION="Release"
DEVICE_DESTINATION="generic/platform=ios"

echo ""
echo "════════════════════════════════════════════════════════"
echo "Build Configuration"
echo "════════════════════════════════════════════════════════"
echo ""
echo "Project:        $PROJECT_NAME"
echo "Workspace:      $WORKSPACE"
echo "Configuration:  $CONFIGURATION"
echo "Build Dir:      $BUILD_DIR"
echo ""

# Create build directory
mkdir -p "$BUILD_DIR/DerivedData"

# Step 1: Clean
echo "░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░"
echo "Step 1: Cleaning build artifacts..."
echo "░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░"

xcodebuild \
    -workspace "$SCRIPT_DIR/$WORKSPACE" \
    -scheme "$PROJECT_NAME" \
    -configuration "$CONFIGURATION" \
    -derivedDataPath "$BUILD_DIR/DerivedData" \
    clean 2>&1 | tail -10

echo "✓ Clean complete"
echo ""

# Step 2: Build for device (arm64)
echo "░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░"
echo "Step 2: Building for iOS device (arm64)..."
echo "░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░"

xcodebuild \
    -workspace "$SCRIPT_DIR/$WORKSPACE" \
    -scheme "$PROJECT_NAME" \
    -configuration "$CONFIGURATION" \
    -destination "$DEVICE_DESTINATION" \
    -derivedDataPath "$BUILD_DIR/DerivedData" \
    -arch arm64 \
    CODE_SIGN_IDENTITY="" \
    CODE_SIGNING_REQUIRED=NO \
    CODE_SIGNING_ALLOWED=NO \
    build 2>&1 | tail -20

BUILD_RESULT=$?
if [ $BUILD_RESULT -ne 0 ]; then
    echo "❌ Build failed with exit code: $BUILD_RESULT"
    exit 1
fi

echo "✓ Build complete"
echo ""

# Step 3: Find built app
echo "░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░"
echo "Step 3: Finding compiled app..."
echo "░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░"

APP_PATH=$(find "$BUILD_DIR/DerivedData" -name "${PROJECT_NAME}.app" -type d 2>/dev/null | head -1)

if [ -z "$APP_PATH" ]; then
    echo "❌ ERROR: App bundle not found in build output"
    echo "   Searched: $BUILD_DIR/DerivedData"
    echo ""
    echo "   Trying alternate build method..."
    
    # Try archive method
    ARCHIVE_PATH="$BUILD_DIR/${PROJECT_NAME}.xcarchive"
    
    xcodebuild \
        -workspace "$SCRIPT_DIR/$WORKSPACE" \
        -scheme "$PROJECT_NAME" \
        -configuration "$CONFIGURATION" \
        -destination "$DEVICE_DESTINATION" \
        -archivePath "$ARCHIVE_PATH" \
        -derivedDataPath "$BUILD_DIR/DerivedData" \
        -arch arm64 \
        CODE_SIGN_IDENTITY="" \
        CODE_SIGNING_REQUIRED=NO \
        CODE_SIGNING_ALLOWED=NO \
        archive 2>&1 | tail -20
    
    if [ -d "$ARCHIVE_PATH" ]; then
        APP_PATH="$ARCHIVE_PATH/Products/Applications/${PROJECT_NAME}.app"
        echo "✓ Archive created"
    else
        echo "❌ ERROR: Failed to create app archive"
        exit 1
    fi
fi

if [ ! -d "$APP_PATH" ]; then
    echo "❌ ERROR: App bundle not found at: $APP_PATH"
    exit 1
fi

echo "✓ App found: $APP_PATH"
APP_SIZE=$(du -sh "$APP_PATH" | awk '{print $1}')
echo "  Size: $APP_SIZE"
echo ""

# Step 4: Create IPA structure
echo "░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░"
echo "Step 4: Creating IPA package..."
echo "░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░"

# Prepare IPA structure
PAYLOAD_DIR="$BUILD_DIR/Payload"
rm -rf "$PAYLOAD_DIR"
mkdir -p "$PAYLOAD_DIR"

# Copy app to Payload
cp -r "$APP_PATH" "$PAYLOAD_DIR/"
echo "✓ App copied to Payload directory"

# Create IPA file
IPA_FILE="$BUILD_DIR/${PROJECT_NAME}.ipa"
rm -f "$IPA_FILE"

cd "$BUILD_DIR"
zip -r -q "$IPA_FILE" Payload/
cd - > /dev/null

if [ ! -f "$IPA_FILE" ]; then
    echo "❌ ERROR: Failed to create IPA file"
    exit 1
fi

IPA_SIZE=$(ls -lh "$IPA_FILE" | awk '{print $5}')
echo "✓ IPA file created: ${PROJECT_NAME}.ipa ($IPA_SIZE)"
echo ""

# Step 5: Verify IPA
echo "░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░"
echo "Step 5: Verifying IPA package..."
echo "░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░"

echo ""
echo "IPA Contents:"
unzip -l "$IPA_FILE" | head -15

# Check Info.plist
if unzip -l "$IPA_FILE" | grep -q "Info.plist"; then
    echo "✓ Info.plist found in IPA"
else
    echo "⚠️  Warning: Info.plist not found"
fi

# Check executable
EXECUTABLE_NAME=$(grep -A1 "CFBundleExecutable" "$SCRIPT_DIR/${PROJECT_NAME}/Info.plist" | tail -1 | sed 's/<[^>]*>//g' | xargs)
if [ -n "$EXECUTABLE_NAME" ] && unzip -l "$IPA_FILE" | grep -q "$EXECUTABLE_NAME"; then
    echo "✓ Executable binary found in IPA"
else
    echo "⚠️  Warning: Executable not found (may be embedded in .app)"
fi

echo ""

# Step 6: Final summary
echo "════════════════════════════════════════════════════════"
echo "✅ Build Complete!"
echo "════════════════════════════════════════════════════════"
echo ""
echo "📱 IPA File Location:"
echo "   $IPA_FILE"
echo ""
echo "📊 File Size: $IPA_SIZE"
echo ""
echo "════════════════════════════════════════════════════════"
echo "Installation Methods"
echo "════════════════════════════════════════════════════════"
echo ""
echo "1️⃣  Using Xcode:"
echo "   - Open Xcode"
echo "   - Menu: Window → Devices and Simulators"
echo "   - Select your iPhone"
echo "   - Drag and drop the IPA file"
echo ""
echo "2️⃣  Using Apple Configurator 2:"
echo "   - Open Apple Configurator 2 on Mac"
echo "   - Connect iPhone"
echo "   - Drag the IPA file to device"
echo ""
echo "3️⃣  Using iTunes (if available):"
echo "   - Connect iPhone"
echo "   - Open iTunes/Music"
echo "   - Drag IPA to the device"
echo ""
echo "4️⃣  Using iPad Air (if available as service):"
echo "   - Use Alt Server or similar tools"
echo ""
echo "════════════════════════════════════════════════════════"
echo ""
echo "For device installation, you may need:"
echo "  • Apple ID"
echo "  • Trust profile on device: Settings > General > Device Management"
echo "  • OR use Xcode with Apple Developer account"
echo ""
