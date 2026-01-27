# EncoraAndroidProject_2
flowchart TD
    A[App Launch] --> B[LoginScreen]

    B -->|User clicks Sign-In| C[Google Sign-In Intent]
    B -->|Already logged in?| D[HomeScreen]

    C --> E[handleSignInResult()]
    E --> F[AuthViewModel.signInWithGoogle()]
    F --> G[Firebase Auth + DataStore]
    G --> H[_uiState = Success]
    H --> I[onLoginSuccess()]
    I --> D

    D --> J[NotesViewModel collects UID]

    D --> K[Logout (icon)]
    D --> L[Switch Account (icon)]

    K --> M[authViewModel.logoutKeepAccount()]
    M --> N[Clears session + DataStore UID]
    N --> B

    L --> O[authViewModel.switchAccount()]
    O --> P[Clears session + revokes Google token]
    P --> B
