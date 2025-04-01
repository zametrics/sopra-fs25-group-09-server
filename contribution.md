# Contribution Report

## Week 13 (March 31, 2025)

### 🧑‍💻 Richard

#### Fixed NullPointerException in `setDateOfBirth` method
- **Type:** Bug Fix
- **Commit Hash:** `f33b7ae1e2b27039ced91dc5591f547974391c6a`

#### Implemented Integration Tests for Login and Logout
- **Type:** Issue #38
- **Commit Hash:** `5407c66a768a650d5e39b6195b81746b765931b6`

### Added Tets

**Valid Credentials**
- Implemented a test case for a successful login when valid credentials are provided.
- Mocked the `UserService` methods to simulate correct user authentication.
- Verified that the response contains a success flag, a valid token, and the correct user ID.

**Invalid Credentials**
- Implemented a test case for an unsuccessful login attempt with invalid credentials.
- Ensured that the response correctly returns `success: false` without a token.
- Validated that the system does not authenticate users with incorrect passwords.


**Successful Logout**
- Implemented a test case to verify that a logged-in user can log out successfully.
- Checked that the system responds with a success message and updates the user's status to OFFLINE.

**Invalid Token**
- Implemented a test case for logout failure due to missing or invalid tokens.
- Ensured that the system returns a `400 Bad Request` response with an appropriate error message.

--

#### Extended UserControllerTest with Delete User Test Cases
- **Type:** Issue #40
- **Commit Hash:** `bd64c76bc52ccc11fb525cd33f403f62b1c486c9` 

**Delete User Tests**
- Implemented `deleteUser_missingToken_forbidden` to confirm that requests without an Authorization token are rejected with a `403 Forbidden` response and "Not authorized to delete user" message.
- Implemented `deleteUser_tokenNotFound_notFound` to test that an invalid token results in a `404 Not Found` response when the user cannot be authenticated, with an appropriate error message.

---

### 🧑‍💻 Daniel
- ✅ Task 1 (Pending Details)
- ✅ Task 2 (Pending Details)

### 🧑‍💻 Nikola
#### Updated build.gradle
- **Type:** Improvement
- **Commit Hash:** `a746660967007e1097e2f71ba3d870354637e6b5`

**Dependency Update**
- Added the jbcrypt library for secure password hashing.
- Replaced the commented-out Spring Security dependency for password handling.



#### Enhanced UserController with deleteUser endpoint
- **Type:** Issue #44
- **Commit Hash:** `a746660967007e1097e2f71ba3d870354637e6b5`

**Endpoint Implementation**
- Implemented a deleteUser endpoint that validates the Authorization token.
- Checks that the user making the request matches the user to be deleted.
- Returns a 403 Forbidden status if the token is missing or the user IDs do not match.
- Catches UserNotFoundException and responds with a 404 Not Found status when the specified user is not found.



#### Updated UserService for secure password handling and user deletion
- **Type:** Issue #42 / Issue #22
- **Commit Hash:** `a746660967007e1097e2f71ba3d870354637e6b5`

**Password and Deletion Improvements**
- Replaced the insecure plain text password comparison with BCrypt.checkpw for secure credential validation.
- Improved exception handling in the deleteUser method to provide clear error messages when a user is not found.



#### Extended UserControllerTest with additional test cases
- **Type:** Testing #41 / testing #39
- **Commit Hash:** `a746660967007e1097e2f71ba3d870354637e6b5`

**Delete User Tests**
- Added test cases to verify successful deletion when a valid token is provided and the correct user is authenticated.
- Implemented tests ensuring that an unauthorized deletion attempt returns a 403 Forbidden response.

**Login Tests**
- Enhanced login tests to cover both valid and invalid credential scenarios.
- Confirmed that valid credentials result in successful authentication, while invalid credentials are correctly rejected.

---






### 🧑‍💻 Ilias
- ✅ Task 1 (Pending Details)
- ✅ Task 2 (Pending Details)

## Week 14 (April 7, 2025)
*Pending updates...*

