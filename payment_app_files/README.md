# Payment Processing Application

A mock credit card payment processing application built with Spring Boot and Thymeleaf.

## Features

- **REST API Endpoints:**
  - `POST /api/payments/authorize` - Authorize a card transaction
  - `POST /api/payments/capture` - Capture an authorized transaction
  - `POST /api/payments/refund` - Refund a captured transaction
  - `GET /api/payments/{id}` - Get transaction status
  - `GET /api/payments/history` - List recent transactions
  - `POST /admin/cache/clear` - Clear local cache
  - `GET /actuator/health` - Health check endpoint
  - `GET /actuator/prometheus` - Metrics endpoint

- **Frontend:**
  - Payment form with card number, expiry, CVV, and amount fields
  - Transaction history dashboard
  - Status badges (Authorized, Captured, Declined, Refunded)
  - Real-time transaction updates

- **Backend Features:**
  - In-memory H2 database
  - Caffeine cache for transaction lookups
  - Realistic processing delays (200-500ms)
  - Random transaction declines (10% of transactions)
  - Card validation (expiry, test card numbers)

## Test Card Numbers

Use these test card numbers for testing:

- **Visa:** `4263970000005262`
- **MasterCard:** `5425230000004415`
- **Amex:** `374101000000608`

## Requirements

- Java 11 or higher
- Maven 3.6+

## Running the Application

### Single Command

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Alternative: Build and Run JAR

```bash
mvn clean package
java -jar target/payment-app-1.0.0.jar
```

## Accessing the Application

### Web Interface
Open your browser and navigate to:
```
http://localhost:8080
```

### H2 Database Console
Access the H2 console at:
```
http://localhost:8080/h2-console
```

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:paymentdb`
- Username: `sa`
- Password: (leave empty)

### Actuator Endpoints

- Health Check: `http://localhost:8080/actuator/health`
- Prometheus Metrics: `http://localhost:8080/actuator/prometheus`

## API Usage Examples

### Authorize a Payment

```bash
curl -X POST http://localhost:8080/api/payments/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4263970000005262",
    "cardExpiry": "12/25",
    "cvv": "123",
    "amount": 100.00
  }'
```

### Capture a Transaction

```bash
curl -X POST http://localhost:8080/api/payments/capture \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "your-transaction-id"
  }'
```

### Refund a Transaction

```bash
curl -X POST http://localhost:8080/api/payments/refund \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "your-transaction-id"
  }'
```

### Get Transaction by ID

```bash
curl http://localhost:8080/api/payments/1
```

### Get Transaction History

```bash
curl http://localhost:8080/api/payments/history
```

### Clear Cache

```bash
curl -X POST http://localhost:8080/admin/cache/clear
```

## Transaction Flow

1. **Authorize** - Reserve funds on the card
2. **Capture** - Actually charge the card (can only capture authorized transactions)
3. **Refund** - Return funds to the card (can only refund captured transactions)

## Response Codes

- `00` - Approved
- `DECLINED` - Transaction declined
- `EXPIRED` - Card expired
- `INSUFFICIENT_FUNDS` - Insufficient funds
- `INVALID_CARD` - Invalid card number

## Transaction Statuses

- **AUTHORIZED** (Yellow) - Transaction authorized, awaiting capture
- **CAPTURED** (Green) - Transaction captured, payment complete
- **DECLINED** (Red) - Transaction declined
- **REFUNDED** (Gray) - Transaction refunded

## Configuration

The application uses the following default configuration (see `application.properties`):

- Server Port: `8080`
- Database: In-memory H2
- Cache: Caffeine (max 1000 entries, 10 minutes expiry)
- Processing Delay: 200-500ms
- Random Decline Rate: 10%

## Project Structure

```
payment_app/
├── src/
│   └── main/
│       ├── java/com/demo/payment/
│       │   ├── PaymentApplication.java
│       │   ├── controller/
│       │   │   ├── PaymentController.java
│       │   │   ├── AdminController.java
│       │   │   └── WebController.java
│       │   ├── dto/
│       │   │   ├── PaymentRequest.java
│       │   │   ├── PaymentResponse.java
│       │   │   └── TransactionRequest.java
│       │   ├── model/
│       │   │   ├── Transaction.java
│       │   │   ├── TransactionStatus.java
│       │   │   └── TransactionType.java
│       │   ├── repository/
│       │   │   └── TransactionRepository.java
│       │   └── service/
│       │       └── PaymentService.java
│       └── resources/
│           ├── application.properties
│           └── templates/
│               └── index.html
├── pom.xml
└── README.md
```

## Technologies Used

- **Spring Boot 2.7.18** - Application framework
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **Caffeine** - Local caching
- **Thymeleaf** - Template engine
- **Spring Boot Actuator** - Monitoring and metrics
- **Micrometer Prometheus** - Metrics export
- **Hibernate Validator** - Input validation
- **Lombok** - Boilerplate code reduction

## Notes

- This is a **demo application** for testing purposes only
- All data is stored in-memory and will be lost when the application stops
- The application simulates realistic payment processing with delays and random declines
- Only the specified test card numbers will be accepted
- Card expiry dates must be in the future (MM/YY format)

## License

This is a demo application for educational purposes.