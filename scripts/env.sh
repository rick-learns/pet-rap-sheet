# Sourced by the scripts in this directory. Uses JAVA_HOME (JDK 21) and ANDROID_HOME when set,
# otherwise fixed toolchain paths so the scripts work from a bare systemd environment with a
# minimal PATH.
export JAVA_HOME="${JAVA_HOME:-$HOME/.local/jdk-21}"
export ANDROID_HOME="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/.local/android-sdk}}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:${PATH:-/usr/bin:/bin}"

for tool in "$JAVA_HOME/bin/java" "$ANDROID_HOME/platforms"; do
  if [ ! -e "$tool" ]; then
    echo "missing toolchain: $tool" >&2
    exit 2
  fi
done
