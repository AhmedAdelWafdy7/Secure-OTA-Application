#include <iostream>
#include <fstream>
#include <sstream>
#include <iomanip>
#include <curl/curl.h>
#include <openssl/sha.h>
#include <mosquitto.h>
#include <string>
#include <cstdlib>
#include <nlohmann/json.hpp>

using json = nlohmann::json;

bool update_possible = false;

void on_connect(struct mosquitto *mosq, void *userdata, int rc) {
    std::string on_connect_msg  = "";
    if (rc == 0) {
        std::cout << "Connected to MQTT broker" << std::endl;
        mosquitto_subscribe(mosq, NULL, "ota/update", 0);
        mosquitto_subscribe(mosq, NULL, "ota/response", 0);
        // i should publish on ota/update_possible "" and should be retained
        mosquitto_publish(mosq, NULL, "ota/update_possible", on_connect_msg.size(), on_connect_msg.c_str(), 0, true);

    } else {
        std::cerr << "Failed to connect to MQTT broker" << std::endl;
    }
}

std::string get_checksum(const std::string& path) {
    unsigned char hash[SHA256_DIGEST_LENGTH];
    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    std::ifstream file(path, std::ifstream::binary);

    if (!file.is_open()) {
        throw std::runtime_error("Failed to open file");
    }

    char buffer[4096];

    while (file.read(buffer, sizeof(buffer))) {
        SHA256_Update(&sha256, buffer, file.gcount());
    }

    if (file.gcount() > 0) {
        SHA256_Update(&sha256, buffer, file.gcount());
    }

    SHA256_Final(hash, &sha256);
    std::ostringstream result;

    for (int i = 0; i < SHA256_DIGEST_LENGTH; ++i) {
        result << std::hex << std::setw(2) << std::setfill('0') << (int)hash[i];
    }

    return result.str();
}

void on_message(struct mosquitto *mosq, void *userdata, const struct mosquitto_message *msg) {
    std::string topic(msg->topic);
    std::string payload = std::string((char*)msg->payload, msg->payloadlen);

    if (topic == "ota/update") {
        auto json_payload = json::parse(payload);
        std::string file_url = json_payload.at("url").get<std::string>();
        std::string expected_checksum = json_payload.at("checksum").get<std::string>();

        std::cout << "Received file URL: " << file_url << std::endl;
        std::cout << "Expected checksum: " << expected_checksum << std::endl;

        CURL *curl;
        FILE *fp;
        CURLcode res;
        std::string outfilename = "HeadUnit_latest";

        curl = curl_easy_init();

        if (curl) {
            fp = fopen(outfilename.c_str(), "wb");
            curl_easy_setopt(curl, CURLOPT_URL, file_url.c_str());
            curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, NULL);
            curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);
            res = curl_easy_perform(curl);
            curl_easy_cleanup(curl);
            fclose(fp);

            if (res == CURLE_OK) {
                std::cout << "File downloaded successfully: " << outfilename << std::endl;

                std::string calculated_checksum;

                try {
                    calculated_checksum = get_checksum(outfilename);
                    std::cout << "Calculated checksum: " << calculated_checksum << std::endl;
                } catch (const std::exception &e) {
                    std::cerr << "Checksum calculation failed: " << e.what() << std::endl;
                    return;
                }

                if (calculated_checksum == expected_checksum) {
                    std::cout << "Checksum verification passed" << std::endl;

                    std::string update_possible_msg = R"({"status": "update_possible"})";
                    mosquitto_publish(mosq, NULL, "ota/update_possible", update_possible_msg.size(), update_possible_msg.c_str(), 0, false);
                } else {
                    std::cerr << "Checksum verification failed" << std::endl;
                }

            } else {
                std::cerr << "Failed to download file" << std::endl;
            }
        }
    } else if (topic == "ota/response") {
        // payload is verison so i should publish on ota/update_possible "updating" 
        std::string updating_msg = "updating";
        mosquitto_publish(mosq, NULL, "ota/update_possible", updating_msg.size(), updating_msg.c_str(), 0, false);
        system("kill -9 $(pgrep -f HeadUnit)");
        system("chmod +x ../../HeadUnit_latest");
        system("mv ../../HeadUnit_latest /usr/bin/HeadUnit");
        system("reboot");
    }
}

int main(int argc, char *argv[]) {
    mosquitto_lib_init();

    struct mosquitto *mosq = mosquitto_new(NULL, true, NULL);
    if (!mosq) {
        std::cerr << "Failed to create Mosquitto instance" << std::endl;
        return 1;
    }

    mosquitto_connect_callback_set(mosq, on_connect);
    mosquitto_message_callback_set(mosq, on_message);
    if (mosquitto_connect(mosq,"54.93.41.255", 1883, 60) != MOSQ_ERR_SUCCESS) {
        std::cerr << "Failed to connect to MQTT broker" << std::endl;
        return 1;
    }

    mosquitto_loop_forever(mosq, -1, 1);

    mosquitto_destroy(mosq);
    mosquitto_lib_cleanup();

    return 0;
}
