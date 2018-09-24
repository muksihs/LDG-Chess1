#!/bin/bash
set -e
set -o pipefail

if [ -z "$STY" ]; then exec screen -dm /bin/bash "$0"; fi

cd "$(dirname "$0")"

rm LDG-Chess1-*.log || true
date="$(date +%Y-%m-%d_%H-%M-%S)"
exec &> >(tee -a "LDG-Chess1-${date}.log")

echo "===";
echo "--- $(date): Run Game";
java -jar ./LDG-Chess1.jar --auth-file ${HOME}/.steem/far-horizons.properties

echo "--- $(date): Done";

