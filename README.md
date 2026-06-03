# CargoRoute IQ 🚛📦

CargoRoute IQ is a comprehensive, web-based logistics platform designed for regional carriers, freight forwarders, and logistics coordinators. It streamlines supply chain operations by centralizing bookings, managing vehicle capacity, optimizing route assignments, and balancing load consolidation across fleets.

---

## 🚀 Key Features

*   **Fleet Management**: Track vehicle availability, capacity, and maintenance logs.
*   **Dynamic Routing**: Optimize delivery paths for maximum efficiency and fuel savings.
*   **Load Balancing**: Intelligently consolidate shipments to maximize vehicle utilization.
*   **Real-time Bookings**: Centralized management of shipping requests with live status updates.
*   **Advanced Analytics**: Visual KPIs and interactive reports powered by Recharts.
*   **Role-Based Access Control (RBAC)**: Secure, granular access tailored for Admins, Dispatchers, Drivers, and Shippers[cite: 1].

---

## 🛠 Tech Stack

### Frontend
*   **Library**: React 19.2.4
*   **Routing**: React Router DOM 7.14.0
*   **UI Components**: Lucide-React & React-Icons
*   **Data Visualization**: Recharts 3.8.1
*   **HTTP Client**: Axios 1.14.0
*   **Authentication**: JWT Decode 4.0.0

### Backend (Microservices)
*   **Language**: Java 17 (LTS)
*   **Framework**: Spring Boot 4.0.5
*   **Architecture**: Spring Cloud 2025.1.1 (Microservices)
*   **Service Discovery**: Netflix Eureka
*   **Security**: Spring Security with JWT (jjwt)
*   **Persistence**: MySQL with Spring Data JPA
*   **Build Tool**: Maven

---

## 📂 Project Structure

```text
cargoRoute-IQ-Project/
├── CargoRoute-IQ/           # Spring Boot Microservices
│   ├── API-Gateway/         # Unified entry point & routing logic
│   ├── Eureka-Server/       # Service registry & discovery
│   ├── Fleet-Service/       # Vehicle & driver resource management
│   ├── Routing-Service/     # Path optimization & route planning
│   └── Booking-Service/     # Shipment lifecycle & order management
└── frontendproject/         # React Frontend Application
    ├── src/api/             # API services & Axios interceptors
    ├── src/components/      # Shared UI elements
    ├── src/pages/           # View modules (Dashboards, Login, etc.)
    └── src/styles/          # Unified CSS & theme modules
```

---
## Getting Started
Prerequisites
**Node.js**: v18 or higher

**JDK**: 17 (LTS)

**Maven**: 3.8 or higher

**MySQL**: Running on port 3306

**Installation & Execution**
1. Clone the Repository
#### git clone [https://github.com/SnehaMai18/cargoRoute-IQ-Project.git](https://github.com/SnehaMai18/cargoRoute-IQ-Project.git)
cd cargoRoute-IQ-Project

**2. Frontend Deployment**
cd frontendproject
npm install
npm start
*   The application will be accessible at `http://localhost:3000`.

3.  **Backend Deployment**
    *   Initialize the **Eureka Server** module first to enable service discovery
    *   Launch the **API Gateway** module
    *   Start functional microservices: **Fleet**, **Routing**, and **Booking**.

---

## 🧪 Quality Assurance

The frontend utilizes a robust testing suite for components and user workflows[cite: 1].
```bash
npm test


