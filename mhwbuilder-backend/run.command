#!/bin/bash

DIR="$(cd "$(dirname "$0")" && pwd)"

export JAVA_HOME="$DIR/runtime"
export PATH="$JAVA_HOME/bin:$PATH"

"$JAVA_HOME/bin/java" -jar "$DIR/app.jar"