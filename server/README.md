# Nexo AI Keyboard — Backend Render

Este diretório é o backend oficial do Nexo AI Keyboard.

## Variáveis obrigatórias no Render

- `OPENAI_API_KEY`: chave privada da API OpenAI.
- `APP_TOKEN`: token privado compartilhado entre o app e este backend.

## Variáveis já definidas pelo `render.yaml`

- `OPENAI_MODEL_FAST=gpt-5.6-luna`
- `OPENAI_MODEL_BALANCED=gpt-5.6-terra`
- `OPENAI_MODEL_SMART=gpt-5.6-sol`
- `MAX_CHARS=12000`
- `ALLOWED_ORIGIN=https://nexo-keyboard.page.gd`
- `PUBLIC_SITE_URL=https://nexo-keyboard.page.gd`

A chave OpenAI nunca deve ir para Android, GitHub ou InfinityFree.

Endpoint esperado após criar o Blueprint:

`https://nexo-keyboard-api.onrender.com/api.php`

Se o Render atribuir outro hostname, use o endereço real no aplicativo e também em `htdocs/assets/config.js`.
