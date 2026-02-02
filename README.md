# CPD Project — Multi-Room TCP Chat with AI

# Grade: 19.05/20

This project implements a **multi-room TCP chat system** with AI support, user authentication, and fault-tolerant reconnect via session tokens. It is built with **Java 21**, uses **virtual threads (Project Loom)** for scalable concurrency, and includes **Ollama** integration for AI-assisted rooms.

## Features

- Multi-room public and private chat rooms
- AI-enhanced rooms with contextual bot responses
- User authentication with password validation
- Token-based reconnection (seamless login)
- Private messaging between users
- Room management (create/join/leave/list)

## Tech Stack

- Java SE 21
- TCP sockets
- Virtual threads
- Concurrency tools (`ReentrantLock`, `Thread.startVirtualThread()`)

## Setup

### 1) Install Java 21+

```bash
java --version
```

### 2) (Optional) Run Ollama for AI rooms

```bash
sudo docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama14 ollama/ollama
```

## Build & Run

From the [project/src](project/src) directory:

```bash
javac -cp "libs/*" server/*.java client/*.java
```

### Start the server

```bash
java -cp ".:libs/*" server.ChatServer
```

### Start a client

```bash
java -cp ".:libs/*" client.ClientUI
```

Default server port is `12345`.

## Client Commands

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

## Authentication & Tokens

- First login uses username/password via `AuthenticationManager`
- A session token is stored in `helpers/token_<username>.txt`
- If the token is still valid, the client reconnects without re-entering credentials

## Project Structure

```text
project/
├── src/
│   ├── client/
│   │   └── ClientUI.java
│   ├── server/
│   │   ├── AIHelper.java
│   │   ├── AuthenticationManager.java
│   │   ├── ChatServer.java
│   │   ├── ClientHandler.java
│   │   ├── Room.java
│   │   ├── RoomManager.java
│   │   └── TokenManager.java
│   └── helpers/
│       └── users.txt
└── doc/
		└── README.md
```

## Academic Info

- **Course**: Computação Paralela e Distribuída (CPD)
- **Class**: T04
- **Group**: G16
- **Authors**:
	- Ana Carolina Coutinho — up202108685
	- José Granja — up202205143
	- Leonardo Ribeiro — up202205144

