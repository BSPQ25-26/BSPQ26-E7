document.addEventListener("DOMContentLoaded", function() {
  if (typeof mermaid === 'undefined') return;
  try {
    mermaid.initialize({ startOnLoad: false, theme: 'default' });
    // Replace highlight-mermaid pre blocks with .mermaid containers
    document.querySelectorAll('.highlight-mermaid pre').forEach(function(pre) {
      var code = pre.textContent || '';
      var container = document.createElement('div');
      container.className = 'mermaid';
      container.textContent = code;
      pre.parentNode.replaceChild(container, pre);
    });
    // Render all mermaid blocks
    mermaid.init(undefined, document.querySelectorAll('.mermaid'));
  } catch (e) {
    // Fail silently; diagrams will remain as code blocks
    console.warn('Mermaid init failed', e);
  }
});
