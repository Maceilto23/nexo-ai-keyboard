# Segurança e privacidade — Nexo AI Keyboard

1. **Nunca coloque `OPENAI_API_KEY` no APK, GitHub, InfinityFree ou JavaScript público.** Ela pertence somente às variáveis de ambiente do Render.
2. O `APP_TOKEN` autentica o aplicativo no backend. Para uso pessoal/fechado ele funciona como segredo compartilhado. Para distribuição pública em escala, use autenticação individual e tokens de curta duração.
3. O Nexo AI só chama o backend quando o usuário toca em uma ação de IA.
4. Campos identificados como senha desabilitam as ações de IA.
5. O backend valida autenticação, limita tamanho das entradas, aplica rate limit simples e não implementa armazenamento do conteúdo das mensagens.
6. O request à Responses API usa `store=false`.
7. O histórico no Android é opcional, local e desligado por padrão.
8. Use apenas HTTPS.
9. Revogue imediatamente qualquer chave ou token que tenha sido publicado ou compartilhado indevidamente.
10. O domínio `nexo-keyboard.page.gd` é público e não deve receber arquivos contendo segredos.
