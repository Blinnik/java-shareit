# ShareIt
An app for renting things. You can exchange things with friends for a while: tools, gadgets, books, etc.

## Technology stack

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

## Structure

The project consists of two modules:
+ the **gateway** performs all the validation of requests — incorrect ones will be excluded;
+ the **server** contains all the basic logic.

## Functionality

+ adding a new thing, editing a thing, viewing information about a specific thing by its identifier, viewing by the owner a list of all his things with a name and description for each;
+ search for things for potential tenants;
+ adding a new booking request, confirming or rejecting a booking request, getting data about a specific booking, getting a list of all bookings of the current user, getting a list of bookings for all things of the current user;
+ adding a new request for something, getting a list of user requests along with data about responses to them, getting a list of requests created by other users, getting data about one specific request.

## Setup
1. [Install Java 11 JDK](https://hg.openjdk.org/jdk/jdk11)
2. [Install Docker](https://www.docker.com/)
3. Clone this repository to your local machine
```shell
git clone git@github.com:Blinnik/java-shareit.git
```
4. Generate module jars
```shell
mvn install
```
5. Run Docker Compose (pre-launching Docker)
```shell
docker compose up
```