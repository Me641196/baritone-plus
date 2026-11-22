#!/usr/bin/env bash
# Builds baritoneplus-1.0.0.jar with plain javac + jar (no Gradle).
# Usage: ./build.sh [minecraft_dir] [baritone_api_jar]
set -euo pipefail
cd "$(dirname "$0")"

MC_DIR="${1:-$HOME/.minecraft}"
API="${2:-libs/baritone-api-neoforge-1.11.2.jar}"

[ -d "$MC_DIR/libraries" ] || { echo "No libraries folder in $MC_DIR. Install NeoForge 1.21.1 there first, or pass the right folder."; exit 1; }
[ -f "$API" ] || { echo "Missing $API (see libs/README.txt)"; exit 1; }
command -v javac >/dev/null || { echo "javac not found. Install a JDK 21."; exit 1; }

rm -rf build && mkdir -p build/classes
CP="$API"
while IFS= read -r -d '' j; do CP="$CP:$j"; done < <(find "$MC_DIR/libraries" -name '*.jar' -print0)

find src/main/java -name '*.java' > build/sources.txt
javac --release 21 -encoding UTF-8 -cp "$CP" -d build/classes @build/sources.txt
cp -r src/main/resources/. build/classes/
jar --create --file baritoneplus-1.0.0.jar -C build/classes .
echo "Built baritoneplus-1.0.0.jar"
