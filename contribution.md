# Contribution Report

## Week 13 (March 31, 2025)

### 🧑‍💻 Richard

#### ✅ Fixed NullPointerException in `setDateOfBirth` method
- **Type:** Bug Fix
- **Commit Hash:** `f33b7ae1e2b27039ced91dc5591f547974391c6a`

#### Implemented Integration Tests for Login and Logout
- **Type:** Issue #38
- **Commit Hash:** `5407c66a768a650d5e39b6195b81746b765931b6`

#### Login Tests

**Valid Credentials**
- Implemented a test case for a successful login when valid credentials are provided.
- Mocked the `UserService` methods to simulate correct user authentication.
- Verified that the response contains a success flag, a valid token, and the correct user ID.

**Invalid Credentials**
- Implemented a test case for an unsuccessful login attempt with invalid credentials.
- Ensured that the response correctly returns `success: false` without a token.
- Validated that the system does not authenticate users with incorrect passwords.

#### Logout Tests

**Successful Logout**
- Implemented a test case to verify that a logged-in user can log out successfully.
- Checked that the system responds with a success message and updates the user's status to OFFLINE.

**Invalid Token**
- Implemented a test case for logout failure due to missing or invalid tokens.
- Ensured that the system returns a `400 Bad Request` response with an appropriate error message.

---

### 🧑‍💻 Daniel
- ✅ Task 1 (Pending Details)
- ✅ Task 2 (Pending Details)

### 🧑‍💻 Nikola
# Commit a746660967007e1097e2f71ba3d870354637e6b5

## Updated build.gradle  
**Type:** Improvement  
Enhanced the dependency configuration by adding the jbcrypt library for secure password hashing.

## Enhanced UserController with deleteUser endpoint  
**Type:** Feature  
Implemented and refined the deleteUser endpoint to:  
- Check for a valid Authorization token and return a 403 Forbidden status if missing.  
- Verify that the user making the request matches the user to be deleted, returning an appropriate error message if there is a mismatch.  
- Handle exceptions by catching UserNotFoundException and responding with a 404 Not Found status when the specified user does not exist.

## Updated UserService for secure password handling and user deletion  
**Type:** Bug Fix / Improvement  
Improved the user service by:  
- Replacing the insecure plain text password comparison with BCrypt.checkpw to ensure secure validation of user credentials.  
- Implementing robust exception handling in the deleteUser method to provide clear error messages when a user is not found.

## ✅ Extended UserControllerTest with additional test cases  
**Type:** Testing  
Added comprehensive test cases to cover:  
- Successful deletion of a user account when a valid token is provided and the correct user is authenticated.  
- The scenario where deletion fails because an unauthorized user attempts to delete another account, ensuring a 403 Forbidden response is returned.  
- Enhanced login tests to validate both successful and failed login attempts, ensuring that the system correctly distinguishes between valid and invalid credentials.







### 🧑‍💻 Ilias
- ✅ Task 1 (Pending Details)
- ✅ Task 2 (Pending Details)

## Week 14 (April 7, 2025)
*Pending updates...*

