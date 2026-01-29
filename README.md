# EncoraAndroidProject_2
```mermaid
flowchart TD
    A(App Launch) --> B(LoginScreen)

    B -->|User clicks Sign-In| C(Google Sign-In Intent)
    B -->|Already logged in?| D(HomeScreen)

    C --> E(handleSignInResult)
    E --> F(signInWithGoogle in AuthViewModel)
    F --> G(Firebase Auth + DataStore)
    G --> H(_uiState = Success)
    H --> I(onLoginSuccess)
    I --> D

    D --> J(NotesViewModel collects UID)

    D --> K(Logout Icon)
    D --> L(Switch Account Icon)

    K --> M(logoutKeepAccount)
    M --> N(Clears session + DataStore UID)
    N --> B

    L --> O(switchAccount)
    O --> P(Clears session + revokes Google token)
    P --> B
