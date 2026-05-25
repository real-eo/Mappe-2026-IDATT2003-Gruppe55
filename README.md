# Portfolio Project IDATT2003

## Project Description

[//]: # (TODO: Write a short description of your project/product here.)

This project is a **stock market simulation game** called "Millions" with a graphical user interface built using **JavaFX**. It's built on **Java 25** and **Maven**. 

### Key Features:
- **Stock Trading**: Buy and sell shares on a simulated stock exchange with realistic Norwegian stocks (Equinor, DNB, Telenor)
- **Portfolio Management**: Track owned shares and their current values with real-time calculations
- **Weekly Simulation**: Advance time to simulate market changes with stock prices fluctuating ±5% per week
- **Transaction History**: Complete archive of all buy and sell transactions
- **Error Handling**: Robust validation for insufficient funds, invalid quantities, and more
- **Financial Precision**: Uses BigDecimal for accurate monetary calculations

## Project Structure

[//]: # (TODO: Describe the structure of your project here. How have you used packages in your structure. Where are all sourcefiles stored. Where are all JUnit-test classes stored. etc.)

### Packages:
- **`controller`**: Main application controller managing interactions between model and view
- **`model`**: Core business logic classes like `Exchange`, `Player`, `Portfolio`, `Stock`, and transactions
- **`model.calculator`**: Transaction calculation logic for purchases and sales
- **`exception`**: Custom exception classes for error handling
- **`view`**: JavaFX user interface components

### Full Overview:
```
millions-assignment/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── edu/ntnu/idi/idatt2003/millions/
│   │           ├── controller/              # Application controller
│   │           │   └── ExchangeController.java
│   │           ├── exception/               # Custom exceptions
│   │           │   ├── MillionsException.java
│   │           │   ├── InsufficientFundsException.java
│   │           │   ├── InvalidQuantityException.java
│   │           │   ├── ShareNotOwnedException.java
│   │           │   ├── StockNotFoundException.java
│   │           │   └── TransactionAlreadyCommittedException.java
│   │           ├── model/                   # Domain models
│   │           │   ├── Exchange.java
│   │           │   ├── Player.java
│   │           │   ├── Portfolio.java
│   │           │   ├── Stock.java
│   │           │   ├── Share.java
│   │           │   ├── Transaction.java
│   │           │   ├── Purchase.java
│   │           │   ├── Sale.java
│   │           │   ├── TransactionArchive.java
│   │           │   └── calculator/          # Business logic
│   │           │       ├── TransactionCalculator.java
│   │           │       ├── PurchaseCalculator.java
│   │           │       └── SaleCalculator.java
│   │           └── view/                    # User interface
│   │               └── MainApp.java
│   └── test/
│       └── java/
│           └── edu/ntnu/idi/idatt2003/millions/
│               ├── ExchangeTest.java
│               ├── PortfolioTest.java
│               ├── PurchaseCalculatorTest.java
│               ├── SaleCalculatorTest.java
│               ├── TransactionArchiveTest.java
│               └── TransactionTest.java
├── pom.xml
└── README.md
```

## Link to Repository

[//]: # (TODO: Include a link to your GitHub repository here.)

[Project GitHub Repository - IDATT2003 - Gruppe 55](https://github.com/NTNU-IDI/Mappe-2026-IDATT2003-Gruppe55) 

## How to Run the Project

[//]: # (TODO: Describe how to run your project here. What is the main class? What is the main method? What is the input and output of the program? What is the expected behaviour of the program?)

> [!WARNING]
> Make sure Maven is installed as a plugin in IntelliJ IDEA before proceeding. Also ensure you have Java 25 or higher installed.

### Open the Project
1. Launch IntelliJ IDEA
2. Select **File > Open** and open the root directory of the project

### Add Maven Configuration
If IntelliJ doesn't automatically recognize the `pom.xml` file, then right-click the `pom.xml` file and select **Add as Maven Project**

### Build the Project
1. Open the Maven tool window by clicking **View > Tool Windows > Maven**
2. Navigate to **Lifecycle** and double-click **clean**, then **install**

### Run the Application
**Option 1: Using Maven**
1. Open the Maven tool window
2. Navigate to **Plugins > javafx** and double-click **javafx:run**

**Option 2: Using IntelliJ**
1. Locate the `MainApp.java` file inside `src/main/java/edu/ntnu/idi/idatt2003/millions/view`
2. Right-click the file and select **Run MainApp.main()**

### Run the Tests

[//]: # (TODO: Describe how to run the tests here.)

1. Open the Maven tool window by clicking **View > Tool Windows > Maven**
2. Navigate to **Lifecycle** and double-click **test**

Alternatively, you can run tests from the command line:
```bash
mvn test
```

## How to Use the Application

Once the application starts:
1. **Start or Load a Game**: Enter a player name and starting capital, then click **Start Game**, or choose **Load Game** to resume a saved session.
2. **Browse the Market**: Use the **All Stocks**, **Watchlist**, and **Market Movers** tabs and the search bar to find stocks by symbol or company name.
3. **Manage Watchlist**: Click the star icon on a stock card to add or remove it from your watchlist.
4. **Buy or Sell Shares**: Click **Buy** or **Sell** on a stock card, enter a share quantity, and confirm the trade.
5. **Advance Time**: Click **Next Week** to progress the simulation and update prices.
6. **Track Progress**: The header shows cash, net worth, week, and status; the portfolio panel lists current holdings.
7. **Save Your Game**: Switch to **Profile** and click **Save Game**. Closing the app also auto-saves the active game.

Starting capital is chosen on the start screen, and prices are shown in USD.
