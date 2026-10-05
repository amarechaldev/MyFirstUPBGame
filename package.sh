#!/usr/bin/env bash
# Packages the game as a self-contained application in dist/, with its own
# trimmed Java runtime, so players don't need Java installed.
# Builds MyFirstUPBGame.jar first (see build.sh), then runs jpackage and zips
# the result. The app only runs on the OS it was built on: build the Windows
# version on Windows and the macOS version on a Mac.
set -euo pipefail

cd "$(dirname "$0")"

APP_NAME="MyFirstUPBGame"
MAIN_CLASS="base.LaunchMyFirstUPBGame"
JAR="MyFirstUPBGame.jar"
MODULES="java.base,java.desktop"
INPUT_DIR="build/jpackage-input"
DIST_DIR="dist"

for tool in jpackage jar; do
    if ! command -v "$tool" > /dev/null 2>&1; then
        echo "Error: '$tool' not found. Install a JDK (17 or newer) and add it to the PATH." >&2
        exit 1
    fi
done

./build.sh

echo "Cleaning previous package..."
rm -rf "$DIST_DIR" build
mkdir -p "$INPUT_DIR"
cp "$JAR" "$INPUT_DIR/"

echo "Creating the application with a bundled Java runtime..."
jpackage --type app-image \
    --name "$APP_NAME" \
    --input "$INPUT_DIR" \
    --main-jar "$JAR" \
    --main-class "$MAIN_CLASS" \
    --add-modules "$MODULES" \
    --jlink-options "--strip-debug --no-header-files --no-man-pages" \
    --dest "$DIST_DIR"

rm -rf build

echo "Zipping..."
case "$(uname -s)" in
    Darwin)
        ZIP="$DIST_DIR/$APP_NAME-macos.zip"
        ditto -c -k --keepParent "$DIST_DIR/$APP_NAME.app" "$ZIP"
        ;;
    MINGW*|MSYS*|CYGWIN*)
        ZIP="$DIST_DIR/$APP_NAME-windows.zip"
        jar --create --no-manifest --file "$ZIP" -C "$DIST_DIR" "$APP_NAME"
        ;;
    *)
        ZIP="$DIST_DIR/$APP_NAME-linux.tar.gz"
        tar -czf "$ZIP" -C "$DIST_DIR" "$APP_NAME"
        ;;
esac

echo "Done: $ZIP"
echo "Share this file. Players unzip it and launch $APP_NAME (no Java needed)."
