# LabLend Frontend

React + TypeScript + Vite frontend for the LabLend project.

## Backend integration

- Frontend calls backend endpoints under `/api`.
- During local development, Vite proxies `/api/*` to `http://localhost:8080`.
- You can override the API base URL with `VITE_API_BASE_URL`, for example:
	- `VITE_API_BASE_URL=http://localhost:8080/api`

Current integrated resources:

- Equipment (`/api/equipment`): list, create, reserve, update status, delete.
- Loans (`/api/loans`): list, create, update status, delete.

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
