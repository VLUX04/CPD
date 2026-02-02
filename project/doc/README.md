
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

## 🐳 Docker 

### Run the Docker Container

To enable the AI Bot feature, Ollama is required. The Docker setup will automatically download and run Ollama 3 when you start the server.

```bash
sudo docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama14 ollama/ollama
```

## 🚀 Running the System

### 1. Compile All Files

Assuming you're in the assign/src folder of the project:

```bash
javac -cp "libs/*" server/*.java client/*.java
```

### 2. Start the Server

```bash
java -cp ".:libs/*" server.ChatServer
```

By default, it listens on port `12345`.

### 3. Start a Client

```bash
java -cp ".:libs/*" client.ClientUI
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
/logout         Log out and delete session token
```

---

## 🔐 Authentication & Tokens

* First login is done with username/password (loaded or hardcoded by `AuthenticationManager`)
* Session token is saved on disk in `helpers/token_<username>.txt`
* Upon reconnect, if it is still valid (hasn't expired), token is used for seamless re-entry (without typing credentials)

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

  * Ana Carolina Coutinho up202108685
  * Leonardo Ribeiro up202205144
  * José Granja up202205143



