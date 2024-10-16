Library Management System
======================
Candidate: Thurston George Davis
## Prerequisites: <br />
Will need Maven and Java 21 installed
## To build and run the application: <br />
Either with maven spring-boot plugin

```
mvn spring-boot:run
```
Or building/running the jar<br />
```
mvn clean install
java -jar target/library-management-1.0-0.jar
```
To see if the application is up and running
```
curl localhost:8080/library-api/health
```
## API Endpoints <br />
1. Create a Book 
```
curl -X POST \
  http://localhost:8080/library-api/v1/books \
  -H 'Accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{"isbn": "123","title": "title","author": "auth", "publicationYear": 2024, "availableCopies": 10}'
```
2. remove a book DELETE: /library-api/v1/books/{isbn}
```
curl -X DELETE \
  http://localhost:8080/library-api/v1/books/123 \
  -H 'Accept: application/json' \
  -H 'Content-Type: application/json'
```
3. findBookByISBN GET: /library-api/v1/books/{isbn}
```
curl -X GET \
  http://localhost:8080/library-api/v1/books/123 \
  -H 'Accept: application/json' \
  -H 'Content-Type: application/json'
```
4. findBooksByAuthor GET: /library-api/v1/books?author={author}
```
curl -X GET \
  http://localhost:8080/library-api/v1/books?author=auth \
  -H 'Accept: application/json' \
  -H 'Content-Type: application/json'
```
5. borrowBook PUT: /library-api/v1/books/{isbn}/borrow
```
curl -X PUT \
  http://localhost:8080/library-api/v1/books/123/borrow \
  -H 'Accept: application/json' \
  -H 'Content-Type: application/json'
```
6. returnBook PUT: /library-api/v1/books/{isbn}/return
```
curl -X PUT \
  http://localhost:8080/library-api/v1/books/123/return \
  -H 'Accept: application/json' \
  -H 'Content-Type: application/json'
```
