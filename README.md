<h1 align="center"> Secure Over-The-Air (OTA) Update System for Vehicles </h1>


# Introduction

This project presents a comprehensive and secure Over-The-Air (OTA) update system tailored for modern vehicles, where software plays a vital role in overall functionality and safety.

Using a mobile application that communicates with the vehicle’s ECU via the `Mosquitto` MQTT broker, this system enables reliable and wireless delivery of software and firmware updates.

It is designed with cybersecurity at its core—mitigating threats such as Man-in-the-Middle (MITM) attacks, firmware spoofing, and rollback exploits—while adhering to critical industry standards like **ISO 21434** and **UN R155** for automotive cybersecurity compliance.

---

# System Overview

This OTA system is composed of four main components:

- **Mobile App**: Interfaces with the user and displays update information.
- **ECU (Electronic Control Unit)**: Downloads and installs updates.
- **Server**: Hosts update files and coordinates messaging.
- **MQTT Broker (Mosquitto)**: Manages communication between all three nodes.

---

# Update Flow

1. **Uploading the Update**  
   A new update file is uploaded to the server using `curl`:

   ```bash
   curl -F "file=@<update-file-version>.bin" https://<our-server-ip-address>:80/upload
   ```

2. **Announcing the New Update**    
    Once uploaded, the server publishes the update meta-data to the `ota/update` topic.

3. **ECU Receives Announcement**    
    The `ECU` is already subscribed to the `ota/response` topic. Once it receives the message, it publishes the update meta-data to the `ota/update_possible` topic as a retained message, notifying the app of a pending update.

4. **App Displays Update**  
    The app, subscribed to the `ota/update_possible` topic, receives the new update announcement and shows update details in the UI. If the user clicked `install`, the app will publish the update meta-data to the `ota/response` topic.

5. **ECU Initates Download**    
    Subscribed to `ota/response`, the ECU reacts by downloading the update using `curl`:

    ```bash
    curl -O https://<our-server-ip-address>:80/files/<update-file-version>.bin
    ```
    During the download, the `ECU` publishes "updating" to the `ota/update_possible` topic as a retained message. This informs the app that the update is in progress.

6. **Validate and Finish**  
Once downloaded, the ECU validates the file using a SHA checksum. If valid, it clears the update status by publishing an empty retained message to the `ota/update_possible` topic. This informs the app that the system is now up-to-date.

---

# System Architecture

![Architecture](Docs/images/ota_arch.png)

---

# Installation and Usage

## Prerequisites

- Docker + Docker Compose
- Mosquitto MQTT Broker
- Python3 and Pip
- Android Studio and Gradle

## Installation
Open a new terminal and clone the repository:

```bash
# Clone the repository
git clone https://github.com/muhammadzkralla/Secure-OTA-Application
cd Secure-OTA-Application
```

## Docker
If you have docker installed, you can build the containers by:

```bash
docker-compose up --build
```

## Mosquitto MQTT Broker
Open a new terminal and type this to install Mosquitto MQTT Broker:

```bash
sudo apt update
sudo apt install mosquitto mosquitto-clients
sudo systemctl enable mosquitto
sudo systemctl start mosquitto
```

Check if Mosquitto is running:
```bash
sudo systemctl status mosquitto
```

## Flask Server
Open a new terminal and type this to start the Flask file server:

```bash
cd server
sudo pip3 install flask flask-restx pymysql paho-mqtt
sudo python3 main.py
```


## Android App

1- Open the android app project in Android Studio. <br>
2- Sync the project and wait for gradle to download dependencies. <br>
3- Modify the MQTT `BROKER_URL` constant in the constants directory to your MQTT Broker ip address. <br>
4- Open your AVD or connect your Android phone and make sure ADB is working. <br>
5- Run the project. <br>
