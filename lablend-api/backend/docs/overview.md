# Overview

This documentation hub contains:

- Project-level guides and architecture notes in Markdown.
- Java API reference generated with Maven Javadoc.

## Local Build

From the repository root:

```bash
cd lablend-api/backend
mvn -B clean javadoc:javadoc
python3 -m pip install -r docs/requirements.txt
python3 -m sphinx -b html docs docs/_build/html
mkdir -p docs/_build/html/javadoc
cp -r target/site/apidocs/* docs/_build/html/javadoc/
```

Then open `docs/_build/html/index.html` in your browser.
