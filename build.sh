#!/usr/bin/env bash
# Builds an executable jar (MyFirstUPBGame.jar) at the project root.
# The jar bundles the game classes, the resources (images, sounds) and the
# classes of the libraries in lib/. No source code is included.
# Run it with: java -jar MyFirstUPBGame.jar   (requires Java 8 or newer)
set -euo pipefail

cd "$(dirname "$0")"

MAIN_CLASS="base.LaunchMyFirstUPBGame"
JAVA_RELEASE="8"
BUILD_DIR="build"
STAGING_DIR="$BUILD_DIR/classes"
JAR="MyFirstUPBGame.jar"

for tool in javac jar; do
    if ! command -v "$tool" > /dev/null 2>&1; then
        echo "Error: '$tool' not found. Install a JDK (9 or newer) and add it to the PATH." >&2
        exit 1
    fi
done

echo "Cleaning previous build..."
rm -rf "$BUILD_DIR" "$JAR"
mkdir -p "$STAGING_DIR"

echo "Compiling sources (Java $JAVA_RELEASE)..."
find src -name '*.java' > "$BUILD_DIR/sources.txt"
javac --release "$JAVA_RELEASE" -Xlint:-options -encoding UTF-8 -cp "lib/*" -d "$STAGING_DIR" @"$BUILD_DIR/sources.txt"

echo "Copying resources..."
cp -r resources/. "$STAGING_DIR/"

echo "Bundling libraries..."
for lib in lib/*.jar; do
    (cd "$STAGING_DIR" && jar xf "../../$lib")
done
rm -rf "$STAGING_DIR/META-INF"
find "$STAGING_DIR" -name '*.java' -delete

echo "Packaging $JAR..."
jar cfe "$JAR" "$MAIN_CLASS" -C "$STAGING_DIR" .

rm -rf "$BUILD_DIR"

echo "Done: $JAR"
echo "Run it with: java -jar $JAR"
