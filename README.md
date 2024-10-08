# Game Developers Platform Mobile

Welcome to the Game Developers Platform Mobile repository! This Android application provides a mobile interface for the Web Game Developers Platform, allowing users to manage their game profiles and interact with the platform on mobile devices.

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Testing](#testing)
- [License](#license)

## Project Overview

The Game Developers Platform Mobile is an Android application built using Kotlin. It connects to the backend API to provide a seamless mobile experience for users to manage games, view profiles, and more.

## Features

- **User Authentication:** Secure login and registration.
- **Game Management:** View, add, and edit game details.
- **Profile Management:** View and update user profiles.
- **Responsive UI:** Designed for a smooth experience on Android devices.

## Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/liorhassin/GameDevelopersPlatform-Mobile.git
   ```

2. **Open the project in Android Studio:**

    Launch Android Studio and open the project directory.

3. **Install dependencies:**

    Android Studio will automatically handle dependencies specified in build.gradle files.

4. **Run the app:**

    Use the "Run" button in Android Studio to build and deploy the app to an emulator or physical device.

## Usage

- **Launching the App:** Open the app on your Android device or emulator.
- **Features:** Navigate through the app to explore functionalities like game management and profile updates.

## Configuration

- **API URL:** Update the API URL in the BuildConfig or Constants file to match your backend server:

  ```kotlin
  const val BASE_URL = "http://localhost:5000/api"
  ```
  Replace http://localhost:5000/api with the URL of your backend server.

- **Environment Variables:** You can define environment-specific variables in gradle.properties or directly in your code.

## Testing

- **Unit Tests:** Place unit tests in the src/test directory.

- **Instrumented Tests:** Place instrumented tests in the src/androidTest directory.

- **Running Tests:** Use Android Studio or the command line to execute tests:

  ```bash
  ./gradlew test
  ./gradlew connectedAndroidTest
  ```

## License

This project is licensed under the MIT License.
