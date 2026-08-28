# Diagramme d'architecture Graphify

## Fichiers

- `docs/architecture-graph.html`
- `docs/architecture-graph.css`
- `docs/architecture-graph.js`

## Lancement local

Depuis la racine du projet :

```bash

python3 -m http.server

```

Puis ouvrir :

`http://localhost:8000/docs/architecture-graph.html`

## Notes

- Source de données : `graphify-out/graph.json`
- Vue volontairement synthétique (nœuds clés + ports/use cases + persistance) pour éviter d'afficher tous les nœuds simultanément.
