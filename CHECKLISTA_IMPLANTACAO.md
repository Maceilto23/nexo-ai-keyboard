# Checklist de implantação — Nexo AI Keyboard 3.1

Siga nesta ordem.

## ETAPA 1 — GitHub

Suba para um repositório novo todo o conteúdo desta pasta `NexoAI_Keyboard_2026`.

O GitHub deve conter:

- `.github/`
- `android/`
- `server/`
- `htdocs/`
- `tools/`
- `render.yaml`
- `.gitignore`
- `README.md`
- `SECURITY.md`

Não envie chave OpenAI, APP_TOKEN real nem arquivo `.jks`.

## ETAPA 2 — Criar APP_TOKEN

No Windows PowerShell, dentro do projeto:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\generate-app-token.ps1
```

Copie o token exibido e guarde em local privado. Ele será usado no Render e no aplicativo.

## ETAPA 3 — Render

No Render:

1. Crie um novo **Blueprint** apontando para o repositório GitHub.
2. O Render localizará `render.yaml`.
3. O serviço definido chama-se `nexo-keyboard-api`.
4. Quando solicitado, informe:
   - `OPENAI_API_KEY` = sua chave OpenAI.
   - `APP_TOKEN` = token gerado na etapa anterior.
5. Conclua o deploy.

Depois abra:

`https://nexo-keyboard-api.onrender.com/api.php`

A resposta esperada é um JSON com `"ok":true` e `"status":"online"`.

Se o Render criar um endereço diferente, anote a URL real.

## ETAPA 4 — InfinityFree

Conta/domínio novo:

`https://nexo-keyboard.page.gd`

No File Manager desse domínio:

1. Entre na pasta `htdocs`.
2. Apague apenas a página padrão criada para esse novo domínio, se houver.
3. Envie **o conteúdo** da pasta local `htdocs/` deste projeto.
4. Não envie a pasta `server/` para o InfinityFree.
5. Não envie `OPENAI_API_KEY` nem `APP_TOKEN` para o InfinityFree.

Abra:

`https://nexo-keyboard.page.gd`

Se o endereço do Render não for `https://nexo-keyboard-api.onrender.com/api.php`, edite:

`htdocs/assets/config.js`

antes de enviar o site.

## ETAPA 5 — Gerar APK de teste

No GitHub:

1. Abra **Actions**.
2. Selecione **Build Nexo AI Debug**.
3. Use **Run workflow**.
4. Ao terminar, abra a execução.
5. Baixe o artifact `NexoAI-Keyboard-debug`.
6. Extraia e instale `app-debug.apk` no Android.

## ETAPA 6 — Configurar o aplicativo

Ao abrir o Nexo AI:

1. Confirme o endpoint do Render.
2. Informe o mesmo `APP_TOKEN` configurado no Render.
3. Toque em **Salvar configurações**.
4. Toque em **Testar IA**.
5. Se aparecer `IA online`, toque em **Ativar teclado no Android**.
6. Habilite **Nexo AI Keyboard**.
7. Volte ao app e toque em **Escolher teclado agora**.
8. Selecione **Nexo AI Keyboard**.

## ETAPA 7 — Teste no WhatsApp

Teste estes fluxos:

1. Digite uma mensagem e toque em **Otimizar**.
2. Selecione apenas parte do texto e toque em **Profissional**.
3. Use **Desfazer**.
4. Copie uma mensagem recebida, abra a caixa de resposta vazia e toque em **Responder**.
5. Teste **Corrigir**, **Resumir**, **Organizar** e **Minha Cara**.
6. Confirme que em campo de senha aparece modo seguro e a IA fica bloqueada.

## ETAPA 8 — APK release assinado

Para distribuição, configure no GitHub:

- `NEXO_KEYSTORE_BASE64`
- `NEXO_KEYSTORE_PASSWORD`
- `NEXO_KEY_ALIAS`
- `NEXO_KEY_PASSWORD`

Depois execute **Build Nexo AI Release**.

Ele gera:

- APK release assinado.
- AAB para distribuição compatível com Play Console.

## Regra principal

```text
InfinityFree = site público
Render       = API privada do Nexo AI
GitHub       = código + compilação
Android      = teclado
OpenAI key   = SOMENTE Render
```
