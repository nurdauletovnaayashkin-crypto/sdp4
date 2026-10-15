# Assignment #4 — Facade Design Pattern: Online Order Processing System

**Course:** Software Design Patterns (OP 6B06102)  
**Option:** Option B — Online Order Processing Facade  
**Language:** Java (JDK 17)

---

## 1. Project Overview

This project implements the **Facade Structural Design Pattern** in Java. It simulates a complex e-commerce **Online Order Processing System** where placing an order requires coordinating multiple subsystem services in a specific sequence:
1. `InventoryService`: Checking stock availability and reserving/releasing items.
2. `PaymentProcessor`: Processing credit card payments and handling refunds.
3. `ShippingService`: Creating and cancelling delivery shipments.
4. `NotificationService`: Sending email/SMS status notifications to customers.

The `OrderFacade` acts as a unified, simplified interface over these four subsystems, hiding orchestration complexity, transaction ordering, and rollback logic when a step fails.

---

## 2. Package Structure

```text
sdp4/
└── src/
    └── orderprocessing/
        ├── Main.java                       # Client entry point
        ├── facade/                         # Facade Pattern implementation
        │   ├── OrderFacade.java            # Main Facade
        │   ├── OrderConfirmation.java      # DTO for successful order result
        │   ├── OrderException.java         # Facade-level exception
        │   └── ProcessedOrder.java         # Internal record for active orders
        ├── model/                          # Domain Entities / Data Models
        │   ├── Cart.java
        │   ├── CartItem.java
        │   └── Customer.java
        └── subsystem/                      # Complex Subsystem Services
            ├── InventoryService.java
            ├── PaymentProcessor.java
            ├── ShippingService.java
            ├── NotificationService.java
            ├── ProductCatalog.java
            └── *Exception.java             # Subsystem-specific exceptions
            
## 3. Key Design Features & Rollback Mechanism
The OrderFacade handles execution flow and rollbacks automatically:

Successful Order (placeOrder): Reserves stock -> Charges payment -> Creates shipment -> Sends confirmation notification.

Payment Decline: If payment fails, reserved stock is automatically released (rollback).

Unsupported Shipping: If shipping fails, payment is refunded AND stock is released.

Order Cancellation (cancelOrder): Cancels shipment -> Refunds payment -> Releases stock -> Sends cancellation notification. Prevents double cancellation.

## 4. Clean Code Principles Applied
Meaningful Names: Method signatures like placeOrder(Cart cart, Customer customer) and cancelOrder(String orderId) clearly state intent without revealing underlying subsystem steps.

Single Responsibility Principle (SRP): OrderFacade is strictly responsible for orchestrating subsystem workflows. It does not reimplement inventory calculations, payment algorithms, or shipping logic.

No Leaking Subsystem Details: The client works with OrderFacade and higher-level objects (OrderConfirmation, OrderException) rather than handling low-level subsystem exceptions directly.

Small, Focused Subsystem Classes: Each service (InventoryService, PaymentProcessor, etc.) maintains a single responsibility and remains decoupled from the Facade.

Robust Error Handling & Rollbacks: Subsystem failures trigger predictable state recovery (releasing reserved stock, refunding money) before throwing high-level exceptions.

## 5. How to Run
Open the project in IntelliJ IDEA.

Ensure JDK 17 is selected in File -> Project Structure -> Project SDK.

Locate src/orderprocessing/Main.java.

Right-click Main.java and select Run 'Main.main()'.

## 6. Subsystem Direct Access (Defining Property of Facade)
Subsystem classes remain completely unaware of OrderFacade (no back-references). Advanced clients (e.g., admin tools, warehouse management) can still bypass the Facade and use InventoryService or PaymentProcessor directly.