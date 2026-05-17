project = "LabLend"
author = "LabLend Team"
release = "1.0.0"
version = "1.0"
copyright = "2026, LabLend Team"

extensions = [
    "myst_parser",
    "sphinxcontrib.mermaid",
]

templates_path = ["_templates"]
exclude_patterns = ["_build", "Thumbs.db", ".DS_Store", "*.swp"]

# Support both reStructuredText and Markdown source files
source_suffix = {
    ".rst": "restructuredtext",
    ".md": "markdown",
}

master_doc = "index"
html_theme = "sphinx_rtd_theme"
html_static_path = ["_static"]

# RTD Theme configuration
html_theme_options = {
    "logo_only": False,
    "collapse_navigation": True,
    "sticky_navigation": True,
    "navigation_depth": 4,
    "includehidden": True,
    "titles_only": False,
    "style_external_links": True,
}

# MyST Markdown extensions
myst_enable_extensions = [
    "deflist",
    "colon_fence",
    "fieldlist",
    "html_admonition",
    "smartquotes",
    "strikethrough",
    # 'table' is not a valid MyST extension name for this parser version
    "tasklist",
]

myst_heading_anchors = 3

# Enable syntax highlighting
pygments_style = "sphinx"
highlight_language = "python"

# Register `mermaid` as a plain-text lexer so Pygments doesn't warn
try:
    from sphinx.highlighting import lexers
    from pygments.lexers.special import TextLexer

    lexers["mermaid"] = TextLexer()
except Exception:
    # If Pygments or Sphinx API differs, ignore and continue — mermaid extension will still render diagrams in HTML
    pass

# Configuration for sphinxcontrib-mermaid
# Use a recent mermaid release and initialize client-side rendering
mermaid_version = "10.4.0"
mermaid_init_js = True
# Leave diagrams as raw Mermaid so the client-side library renders them
mermaid_output_format = "raw"

# Ensure the mermaid runtime is loaded in the built HTML and initialize it.
# This is a fallback and explicit inclusion to guarantee client-side rendering.
html_js_files = [
    "https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js",
    "js/mermaid-init.js",
]
