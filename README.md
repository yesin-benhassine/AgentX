API DOCUMENTATION


8/1/2025
Endpoints:
GET /api/auth/company/create-company:

Graphql inspired endpoint, 
a request to it is like this:
    GET http://localhost:8080/api/auth/company/create-company
    query params:
    name:string
    email:string

POST /api/auth/company/register:
    this endpoint is for registering new team members, it's still not permission based as 
    we've not yet decided the permission roles
    but it takes in a json
    having 
    username*
    email*
    password*
    companyId*
    street address
    city
    state
    zip code
    country
    phone number
    ps:fields marked with "*" are compulsory 
POST /api/auth/company/login:
    takes 
    email*
    password*
    and does the authentication and returns 
    a success/failure message
    and sets httpOnly cookies named
    "refresh_token"
    "access_token"

Since we're in the topic of authentication here's the flow


The custom user details service uses email based identification and not username based identification,
when a user is registered their email gets a prefix
one of two:"admin:" or "company:"
the current implementation has the correct methodology to authenticate via the email directly without the user adding the prefix so no worries in that regard, 
authenticated endpoints do accept
Bearer <Access Token|Refresh_Token> Authorization header 
however it's absolutely unnecessary, the way it is now it's httpOnlyCookies so authentication flow for the frontend is very seamless and direct (question for the team:
i can make the jwt filter check if the access token is expired and refresh it automatically because each request carries it's refresh and access tokens so we can automate this even further but since i haven't seen this flow before i decided against implementing it without consulting
)

back to the endpoints:

/api/auth/admin/register:
this endpoint requires the role "ROLE_SUPER_ADMIN"
and an authenticated superuser
currently the superuser is generated using a public endpoint
/api/auth/admin/make-super-user

later on i plan on making a mini cli tool to generate a superuser, the current implementation generates a super user
with email "admin@admin.admin" and password "admin123"

the payload the registration endpoint takes is

username*
password*
email*
address
city
state
zip code
country
phone number



POST /api/auth/admin/login
this is the same as the company user login with the same functionality and accessibility



/GET /api/auth/refresh

this endpoint is a simple endpoint that is public and works with both 
Authorization header tokens and httpOnly cookies(i prefer the latter for simplicity)
so a logged in user with an expired access token just requests a refresh and if they meet the criteria it resets the httpOnly cookies(one thing is, even thought authorization header is allowed, no endpoint returns the jwt explicitly)

debugging endpoints:

GET /api/auth/company/verify?token=...
this endpoint serves as a jwtDecode to test tokens and get some data on the logged in user
it's broken since now tokens are httpOnly and inaccessible so i'll remove it next time 


