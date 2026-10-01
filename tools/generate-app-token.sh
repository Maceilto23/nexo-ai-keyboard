#!/usr/bin/env sh
set -eu
if command -v openssl >/dev/null 2>&1; then
  TOKEN="$(openssl rand -hex 32)"
else
  TOKEN="$(python3 -c 'import secrets; print(secrets.token_hex(32))')"
fi
printf 'APP_TOKEN gerado:\n%s\n\nUse o MESMO token no Render e no app Nexo AI.\n' "$TOKEN"
