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

# Prints the path of the JDK's bin folder that contains jpackage, or nothing.
# The PATH often only holds launchers for java/javac (Oracle's "javapath" on
# Windows, /usr/bin on macOS) without jpackage, so other places are tried too.
find_jdk_bin() {
    local candidates=()
    if [ -n "${JAVA_HOME:-}" ]; then
        local java_home_env="$JAVA_HOME"
        command -v cygpath > /dev/null 2>&1 && java_home_env="$(cygpath -u "$java_home_env")"
        candidates+=("$java_home_env/bin")
    fi
    if command -v jpackage > /dev/null 2>&1; then
        candidates+=("$(dirname "$(command -v jpackage)")")
    fi
    if command -v java > /dev/null 2>&1; then
        local java_home
        java_home="$(java -XshowSettings:properties -version 2>&1 \
            | sed -n 's/^ *java\.home = //p' | tr -d '\r' | head -n 1 || true)"
        if [ -n "$java_home" ]; then
            command -v cygpath > /dev/null 2>&1 && java_home="$(cygpath -u "$java_home")"
            candidates+=("$java_home/bin")
        fi
    fi
    if [ -x /usr/libexec/java_home ]; then
        local mac_home
        mac_home="$(/usr/libexec/java_home -v 14+ 2>/dev/null || true)"
        [ -n "$mac_home" ] && candidates+=("$mac_home/bin")
    fi

    local dir
    for dir in ${candidates[@]+"${candidates[@]}"}; do
        if [ -x "$dir/jpackage" ] || [ -x "$dir/jpackage.exe" ]; then
            echo "$dir"
            return
        fi
    done
}

JDK_BIN="$(find_jdk_bin)"
if [ -z "$JDK_BIN" ]; then
    echo "Error: 'jpackage' not found. It comes with JDK 14 or newer (17+ recommended)." >&2
    if command -v java > /dev/null 2>&1; then
        echo "The Java found on the PATH is:" >&2
        java -version 2>&1 | head -n 1 | sed 's/^/    /' >&2
    fi
    echo "Install a recent JDK, then point JAVA_HOME to it, for example:" >&2
    echo '    export JAVA_HOME="/c/Program Files/Java/jdk-21"' >&2
    exit 1
fi
JPACKAGE="$JDK_BIN/jpackage"
if [ -x "$JDK_BIN/jar" ] || [ -x "$JDK_BIN/jar.exe" ]; then
    JAR_TOOL="$JDK_BIN/jar"
else
    JAR_TOOL="jar"
fi
echo "Using jpackage: $JPACKAGE"
# So build.sh uses the same JDK (the PATH may only have java/javac launchers).
export PATH="$JDK_BIN:$PATH"

# --jlink-options only exists from JDK 16. Older jpackage (14, 15) already
# strips debug info, header files and man pages by default.
JPACKAGE_VERSION="$("$JPACKAGE" --version 2>/dev/null | grep -Eo '^[0-9]+' | head -n 1 || true)"
JPACKAGE_OPTS=()
if [ -z "$JPACKAGE_VERSION" ] || [ "$JPACKAGE_VERSION" -ge 16 ]; then
    JPACKAGE_OPTS+=(--jlink-options "--strip-debug --no-header-files --no-man-pages")
fi

./build.sh

echo "Cleaning previous package..."
rm -rf "$DIST_DIR" build
mkdir -p "$INPUT_DIR"
cp "$JAR" "$INPUT_DIR/"

echo "Creating the application with a bundled Java runtime..."
"$JPACKAGE" --type app-image \
    --name "$APP_NAME" \
    --input "$INPUT_DIR" \
    --main-jar "$JAR" \
    --main-class "$MAIN_CLASS" \
    --add-modules "$MODULES" \
    ${JPACKAGE_OPTS[@]+"${JPACKAGE_OPTS[@]}"} \
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
        "$JAR_TOOL" --create --no-manifest --file "$ZIP" -C "$DIST_DIR" "$APP_NAME"
        ;;
    *)
        ZIP="$DIST_DIR/$APP_NAME-linux.tar.gz"
        tar -czf "$ZIP" -C "$DIST_DIR" "$APP_NAME"
        ;;
esac

echo "Done: $ZIP"
echo "Share this file. Players unzip it and launch $APP_NAME (no Java needed)."
