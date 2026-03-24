# LabLend API - BSPQ26-E7

LabLend is a small but complete RESTful microservice designed to manage the borrowing of expensive laboratory equipment by university students. 

The aim of this project is to apply the **SCRUM** framework (divided into 3 Sprints) and **DevOps** practices to develop a multi-tier distributed application.

## Project Scope & Features

This application implements the following requirements:

### 1. CRUD Functionality
* **Equipment Management:** Create, Read, Update, and Delete laboratory items.
* **User Management:** Manage students authorized to borrow equipment.

### 2. Business Rules
* **Availability Check:** Students can only borrow equipment if its current status is "Available".
* **Borrowing Limits & Returns:** The system enforces limits on how many items a student can borrow and manages the return cycle to update the item's availability.

### 3. Non-Functional Requirements (NFRs)
* **API Security:** Sensitive endpoints (like adding or deleting equipment) are protected.
* **API Documentation:** The RESTful API is fully documented and testable.

## Architecture & Technology Stack
* **Architecture:** Multi-tier distributed application exposing a RESTful API.
* **Methodology:** SCRUM (Product Backlog managed via GitHub Projects).
* **DevOps:** CI/CD pipelines using GitHub Actions, Issue tracking, and Agile boards.
