# EventChain - Decentralized Event Management System

Production-ready full-stack decentralized event management with NFT tickets, lottery, Stripe payments, and blockchain verification.

## Tech Stack

- **Frontend:** React (Vite), Tailwind CSS, Ethers.js, STOMP WebSocket, QR codes
- **Backend:** Java 21, Spring Boot 3.2, Spring Security, JWT, OAuth2, Stripe, Web3j
- **Blockchain:** Solidity 0.8.x, ERC-721 (OpenZeppelin), Polygon Mumbai

## Quick Start

### Prerequisites

- Node.js 18+
- Java 21
- MySQL 8
- (Optional) MetaMask, Stripe test account, Polygon Mumbai wallet

### 1. Database Setup

```bash
mysql -u root -p < schema.sql
```

Or use Docker:

```bash
docker run -d --name eventchain-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=event_management_chain -p 3306:3306 mysql:8.0
```

### 2. Backend

```bash
cd backend
cp ../.env.example .env   # Edit .env with your config
mvn spring-boot:run
```

Backend runs at `http://localhost:8080`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173`.

### 4. Blockchain (Optional)

```bash
cd blockchain
npm install
cp .env.example .env      # Add PRIVATE_KEY for deployer
npx hardhat compile
npx hardhat run scripts/deploy.js --network mumbai
```

Copy the deployed contract address to backend `.env` as `CONTRACT_ADDRESS`.

## Docker Deployment

```bash
docker-compose up -d
```

Then build and serve the frontend:

```bash
cd frontend && npm run build
# Serve dist/ with nginx or similar
```

## Environment Variables

| Variable | Description |
|----------|-------------|
| DB_* | MySQL connection |
| JWT_SECRET | 256-bit secret for JWT signing |
| STRIPE_SECRET_KEY | Stripe test key (sk_test_...) |
| STRIPE_WEBHOOK_SECRET | Stripe webhook signing secret |
| CONTRACT_ADDRESS | Deployed NFT contract (optional) |
| BLOCKCHAIN_PRIVATE_KEY | Wallet private key for minting |
| RPC_URL | Polygon Mumbai RPC |

## API Overview

- `POST /api/auth/register` - Register
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token
- `GET /api/events` - List events
- `POST /api/events` - Create event (admin)
- `POST /api/applications/events/{id}/apply` - Apply for event
- `POST /api/events/{id}/lottery/trigger` - Trigger lottery (admin)
- `POST /api/payments/checkout/{applicationId}` - Create Stripe checkout
- `GET /api/tickets/me` - My tickets
- `POST /api/checkin/events/{id}/tickets/{tokenId}` - Check in
- `GET /api/certificates/verify/{id}` - Verify certificate

## Stripe Test Card

Use `4242 4242 4242 4242` for successful test payments.

## Project Structure

```
Event-Management/
├── backend/          # Spring Boot API
├── frontend/         # React Vite app
├── blockchain/       # Solidity contracts + Hardhat
├── schema.sql        # MySQL schema
├── docker-compose.yml
└── README.md
```

## License

MIT
