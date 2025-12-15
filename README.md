# MedTracker

MedTracker is a Progressive Web App (PWA) designed to help users track their daily medication intake.

## Features

- **Daily Tracking**: Easily mark medications as taken.
- **Time Selection**: Customize medication times during onboarding.
- **Visual Feedback**: Clear visual cues for completed and pending medications.
- **PWA Support**: Installable on mobile and desktop devices.
- **Notifications**: Get reminded when it's time to take your medication.

## Tech Stack

- **React**: UI library.
- **TypeScript**: Type safety.
- **Vite**: Build tool.
- **Material UI**: Component library.
- **PWA**: Offline capabilities and installation.

## Getting Started

1.  Clone the repository:
    ```bash
    git clone https://github.com/codexico/med-tracker.git
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Run the development server:
    ```bash
    npm run dev
    ```

## Running with Docker / Devcontainer

This project is configured to run inside a Docker container using VS Code's Dev Containers extension. This ensures a consistent development environment for all contributors.

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop)
- [Visual Studio Code](https://code.visualstudio.com/)
- [Dev Containers Extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) for VS Code

### Steps

1.  Open the project folder in VS Code.
2.  You should see a notification asking if you want to reopen the folder in a container. Click **Reopen in Container**.
    - Alternatively, press `F1`, type `Dev Containers: Reopen in Container`, and select it.
3.  VS Code will build the Docker image and start the container. This might take a few minutes the first time.
4.  Once the container is running, the terminal in VS Code will be inside the container.
5.  Install dependencies (if not automatically installed):
    ```bash
    npm install
    ```
6.  Run the development server:
    ```bash
    npm run dev
    ```
7.  Open your browser at `https://localhost:5173` to view the app.
    - **Note:** Since we use a self-signed certificate for local development (required for PWA features), your browser will likely show a security warning ("Your connection is not private"). You can safely proceed by clicking "Advanced" and then "Proceed to localhost (unsafe)" (or similar, depending on your browser).

### New versions

1.  Run `npm version {major|minor|patch}` to create a new version.


## License

MIT
