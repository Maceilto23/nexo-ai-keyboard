(() => {
  const dot = document.getElementById('statusDot');
  const title = document.getElementById('statusTitle');
  const text = document.getElementById('statusText');
  if (!dot || !title || !text) return;
  const url = window.NEXO_CONFIG?.apiStatusUrl;
  if (!url) return;
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 8000);
  fetch(url, { cache: 'no-store', signal: controller.signal })
    .then(r => r.ok ? r.json() : Promise.reject(new Error('HTTP ' + r.status)))
    .then(data => {
      if (!data?.ok) throw new Error('offline');
      dot.classList.add('online');
      title.textContent = 'Inteligência online';
      text.textContent = `Backend ${data.version || ''} conectado e separado deste site.`;
    })
    .catch(() => {
      dot.classList.add('pending');
      title.textContent = 'Backend ainda não conectado';
      text.textContent = 'O portal está pronto. Falta concluir ou acordar o serviço no Render.';
    })
    .finally(() => clearTimeout(timer));
})();
