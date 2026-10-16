#!/usr/bin/env bash
# Re-records the Roborazzi reference PNGs in app/src/test/screenshots/.
# Only run this for an intended UI change, then review and commit the PNGs.
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh

./gradlew --no-daemon --console=plain recordRoborazziDebug
