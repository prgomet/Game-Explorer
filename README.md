# 🎮 Game Explorer

A high-performance, gamer-themed Android app built with a "Tactical UI" and an **Offline-First** architecture. This project uses the RAWG API to deliver a seamless mission-discovery experience.

## 🚀 Key Features

*   **Personalized Onboarding:** Tailor your feed by selecting favorite genres (saved via DataStore).
*   **Offline-First:** Browsing works without internet using **Room + Paging 3 (RemoteMediator)** synchronization.
*   **Adaptive Layout:** Optimized for all screens.
*   **Premium UX:** Animations, custom pulsing loaders, and **Shared Element Transitions** between screens.

## 🏗️ Architecture & Tech Stack

The app follows **Clean Architecture** and **MVVM**.

*   **UI: Jetpack Compose** with Unidirectional Data Flow.
*   **Navigation:** Jetpack Navigation with support for Shared Element morphing.
*   **Data:** **Retrofit + Moshi** for network; **Room** for local persistence; **Hilt** for Dependency Injection.
*   **Robustness:** Centralized OkHttp interceptors for API key management and real-time network connectivity monitoring.

## 🛠️ Setup

1.  Clone the repo and open in Android Studio.
2.  Get an API key from [RAWG.io](https://rawg.io/apidocs).
3.  Add your key to `app/build.gradle.kts`.
4.  Sync and Run.