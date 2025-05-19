Spring Boot Flattrade WebSocket Market Data Application
This application connects to Flattrade's Market Data WebSocket API to receive real-time market data and processes it to create 1-minute candles. It provides REST endpoints to manage the WebSocket connection and retrieve market data.
Files Overview

FlattradeMDPWebSocketClient.java: Core WebSocket client that connects to Flattrade's market data API
MarketData.java: Model class for market data received from WebSocket
MarketDataService.java: Service for managing market data connections and subscriptions
WebSocketConfig.java: Spring configuration for WebSocket client
MarketDataController.java: REST controller for managing market data connection and retrieving data
CandleStickAggregator.java: Utility class to aggregate tick data into 1-minute candles

Setup Instructions

Add the following dependencies to your pom.xml:

xml<dependencies>
<!-- Spring Boot Starter -->
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-web</artifactId>
</dependency>

    <!-- WebSocket support -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    
    <!-- JSON processing -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    
    <!-- Lombok for boilerplate code reduction -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

Place all the Java files in their respective packages as indicated in the file headers.

Usage
Authentication
Make sure you have authenticated and obtained a token from Flattrade API before using this application.
Connecting to WebSocket
Send a POST request to /api/market/connect with a token:
json{
"token": "YOUR_FLATTRADE_TOKEN"
}
Subscribing to Market Data
Send a POST request to /api/market/subscribe with a list of tokens:
json{
"tokens": ["NSE|26000", "NSE|26009"]
}
Get Market Data

Get all market data: GET /api/market/data
Get specific token data: GET /api/market/data/{token}
Check connection status: GET /api/market/status

Quick Test
For quick testing, use: GET /api/market/quickConnect/{token}
Notes

This application uses Spring Boot's WebSocket client to connect to Flattrade's MDP WebSocket server
Market data is processed in real-time to create 1-minute candles
The application maintains an in-memory store of the latest market data and candles
Error handling and reconnection logic are implemented

Implementation Details
WebSocket Connection Flow

User obtains authentication token from Flattrade API (not part of this implementation)
Application connects to Flattrade WebSocket using the token
Application subscribes to specific market instruments
Real-time data is received and processed

Market Data Processing
The application processes incoming market data in these steps:

Raw tick data is received from WebSocket
Data is parsed into the MarketData object
Latest tick data is stored for each instrument
CandleStickAggregator processes ticks into 1-minute candles
Data is made available through REST endpoints

Error Handling
The application includes error handling for:

WebSocket connection issues
Parse errors
Network timeouts
Subscription failures

Integration with Your Existing Project

Copy the Java files into your existing Spring Boot project
Ensure dependencies are added to your pom.xml
Configure the application.properties with any necessary settings
Test the connection with your Flattrade authentication token

Extending the Application
You can extend this application by:

Adding persistence to store historical candle data
Implementing technical indicators on candle data
Creating a WebSocket server to broadcast processed data to front-end clients
Adding authentication for the REST API
