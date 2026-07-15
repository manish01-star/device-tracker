\# 🚀 Device Tracker



A complete \*\*Android Device Tracking System\*\* built with \*\*Spring Boot\*\*, \*\*Android (Java)\*\*, \*\*MySQL\*\*, and \*\*WebSocket\*\*.



This project enables Android devices to register with a backend server, send real-time location updates, monitor battery status, and display live device information through a web dashboard.



\---



\## 📱 Project Overview



The Device Tracker consists of two main components:



\- \*\*Android Application\*\* – Collects device information and sends live location updates.

\- \*\*Spring Boot Backend\*\* – Stores device data, manages tracking configurations, and provides a web dashboard.



\---



\## ✨ Features



\### 📱 Android Application



\- ✅ Device Registration

\- ✅ Live GPS Location Tracking

\- ✅ Background Location Updates using WorkManager

\- ✅ Battery Percentage Monitoring

\- ✅ Device Information Collection

\- ✅ Runtime Permission Handling

\- ✅ Retrofit API Integration

\- ✅ Automatic Tracking Configuration Sync



\---



\### 🌐 Spring Boot Backend



\- ✅ REST APIs

\- ✅ Device Registration API

\- ✅ Update Device Location API

\- ✅ Device Information Management

\- ✅ Location History

\- ✅ Reverse Geocoding (Latitude → Address)

\- ✅ WebSocket Support

\- ✅ Live Dashboard



\---



\### 📊 Dashboard



\- 📍 Live Device Location

\- 🔋 Battery Percentage

\- 📱 Device Information

\- 🗺 Interactive Map (Leaflet)

\- 📜 Location History

\- 📈 Device Status Monitoring



\---



\# 🛠 Tech Stack



\## Android



\- Java

\- Android SDK

\- Retrofit

\- WorkManager

\- Google Location Services



\## Backend



\- Spring Boot

\- Spring Data JPA

\- REST APIs

\- WebSocket

\- Maven



\## Database



\- MySQL



\## Frontend Dashboard



\- HTML

\- CSS

\- JavaScript

\- Bootstrap 5

\- Leaflet.js



\---



\# 📁 Project Structure



```

device-tracker

│

├── android-app

│   ├── app

│   ├── gradle

│   ├── gradlew

│   └── ...

│

├── device-tracker-api

│   ├── src

│   ├── pom.xml

│   ├── .mvn

│   └── ...

│

└── README.md

```



\---



\# ⚙️ Getting Started



\## 1️⃣ Clone Repository



```bash

git clone https://github.com/manish01-star/device-tracker.git

```



\---



\## 2️⃣ Run Backend



```bash

cd device-tracker/device-tracker-api

```



Configure your database in:



```

src/main/resources/application.properties

```



Run the application:



```bash

mvn spring-boot:run

```



Backend URL



```

http://localhost:8080

```



\---



\## 3️⃣ Run Android App



```bash

cd device-tracker/android-app

```



Open the project in \*\*Android Studio\*\*.



Update your backend IP inside:



```

ApiClient.java

```



Build and run the application on your Android device.



\---



\# 🔄 Project Workflow



```text

Android App

&#x20;     │

&#x20;     ▼

Spring Boot REST APIs

&#x20;     │

&#x20;     ▼

MySQL Database

&#x20;     │

&#x20;     ▼

Web Dashboard

```



\---



\# 📡 REST APIs



\- Register Device

\- Update Device Location

\- Get Device Details

\- Get Tracking Configuration

\- Dashboard APIs



\---



\# 🚀 Future Enhancements



\- 🔐 JWT Authentication

\- 👥 Multi User Support

\- 🔔 Push Notifications

\- 📍 Geofencing

\- 📊 Analytics Dashboard

\- ☁ Cloud Deployment

\- 📈 Device Activity Reports



\---



\# 📷 Screenshots



> Screenshots will be added soon.



\- Android App

\- Dashboard

\- Live Map

\- Device Details



\---



\# 🤝 Contributing



Contributions, suggestions, and improvements are welcome.



1\. Fork the repository

2\. Create a new branch

3\. Commit your changes

4\. Push your branch

5\. Open a Pull Request



\---



\# 👨‍💻 Author



\*\*Manish\*\*



Software Developer



\### Skills



\- Java

\- Spring Boot

\- Android

\- REST APIs

\- MySQL

\- Git

\- GitHub



GitHub:

https://github.com/manish01-star



\---



\## ⭐ Support



If you found this project useful, please consider giving it a ⭐ on GitHub.

