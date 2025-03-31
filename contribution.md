
## Week 13 (March 31, 2025)


### 🧑‍💻 Richard
- ✅ Fixed NullPointerException in `setDateOfBirth` method || Bug Fix ||

  (HASH: f33b7ae1e2b27039ced91dc5591f547974391c6a)

- ✅ Write integration tests to verify the end-to-end login and logout process || Issue:#38 |$

1. Login Tests

✅ Valid Credentials

Implemented a test case for a successful login when valid credentials are provided.

Mocked the UserService methods to simulate correct user authentication.

Verified that the response contains a success flag, a valid token, and the correct user ID.

❌ Invalid Credentials

Implemented a test case for an unsuccessful login attempt with invalid credentials.

Ensured that the response correctly returns success: false without a token.

Validated that the system does not authenticate users with incorrect passwords.

2. Logout Tests

✅ Successful Logout

Implemented a test case to verify that a logged-in user can log out successfully.

Checked that the system responds with a success message and updates the user's status to OFFLINE.

❌ Invalid Token

Implemented a test case for logout failure due to missing or invalid tokens.

Ensured that the system returns a 400 Bad Request response with an appropriate error message.

  (HASH: 5407c66a768a650d5e39b6195b81746b765931b6)

### 🧑‍💻 Daniel
- ✅ 
- ✅ 

### 🧑‍💻 Nikola
- ✅ 
- ✅ 

### 🧑‍💻 Ilias
- ✅ 
- ✅


  ## Week 14 (April 7, 2025)
