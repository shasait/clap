#!/bin/bash

cd "$(dirname "${BASH_SOURCE[0]}")"

############################################################

export SDKMAN_DIR="${SDKMAN_DIR:-${HOME}/.sdkman}"
if [ -s "${SDKMAN_DIR}/bin/sdkman-init.sh" ]; then
    . "${SDKMAN_DIR}/bin/sdkman-init.sh"
    sdk env install
fi

mvn clean package
