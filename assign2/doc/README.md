
# 📄 CPD Project 2 — README

## Overview

This project implements a **multi-room TCP chat system** with AI support, user authentication, and fault-tolerant reconnect via session tokens.

Technologies used:

* Java SE 21
* Virtual Threads (Project Loom)
* Socket Programming (TCP)
* Concurrency (`ReentrantLock`, `Thread.startVirtualThread()`)

---

## ⚙️ Setup and Dependencies

### 1. Install Java 21+

Ensure Java 21 or newer is installed. You can check with:

```bash
java --version
```

If needed, download it from: https://jdk.java.net/21/

### 2. Install Ollama for AI Rooms

To enable the AI Bot feature, install [Ollama](https://ollama.com/) and run a model locally (e.g., `llama2`):

```bash
ollama run llama2
```

---

## 🐳 Docker 

You can run the server and client inside Docker containers to simplify setup.

### Build the Docker Image

```bash
docker build -t cpd-chat .
```

### Run the Server

```bash
docker run -it --rm -p 12345:12345 --name chat-server cpd-chat java server.ChatServer
```

### Run a Client

In a separate terminal:

```bash
docker run -it --rm --network host --name chat-client cpd-chat java client.ClientUI
```

> ℹ️ `--network host` is required to connect to the host server on localhost. For Windows/macOS, adjust accordingly or use Docker Compose.

---

## 🚀 Running the System (Without Docker)

### 1. Compile All Files

Assuming you're in the root of the project:

```bash
javac server/*.java client/*.java
```

### 2. Start the Server

```bash
java server.ChatServer
```

By default, it listens on port `12345`.

### 3. Start a Client

```bash
java client.ClientUI
```

Each client will display an interactive menu for login and room selection.

---

## 👥 User Interaction (Client Side)

The menu includes:

* Username input
* Server IP and Port
* Token-based login (automatic reconnection)

### Available commands once logged in:

```
/help           Show help menu
/rooms          List all rooms
/join <name>    Join or create public room
/createpriv     Create private room (prompted)
/joinpriv       Join private room (prompted)
/createai       Create AI-enhanced room with prompt
/msg <u> <m>    Private message to user
/users          List users in current room
/whoami         Display current identity and room
/leave          Return to Lobby
/quit           Disconnect client
```

---

## 🔐 Authentication & Tokens

* First login is done with username/password (loaded or hardcoded by `AuthenticationManager`)
* Session token is saved on disk in `helpers/token_<username>.txt`
* Upon reconnect, token is used for seamless re-entry (without typing credentials)

---

## 🤖 AI Room Feature

* When creating an AI room with `/createai roomName prompt`, the AI bot (via Ollama) responds contextually to users
* Responses are auto-injected into the timeline as `Bot:`

---

## 📦 Project Structure

```text
.
├── client/
│   └── ClientUI.java
├── server/
│   ├── AIHelper.java
│   ├── AuthenticationManager.java
│   ├── ChatServer.java
│   ├── ClientHandler.java
│   ├── Room.java
│   ├── RoomManager.java
│   └── TokenManager.java
└── helpers/
    └── token_<username>.txt
```

---

## 👨‍🏫 Academic Info

* **Project for**: Computação Paralela e Distribuída (CPD)
* **Turma**: T04
* **Grupo**: G16
* **Authors**:

  * Ana Carolina Coutinho
  * Leonardo Ribeiro
  * José Granja



