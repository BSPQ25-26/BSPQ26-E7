# LabLend Project Overview

## What is LabLend?

**LabLend** is a RESTful microservice and web application for managing the borrowing of expensive laboratory equipment in an academic setting. Students can browse available equipment, submit borrowing requests, and manage their active loans. Administrators can manage inventory, approve or deny requests, and track equipment status.

## Key Features

### For Students
- **Browse Equipment** — View available laboratory equipment with detailed descriptions
- **Request Equipment** — Submit borrowing requests with desired dates
- **Manage Loans** — Track active loans, view return dates, and loan history
- **Waiting List** — Join waiting lists for unavailable equipment

### For Administrators
- **Equipment Management** — Add, edit, delete equipment from the system
- **Inventory Tracking** — Monitor equipment status, availability, and location
- **Request Approval** — Review and approve/deny borrowing requests
- **Overdue Tracking** — Monitor overdue loans and send reminders
- **User Management** — Manage student accounts and permissions

## Technology Stack

### Backend
| Component | Technology | Version |
|-----------|-----------|---------|
| **Runtime** | Java | 21 Temurin |
| **Framework** | Spring Boot | 3.3.5 |
| **ORM** | Spring Data JPA | Latest |
| **Security** | Spring Security + JWT | Latest |
| **Build Tool** | Maven | 3.9+ |
| **Testing** | JUnit 5, Mockito | Latest |
| **Logging** | Log4j2 | Latest |
| **Code Coverage** | JaCoCo | Latest |

### Frontend
| Component | Technology | Version |
|-----------|-----------|---------|
| **Runtime** | Node.js | 18+ |
| **Framework** | React | 19 |
| **Language** | TypeScript | Latest |
| **Build Tool** | Vite | Latest |
| **UI Library** | Material-UI (MUI) | 7 |
| **Routing** | React Router | 7 |
| **HTTP Client** | Axios | Latest |
| **Testing** | Vitest, Testing Library | Latest |
| **Linting** | ESLint | Latest |

### Database
| Component | Technology | Version |
|-----------|-----------|---------|
| **Database** | MySQL | 8.0 |
| **Container** | Docker | Latest |

### DevOps
| Component | Technology |
|-----------|-----------|
| **Containerization** | Docker + Docker Compose |
| **API Documentation** | Javadoc + Sphinx |

## Core Entities

### Equipment
Represents laboratory equipment available for borrowing.

```json
{
  "id": 1,
  "name": "Digital Oscilloscope",
  "type": "Electronic Equipment",
  "status": "AVAILABLE",
  "description": "High-precision 200 MHz oscilloscope"
}
```

### User
Student or administrator account.

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@university.edu",
  "role": "STUDENT"
}
```

### Loan
A borrowing transaction between a user and equipment.

```json
{
  "id": 1,
  "userId": 1,
  "equipmentId": 1,
  "loanDate": "2026-05-16",
  "returnDate": "2026-05-23",
  "status": "ACTIVE"
}
```

## Access Control

The application implements **Role-Based Access Control (RBAC)** with two roles:

- **ADMIN** — Full access to manage equipment, approve requests, manage users
- **STUDENT** — Can view equipment, submit requests, manage personal loans

Protected endpoints require both:
1. Valid JWT token in the `Authorization` header
2. Appropriate role for the operation

## Development Methodology

This project was developed following the **SCRUM methodology** with:
- 3 development sprints
- Sprint planning and retrospectives
- Daily standups and progress tracking
- Continuous integration and deployment

## Project Timeline

- **Sprint 1** — Core backend API and authentication
- **Sprint 2** — Frontend interface and integration
- **Sprint 3** — Testing, documentation, and deployment

## Next Steps

1. **[Quick Start](quick-start.md)** — Get the application running in 5 minutes
2. **[Backend Architecture](../backend/architecture/overview.md)** — Understand how components work together

---

**Team**: LabLend Development Team  
**Academic Institution**: University  
**Course**: Software Engineering  
**Last Updated**: May 16, 2026
