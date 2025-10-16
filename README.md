# Casino Royale - Spring Boot Backend

This repository contains the backend service for the Casino Royale web application. It is a modern, containerized Spring Boot application that provides a secure RESTful API and WebSocket endpoints for user management, wallet operations, and casino games.

-----

## ✨ Features

* **Secure RESTful API**: Endpoints are secured using JWTs provided by Firebase Authentication.
* **Role-Based Access Control**: Differentiates between standard `USER` and `ADMIN` roles.
* **Game Logic**:
    * **Roulette**: Complete backend logic for placing bets and determining outcomes.
    * **Cosmic Cashout**: Real-time, multiplayer game state management using WebSockets (STOMP).
* **Wallet & User Management**: APIs for managing user profiles, balances, and viewing transaction/game history.
* **Admin Capabilities**: Protected endpoints for viewing casino-wide statistics and promoting users to admin status.
* **API Documentation**: Interactive API documentation is automatically generated with Swagger (OpenAPI).
* **Containerized Environment**: Fully configured with Docker and Docker Compose for a consistent and easy-to-run development environment.

-----

## 🛠️ Tech Stack

* **Framework**: Spring Boot 3
* **Language**: Java 21
* **Security**: Spring Security
* **Real-time**: Spring WebSocket (STOMP)
* **Database**: Google Cloud Firestore
* **Authentication**: Firebase Authentication (via Firebase Admin SDK)
* **Build Tool**: Maven
* **Containerization**: Docker & Docker Compose

-----

## 🚀 Getting Started

Follow these steps to set up and run the backend service locally.

### Prerequisites

* [Docker](https://www.docker.com/products/docker-desktop/) and Docker Compose installed.
* A Google Firebase account (the free tier is sufficient).

-----

### 1\. Firebase Project Setup (Crucial)

The backend requires credentials to connect to your Firebase project.

#### 1.1 Create the Service Account Key

1.  Go to your **Firebase Console** -\> **Project Settings** (click the ⚙️ icon) -\> **Service accounts**.
2.  Click **"Generate new private key"**.
3.  A JSON file will be downloaded. Rename this file to **`serviceAccountKey.json`**.
4.  Place this file inside the project's resource folder at: `src/main/resources/`.

#### 1.2 Enable Required Services (required only once for the firestore setup)

1.  **Authentication**: In the Firebase Console, go to **Authentication** -\> **Sign-in method** and **enable** at least one provider (e.g., **Google** or **Email/Password**).
2.  **Database**: Go to **Firestore Database**, click **"Create database"**, and start in **Test mode** for development.
3.  **API**: Ensure the **Cloud Firestore API** is enabled for your project in the Google Cloud Console. If you encounter a `PERMISSION_DENIED` error on startup, the error log will provide a direct link to enable it.

-----

### 2\. Running the Backend

With Docker, running the application is a single command.

1.  Open a terminal in the root directory of this project (where `docker-compose.yml` is located).

2.  Run the following command to build the Docker image and start the container:

    ```bash
    docker-compose up --build
    ```

The backend service will start and be accessible at `http://localhost:8080`.

-----

## 🌐 Interacting with the API

The primary way to interact with and test the backend is through the built-in Swagger UI.

### Accessing Swagger UI

Once the container is running, open your browser and navigate to:

**`http://localhost:8080/swagger-ui/index.html`**

This page provides an interactive interface for all available API endpoints, allowing you to view their details and execute test requests directly from your browser.

### Setting Up Your First Admin User

To test the admin-only endpoints, you must manually promote your user account.

1.  **Get Your UID**: First, authenticate with a client application (like the provided frontend or a test script) to create a user. Then, go to the **Firebase Console** -\> **Authentication** and copy the **User UID** for that account.
2.  **Use Swagger**:
    * Go to the Swagger UI: `http://localhost:8080/swagger-ui.html`.
    * Find the `Admin Panel` section and the `POST /api/v1/admin/users/{uid}/promote` endpoint.
    * Click **"Try it out"**, paste your UID into the `uid` parameter field, and click **Execute**.

Your user will now have the `ADMIN` role. To access protected admin endpoints, you will need to get a new ID token for that user (by logging in again) and use it in the `Authorization` header of your requests.