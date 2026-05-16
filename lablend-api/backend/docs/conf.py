project = "LabLend Backend Documentation"
author = "LabLend Team"
release = "0.0.1-SNAPSHOT"

extensions = [
    "myst_parser",
]

templates_path = ["_templates"]
exclude_patterns = ["_build", "Thumbs.db", ".DS_Store"]

source_suffix = {
    ".md": "markdown",
}

master_doc = "index"
html_theme = "sphinx_rtd_theme"
html_static_path = ["_static"]
myst_enable_extensions = [
    "deflist",
    "colon_fence",
]
