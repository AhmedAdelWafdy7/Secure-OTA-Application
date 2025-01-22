# Secure-OTA-Application
This project outlines a comprehensive approach to implementing secure OTA updates, crucial for modern vehicles that rely heavily on software for their functionality. By wirelessly updating vehicle software and firmware, OTA technology offers a seamless method for enhancing vehicle capabilities and security. 


# Secure Over-The-Air (OTA) Update System for Vehicles

A robust implementation of a secure OTA update system designed for modern vehicles, addressing cybersecurity threats like MITM attacks, firmware spoofing, and rollback attempts. Built with a focus on compliance with **ISO 21434** and **UN R155** automotive cybersecurity standards.



## 🚀 Features
- **End-to-End Encryption**: TLS 1.3 for HTTPS/MQTT communication.
- **Mutual Authentication**: PKI with RSA-4096/ECC and X.509 certificates.
- **Firmware Integrity**: SHA-256 hashing + ECDSA digital signatures.
- **Rollback Protection**: Monotonic version counters + cryptographically signed downgrades.
- **Anomaly Detection**: Integration with Wazuh/Elastic SIEM for intrusion monitoring.
- **Secure Boot**: Firmware validation via TPM/HSM-secured keys.


## 📂 Project Structure
```bash
.
├── app/                   # Android/QML virtual vehicle application
│   ├── android_openssl    # OpenSSL integration for signature verification
│   └── src/               # QML/C++ logic for OTA client and UI
├── docker/                # Docker configurations for MQTT and server
│   ├── docker-compose.yml # MQTT (Mosquitto) + Flask server setup
│   └── dockerFile/
├── gateway/               # CAN/Ethernet communication simulator
│   └── info_client.cpp    # Secure bootloader and ECU update logic
├── server/                # OTA server (Flask/Python)
│   └── main.py            # Firmware upload, signing, and MQTT broadcast
└── README.md              # This file
```



## 🛠️ Installation

### Prerequisites
- Docker + Docker Compose
- OpenSSL
- MQTT Client Libraries (Paho)
- libcurl (for secure HTTPS downloads)

### Setup
1. **Generate TLS Certificates**:
   ```bash
   openssl req -x509 -newkey rsa:4096 -nodes -keyout server.key -out server.pem -days 365
   ```
2. **Configure Docker**:
   - Update `docker-compose.yml` with TLS cert paths.
   - Build containers:
     ```bash
     docker-compose up --build
     ```
3. **Configure MQTT Broker**:
   - Enable TLS in `mosquitto.conf`:
     ```ini
     listener 8883
     certfile /mosquitto/certs/server.pem
     keyfile /mosquitto/certs/server.key
     require_certificate true
     ```

---

## 🖥️ Usage

### Upload Firmware (Admin)
```bash
curl -X POST -H "Authorization: Bearer <JWT>" -F "firmware=@v2.0.bin" https://ota-server:5000/upload
```

### Simulate OTA Update
1. **Vehicle Client**:
   - Subscribes to MQTT topic `ota/updates/vehicles`.
   - Downloads firmware via HTTPS on update notification.
   - Validates signature and flashes via secure bootloader.

2. **Monitor Logs**:
   ```bash
   docker logs ota-server
   ```

---

## 🔒 Security Mechanisms
| Mechanism               | Implementation Details                     | Mitigated Threats               |
|-------------------------|--------------------------------------------|---------------------------------|
| **TLS 1.3**             | ECDHE-ECDSA-AES256-GCM-SHA384 cipher suite | Eavesdropping, MITM             |
| **PKI**                 | RSA-4096 keys + Let’s Encrypt CA           | Unauthorized server impersonation |
| **Digital Signatures**  | ECDSA with HSM-stored keys                 | Firmware tampering              |
| **Rollback Defense**    | Monotonic counters in firmware             | Downgrade attacks               |
| **Anomaly Detection**   | Wazuh monitoring for failed auth attempts  | Intrusion, DDoS                 |

---

## 🧪 Testing
- **Penetration Tests**: 
  ```bash
  # Simulate MITM attack
  msfconsole -q -x "use auxiliary/server/capture/http; set SSL true; run"
  ```
- **Anomaly Detection**:
  - Metrics: F1 score > 0.85 for intrusion detection.
  - Tools: Metasploit, Burp Suite, OWASP ZAP.

---

## 🤝 Contributing
1. Fork the repository.
2. Add features in separate branches (e.g., `feature/secure-boot`).
3. Submit PRs with detailed security impact analysis.

---

## 📜 License
MIT License - See [LICENSE](LICENSE) for details.
```

To complete the MIT License setup, create a `LICENSE` file in your project root with [this standard MIT License text](https://opensource.org/license/mit/).