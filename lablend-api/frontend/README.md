# LabLend Frontend

React + TypeScript + Vite frontend for the LabLend project.

## Current organization

```text
src/
├── app/
│   ├── App.tsx
│   └── styles/
│       ├── app.css
│       └── global.css
├── features/
├── services/
│   └── http/
├── shared/
│   ├── assets/
│   └── ui/
├── test/
└── main.tsx
```

## Folder conventions

- `app/`: root app composition, providers, routing bootstrap, and global styles.
- `features/`: domain modules (e.g. `equipment`, `loans`, `users`) with local components, hooks and API calls.
- `services/http/`: reusable HTTP client and request utilities.
- `shared/ui/`: generic reusable UI components used by multiple features.
- `shared/assets/`: shared static assets (icons, images, logos).
- `test/`: test setup, mocks and shared testing utilities.

## Scripts

- `npm run dev`: start Vite dev server.
- `npm run build`: type-check and build production bundle.
- `npm run lint`: run ESLint.
- `npm run preview`: preview built app.
