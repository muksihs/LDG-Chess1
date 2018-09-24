#!/bin/bash

set -e
set -o pipefail

cd "$(dirname "$0")"

clear
gradle clean build fatjar -xtest

date
scp build/libs/LDG-Chess1.jar muksihs@muksihs.com:.
date

exit 0