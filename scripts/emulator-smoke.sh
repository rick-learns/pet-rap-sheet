#!/usr/bin/env bash
# Optional worker evidence, NOT part of the check: boots the prs-pixel AVD
# headless, installs the debug APK, launches it and saves a screenshot to
# smoke/screen.png. Needs a writable /dev/kvm. Build first with scripts/check.sh
# (never run Gradle and the emulator at the same time).
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh

if [ ! -w /dev/kvm ]; then
  echo "/dev/kvm is not writable; emulator smoke test unavailable" >&2
  exit 3
fi
apk=app/build/outputs/apk/debug/app-debug.apk
[ -f "$apk" ] || { echo "missing $apk; run scripts/check.sh first" >&2; exit 2; }

mkdir -p smoke
emulator -avd prs-pixel -no-window -no-audio -gpu swiftshader_indirect \
  -no-snapshot -no-boot-anim >smoke/emulator.log 2>&1 &
emu_pid=$!
cleanup() {
  adb emu kill >/dev/null 2>&1 || true
  sleep 5
  kill "$emu_pid" >/dev/null 2>&1 || true
}
trap cleanup EXIT

adb wait-for-device
for _ in $(seq 1 180); do
  [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ] && break
  sleep 2
done
[ "$(adb shell getprop sys.boot_completed | tr -d '\r')" = 1 ] || { echo "emulator did not boot" >&2; exit 1; }
echo "boot_completed=1"

adb install -r "$apk"
adb shell am start -W -n app.petrapsheet/.MainActivity
sleep 3
adb exec-out screencap -p >smoke/screen.png
echo "screenshot: smoke/screen.png"
