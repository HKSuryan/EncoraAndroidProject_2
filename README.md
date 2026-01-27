# EncoraAndroidProject_2
                  +----------------+
                  |   App Launch   |
                  +----------------+
                          |
                          v
                  +----------------+
                  |  LoginScreen   |
                  +----------------+
                          |
           +--------------+----------------+
           |                               |
   User clicks Sign-In             Already logged in?
           |                               |
           v                               v
+------------------------+          auto navigate
| Google Sign-In Intent  |----------------------+
+------------------------+                      |
           |                                    v
           v                         +----------------+
  handleSignInResult()                |   HomeScreen   |
           |                         +----------------+
           v                                   |
AuthViewModel.signInWithGoogle()               |
           |                                   |
    Firebase Auth + DataStore                  |
           |                                   |
        _uiState = Success                     |
           |                                   |
           v                                   v
     onLoginSuccess()                     NotesViewModel collects UID
           |                                   |
           v                                   |
      Navigate to HomeScreen                  |
           |                                   |
           +----------------------+------------+
                                  |
                  +---------------+-----------------+
                  |                               |
            Logout (icon)                    Switch Account (icon)
                  |                               |
 authViewModel.logoutKeepAccount         authViewModel.switchAccount
                  |                               |
 Clears session + DataStore UID       Clears session + revokes Google token
                  |                               |
 Navigate to LoginScreen             Navigate to LoginScreen + account picker
