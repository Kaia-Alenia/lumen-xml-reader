#!/bin/sh

set -e

DIRNAME=`dirname "$0"`
APP_HOME=`cd "$DIRNAME" && pwd`
JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$JAR" ]; then
    mkdir -p "$APP_HOME/gradle/wrapper"
    if command -v curl >/dev/null 2>&1; then
        curl -sLo "$JAR" "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar" || true
    elif command -v wget >/dev/null 2>&1; then
        wget -qO "$JAR" "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar" || true
    fi
fi

if [ -f "$JAR" ]; then
    JAVACMD="java"
    if [ -n "$JAVA_HOME" ] ; then
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    exec "$JAVACMD" -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
elif command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    echo "No se encontró gradle ni gradle-wrapper.jar" >&2
    exit 1
fi
