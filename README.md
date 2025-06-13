CryptXpress

**CryptXpress** is a Java-based file encryption and decryption desktop application featuring support for multiple cryptographic algorithms, a Swing-based GUI, database-backed operation logging, and modular design .

---



1. Core Feature Implementation 
- Supports **AES-256**, **Blowfish-128** algorithms.
- Modular architecture separates GUI, encryption logic, and database operations.
- Provides seamless **file encryption/decryption** with password protection and key-based handling.

2. Error Handling and Robustness 
- Catches invalid file types, incorrect keys, missing configurations.
- Uses exception classes like `CryptoException` for controlled failure points.
- Gracefully handles DB connectivity and IO exceptions.

3. Integration of Components 
- Combines GUI (Swing + FlatLaf) with backend cryptographic logic and MySQL DB.
- Integrated DAO pattern for logging encryption operations in a persistent store.

4. Event Handling and Processing 
- Event-driven interface with responsive **buttons**, **progress bars**, and **dialogs**.
- GUI responds instantly to user actions like file selection, method switching, etc.

5. Data Validation 
- Validates:
  - File existence
  - Key/password format
  - DB connectivity
- Prevents empty or invalid fields in GUI inputs.

6. Code Quality & Innovative Features 
- Java 17 with **clean code practices**, modular folder structure, and thread-safe DB access.
- Innovative: Supports **multiple encryption schemes** with user-selectable options.
- FlatLaf theme for modern UI and randomly generating pixel arts.

7. Project Documentation 
- This `README.md` provides:
  - Setup instructions
  - Feature breakdown
  - Use

---

Features

* AES-256, Blowfish-128, and ChaCha20 encryption support
* Swing GUI with FlatLaf theming
* Secure key generation with base64 encoding
* Operation history logging to a MySQL database
* Configurable via `application.properties`
* Thread-safe database connection pooling
* Modular Java 17 codebase following best practices

---

Prerequisites

Ensure the following are installed:

* Java 17 (LTS)
* IntelliJ IDEA (recommended)
* Maven (automatically handled by IntelliJ)
* MySQL Server (local)

---

Project Setup

1. Clone the Repository

Clone the project or extract the provided zip file into your working directory.

```
git clone https://github.com/your-repo/CryptXpress.git
```

2. Import into IntelliJ

* Open IntelliJ IDEA.
* Select **Open** and navigate to the extracted folder.
* IntelliJ will automatically detect the Maven project and import dependencies.

3. Configure SDK and Language Level

* Go to **File → Project Structure → Project**
* Set **Project SDK** to Java 17
* Set **Project language level** to **17 (Sealed types, always-strict floating point semantics)**

---

Database Configuration

1. Create Database

Use the provided SQL script:

```sql
database/setup.sql
```

Execute the script in your MySQL client or IDE to create:

* Database: `file_encryptor_db`
* Table: `operation_history`
* User: `cryptxpress` with limited permissions

2. Configure Credentials

Open:

```
src/main/resources/application.properties
```

Ensure the following entries match your environment:

```properties
db.url=jdbc:mysql://localhost:3306/file_encryptor_db
db.username=cryptxpress
db.password=U2VjdXJlUGFzczEyMyE=
```

> Note: The password is base64 encoded. You may update it accordingly and re-encode.

---

Running the Application

1. Set the Main Class

In IntelliJ:

* Go to **Run → Edit Configurations**
* Add a new **Application** configuration
* Set the main class to:

```
com.encryptor.ui.MainFrame
```

* Choose the module and Java 17 SDK
* Apply and run

Alternatively, right-click `MainFrame.java` → **Run**

---

Using CryptXpress

1. Launch the GUI
2. Select a file to encrypt or decrypt
3. Choose the encryption method
4. Enter or save the key
5. View operation status and history

---

Project Structure Overview

```
src/main/java/com/encryptor/
├── dao/OperationDAO.java
├── model/OperationRecord.java
├── service/
│   ├── EncryptionService.java
│   ├── FileEncryptor.java
│   └── CryptoUtils.java
├── ui
     |___/MainFrame.java
     |___/CryptoController.java
      
└── util/DatabaseConnection.java

src/main/resources/
└── application.properties

database/
└── setup.sql
```

Notes

* Compatible with Java 17 and Maven standard directory structure
* Uses Bouncy Castle for cryptographic support (configured via Maven)
* Supports future expansion with additional algorithms and logging features
