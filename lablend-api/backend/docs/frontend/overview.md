# Frontend Architecture

React 19 + TypeScript frontend for LabLend equipment borrowing system.

## Folder Structure

```text
frontend/src/
├── main.tsx              # React entry point
├── app/
│   ├── App.tsx          # Root component
│   ├── theme.ts         # Material-UI theme configuration
│   ├── routes/
│   │   ├── AppRouter.tsx        # Main routing configuration
│   │   ├── RequireAuth.tsx       # Auth guard wrapper
│   │   └── RequireRole.tsx       # Role-based access guard
│   └── styles/
│       ├── app.css              # App styles
│       └── global.css           # Global styles
├── features/            # Feature modules
│   ├── admin/
│   │   ├── pages/              # Admin pages
│   │   └── components/         # Admin-specific components
│   ├── auth/
│   │   ├── context/AuthContext.tsx  # Authentication state
│   │   ├── pages/                   # Login, signup
│   │   └── components/
│   └── user/
│       ├── pages/              # User pages
│       └── components/
├── services/
│   ├── authService.ts           # Login, logout, token refresh
│   ├── authToken.ts             # Token storage and retrieval
│   ├── equipmentService.ts       # Equipment API calls
│   ├── loanService.ts            # Loan API calls
│   ├── userService.ts            # User API calls
│   ├── waitingListService.ts     # Waiting list API calls
│   └── http/
│       └── client.ts            # Axios HTTP client configuration
├── shared/
│   ├── assets/                  # Images, fonts, icons
│   ├── auth/
│   │   └── jwtClaims.ts        # JWT token decoding
│   ├── types/
│   │   └── domain.ts            # TypeScript types/interfaces
│   └── ui/
│       ├── AppSnackbar.tsx       # Notification component
│       ├── ConfirmDialog.tsx     # Confirmation dialog
│       └── SectionHeader.tsx     # Section title component
└── test/                        # Test utilities
```

## Key Technologies

| Component | Library | Version |
|-----------|---------|---------|
| **Runtime** | Node.js | 18+ |
| **Framework** | React | 19 |
| **Language** | TypeScript | Latest |
| **Build Tool** | Vite | Latest |
| **UI Library** | Material-UI (MUI) | 7 |
| **Router** | React Router | 7 |
| **HTTP Client** | Axios | Latest |
| **Testing** | Vitest + Testing Library | Latest |

## Key Features

- **Authentication**: JWT tokens with login/logout
- **Protected Routes**: RequireAuth wrapper prevents access without token
- **Role-Based Access**: RequireRole wrapper restricts admin pages
- **HTTP Interceptors**: Auto-attach tokens, handle 401 errors
- **Error Handling**: Global error handling in services
- **Material-UI**: Professional component library
- **TypeScript**: Type safety throughout

---

**Next**: [LabLend Project Overview](../getting-started/overview.md)
