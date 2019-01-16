#!/bin/bash

set -e
set -o pipefail

cd "$(dirname "$0")"

clear
./gradlew clean build fatjar -xtest

date
rsync --verbose --progress -z build/libs/LDG-Chess1.jar muksihs@muksihs.com:.
date

exit 0