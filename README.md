# Cast Near

This project is a native Android app that sends notifications to all users within 3km proximity when a user presses a button on their phone. Clicking on the notification leads the user to an activity with the broadcast message. 

## Screenshots

| ![photo_2023-02-14_16-50-24](https://user-images.githubusercontent.com/59611699/218722338-4c579ecc-77f6-4885-b207-e00c9d8a29e4.jpg) | ![photo_2023-02-14_16-50-22](https://user-images.githubusercontent.com/59611699/218722345-84efa85d-1129-4f47-be6c-dc2cb60b89cf.jpg) | ![photo_2023-02-14_16-50-21](https://user-images.githubusercontent.com/59611699/218722351-ba4ca791-ba97-4887-9d6e-7e0c3e3b7a6b.jpg) |
| ---------------------------------------------- | ---------------------------------------------- | ---------------------------------------------- |

## Features

- Sends notifications to all users within 3km proximity when a user presses a button
- Clicking on the notification leads the user to an activity with the broadcast message
- Uses Firebase Firestore for the database
- Uses Firebase Cloud Messaging for sending notifications
- Uses an open source UI template

## Getting Started

### Prerequisites

- Android Studio
- Firebase Account
- Google Play Services SDK

### Installation

1. Clone the repository from GitHub.
2. Open the project in Android Studio.
3. Configure Firebase by following the steps in the [Firebase documentation](https://firebase.google.com/docs/android/setup).
4. Build and run the app on an emulator or physical device.

## Usage

To use this app, follow these steps:

1. Press the button in the app to send a notification to all users within 3km proximity.
2. Click on the notification to view the broadcast message.

## Documentation

The app has the following directory structure:

- `app/src/main/java/com/example/castneat/` - contains the Java source code for the app
- `app/src/main/res/layout/` - contains the XML layout files for the app's user interface
- `app/src/main/res/values/` - contains XML files that define values used throughout the app

The following files are the most important for understanding the app:

- `MainActivity.java` - contains the code for the main activity that sends the notification to all users within 3km proximity
- `BroadcastActivity.java` - contains the code for the activity that displays the broadcast message
- `activity_main.xml` - contains the layout for the main activity
- `activity_broadcast.xml` - contains the layout for the broadcast activity

The app uses Firebase Firestore to store information about users' proximity and Firebase Cloud Messaging to send targeted notifications. The `sendNotification()` method in `MainActivity.java` uses a cloud function to send the notification to all users within 3km proximity.

## Roadmap

- Implement unit tests for the app
- Improve the UI with custom graphics and animations

## Contributing

Contributions to this project are welcome! To contribute, please follow these steps:

1. Fork the repository on GitHub.
2. Create a new branch.
3. Make your changes.
4. Test your changes.
5. Create a pull request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
