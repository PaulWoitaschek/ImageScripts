#!/bin/bash

set -e

echo "$@"
./gradlew --quiet ":installDist"
"./build/install/android_images/bin/android_images" "$@"
