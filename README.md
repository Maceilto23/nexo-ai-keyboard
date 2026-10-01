# Nexo AI Keyboard 2026 — v3.1

Projeto completo do **Nexo AI Keyboard**, um teclado Android (IME) com ações de IA sob comando do usuário.

## Arquitetura oficial

```text
Android / APK
    │ HTTPS + APP_TOKEN
    ▼
Render — nexo-keyboard-api
    │ OPENAI_API_KEY (somente aqui)
    ▼
OpenAI Responses API

InfinityFree — https://nexo-keyboard.page.gd
    └─ site público, privacidade e status
```

O domínio antigo não precisa ser alterado.

## Estrutura

```text
NexoAI_Keyboard_2026/
├─ android/                  app Android / teclado IME
├─ server/                   backend privado para Render
├─ htdocs/                   site público para nexo-keyboard.page.gd
├─ tools/                    utilitários locais
├─ .github/workflows/        compilação automática do APK/AAB
├─ render.yaml               Blueprint do backend Render
├─ CHECKLISTA_IMPLANTACAO.md passo a passo em ordem
├─ SECURITY.md               regras de segurança
└─ README.md
```

## O que o teclado faz

- Teclado Android real baseado em `InputMethodService`.
- QWERTY PT-BR, números, símbolos, acentos, shift, backspace, enter e troca de teclado.
- Ações de IA: Auto, Responder, Rápido, Otimizar, Profissional, Educado, Direto, Convincente, Natural, Firme, Corrigir, Organizar, Aumentar, Resumir, Engraçado, Impactante, Carinhoso, Minha Cara e Personalizar.
- Trabalha apenas na seleção quando houver texto selecionado; sem seleção, usa o conteúdo do campo.
- Pode usar uma mensagem copiada para gerar uma resposta quando o campo estiver vazio.
- Desfazer a última substituição feita pela IA.
- Três atalhos personalizados.
- Perfil “Minha Cara”.
- Controle de tamanho da resposta e limite de caracteres.
- Histórico local opcional, desligado por padrão.
- Bloqueio das ações de IA em campos identificados como senha.
- `ACTION_PROCESS_TEXT` para textos selecionados em aplicativos compatíveis.
- Balão flutuante opcional.

## URLs definidas nesta versão

Site público:

`https://nexo-keyboard.page.gd`

Backend esperado:

`https://nexo-keyboard-api.onrender.com/api.php`

O endpoint é editável dentro do aplicativo. Se o Render atribuir outro hostname, informe a URL real na tela de configuração e altere `htdocs/assets/config.js`.

## Backend

O backend usa a OpenAI **Responses API**. Por padrão:

- tarefas rápidas → `gpt-5.6-luna`
- tarefas intermediárias → `gpt-5.6-terra`
- tarefas mais exigentes → `gpt-5.6-sol`

As respostas são solicitadas com `store=false` no backend do Nexo AI.

### Segredos do Render

Configure manualmente:

- `OPENAI_API_KEY`
- `APP_TOKEN`

Nunca coloque esses valores no repositório.

## InfinityFree

Envie **somente o conteúdo da pasta `htdocs/`** para o `htdocs` da conta do domínio `nexo-keyboard.page.gd`.

A pasta contém apenas arquivos públicos. Ela não contém o backend da IA.

## GitHub Actions

- `Build Nexo AI Debug`: gera APK para testes.
- `Build Nexo AI Release`: gera APK e AAB assinados, usando os secrets de assinatura.

Leia `CHECKLISTA_IMPLANTACAO.md` e siga a ordem indicada.
