# Readme

## Quick Start

### Prerequisites

* [Java 11 JDK or higher](https://openjdk.java.net/install/)

### Start Service Steps
1. Pull code from this repository to your local.
2. Change directory to acmebank
3. On MacOS/Linux `./mvnw package` Or On Windows `.\mvnw.cmd package`
4. On MacOS/Linux `./mvnw spring-boot:run` Or On Windows `.\mvnw.cmd spring-boot:run`

Or 
1. Import this project to IntelliJ
2. Right click src/main/java/com/acmebank/accountsServices/AccountsApplication to show menu.
3. click Run 'AccountsApplic...'

## Endpoints
* GET http://localhost:8080/account/{id}
* POST http://localhost:8080/accounts/transfer

`Request body of POST http://localhost:8080/accounts/transfer`
```json
{
    "fromAccountId": 88888888,
    "toAccountId": 12345678,
    "amount": 100
}
```
## Assumptions

* Assumed authorization has been done on gateway level.
* Assumed this service is a microservice to operate accounts belong to same user.
* Assumed low latency is not critical to this service, use database lock to entity for money transfer.
* In this project, I implemented the 'get the balance of my account' feature first. I have three reasons:
  1. As required to have initial balance of 1,000,000 HKD in accounts, I could establish H2 database in this project first.
  2. After database established, I need to think about the entity model of required which helps to build all the building blocks I need in this project.
  3. I chose to use Spring Data with H2 which simplify this implementation after I defined all the entities.
* Cannot use `account-manager` in package name, I used `account_manager` instead.