#!/usr/bin/env bash
# The single SDLC check: unit tests, screenshot verification, lint, debug APK.
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh

./gradlew --no-daemon --console=plain \
  testDebugUnitTest verifyRoborazziDebug lintDebug assembleDebug
