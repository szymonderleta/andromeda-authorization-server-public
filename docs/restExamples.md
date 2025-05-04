### POST request to login with username and get cookie with jwtToken
POST http://localhost:8087/api/v3/auth/login
Content-Type: application/json
X-Requesting-App: nebula_rest_api
{
"login": "user",
"password": "password"
}
###

### POST request to login with email and get cookie with jwtToken
POST http://localhost:8087/api/v3/auth/login
Content-Type: application/json
X-Requesting-App: nebula_rest_api
{
"login": "user@local.com",
"password": "password"
}
###

### GET request token page using cookie authorization
GET http://localhost:8087/api/v1/table/tokens?page=0&size=5
Content-Type: application/json
Cookie: jwtToken=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcxOTYxOTQsImV4cCI6MTczNzE5OTc5NH0.8cu9zV7EfnV3eo51O4t371JqZB4QMZDFdscp4PEEOjX_nbicVyei0rsRT4r9AuFOk_WGlyFodXYRqDMVYUC4OA
X-Requesting-App: nebula_rest_api
###

### GET request roles with name filter with cookie
GET http://localhost:8087/api/v1/table/roles?roleNameFilter=er
Content-Type: application/json
X-Requesting-App: nebula_rest_api
Cookie: jwtToken=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcxOTYxOTQsImV4cCI6MTczNzE5OTc5NH0.8cu9zV7EfnV3eo51O4t371JqZB4QMZDFdscp4PEEOjX_nbicVyei0rsRT4r9AuFOk_WGlyFodXYRqDMVYUC4OA
###

### GET request roles with name filter with bearer authorization
GET http://localhost:8087/api/v1/table/roles?roleNameFilter=er
Content-Type: application/json
X-Requesting-App: nebula_rest_api
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcxOTYxOTQsImV4cCI6MTczNzE5OTc5NH0.8cu9zV7EfnV3eo51O4t371JqZB4QMZDFdscp4PEEOjX_nbicVyei0rsRT4r9AuFOk_WGlyFodXYRqDMVYUC4OA
###
