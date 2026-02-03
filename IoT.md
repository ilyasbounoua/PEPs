# Documentation Intégration IoT (ESP32) -> Backend

Ce document récapitule les modifications apportées au backend pour permettre la communication avec les cartes ESP32 et fournit un guide d'implémentation pour le client.

## 🛠 Modifications Backend

### Sécurité (Spring Security)
Le fichier `applicationContext.xml` a été modifié pour créer une exception de sécurité. Comme l'ESP32 ne peut pas gérer facilement les sessions ou les jetons CSRF, le chemin suivant a été ouvert :

- **Path :** `/modules/**`
- **Action :** `security="none"` (Filtres de sécurité désactivés)
- **Impact :** Les requêtes POST vers cet endpoint ne nécessitent plus de jeton CSRF ni d'authentification.

---

## 📡 Guide d'implémentation ESP32

L'ESP32 doit envoyer une requête **HTTP POST** avec un corps au format **JSON** correspondant à la structure `ModuleDTO`.

### Configuration Requise (Arduino IDE)
* **Bibliothèques :** `WiFi`, `HTTPClient`, `WiFiClientSecure` (si HTTPS).
* **Format :** JSON (Application/json).

### Exemple de Code (C++/Arduino)

```cpp
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>

// Configuration Réseau
const char* ssid = "VOTRE_SSID";
const char* password = "VOTRE_PASSWORD";

// URL du Backend (Adapter l'IP et le Port)
// Note : Le chemin inclut le nom du WAR définit dans le pom.xml
const char* serverUrl = "[https://192.168.](https://192.168.)x.x:443/PEPs_back-0.1/modules";

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi Connecté !");
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    WiFiClientSecure *client = new WiFiClientSecure;
    
    // Si vous utilisez HTTPS avec un certificat auto-signé
    client->setInsecure(); 

    HTTPClient https;
    
    if (https.begin(*client, serverUrl)) {
      https.addHeader("Content-Type", "application/json");

      // Payload JSON correspondant au ModuleDTO
      String jsonPayload = "{"
        "\"name\": \"Capteur_ESP32_01\","
        "\"location\": \"Salle_A\","
        "\"status\": \"Active\","
        "\"ip\": \"192.168.1.123\","
        "\"config\": {"
          "\"volume\": 75,"
          "\"mode\": \"Automatique\","
          "\"actif\": true,"
          "\"son\": true"
        "}"
      "}";

      int httpResponseCode = https.POST(jsonPayload);

      if (httpResponseCode > 0) {
        Serial.printf("Succès ! Code : %d\n", httpResponseCode);
        Serial.println(https.getString());
      } else {
        Serial.printf("Erreur : %s\n", https.errorToString(httpResponseCode).c_str());
      }
      https.end();
    }
    delete client;
  }
  delay(60000); // Envoi toutes les minutes
}

```
---

### Notes Importantes

- IP Locale : L'ESP32 ne reconnaît pas localhost. Utilisez l'adresse IP locale de la machine hôte.

- Contexte WAR : Si l'application est déployée via Tomcat, assurez-vous que le chemin /PEPs_back-0.1/ est correctement inclus dans l'URL.

- HTTPS : L'utilisation de client->setInsecure() est recommandée en développement pour ignorer la vérification stricte du certificat SSL.

---

### Points à vérifier lors de vos tests :
* **Logs Tomcat :** Si vous recevez un code **404**, c'est que l'URL (contexte du fichier .war) est incorrecte.
* **Logs Java :** Si vous recevez un code **400**, c'est que le JSON envoyé par l'ESP32 ne correspond pas exactement aux types de données attendus par `ModuleDTO`.
