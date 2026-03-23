# Documentation Intégration IoT (ESP32) -> Backend

Ce document récapitule les modifications apportées au backend pour permettre la communication avec les cartes ESP32 et fournit un guide d'implémentation pour le client.

## 🛠 Modifications Backend

### Sécurité (Spring Security)
Le fichier `applicationContext.xml` a été modifié pour créer une exception de sécurité. Comme l'ESP32 ne peut pas gérer facilement les sessions ou les jetons CSRF, le chemin suivant a été ouvert :

- **Path :** `/modules/**`
- **Action :** `security="none"` (Filtres de sécurité désactivés)
- **Impact :** Les requêtes POST vers cet endpoint ne nécessitent plus de jeton CSRF ni d'authentification.

---
## Simulation via Terminal (cURL)

Avant de flasher l'ESP32, il est recommandé de tester l'endpoint directement depuis votre ordinateur pour valider que le backend accepte les données sans erreur de sécurité.

Utilisez la commande suivante dans votre terminal (Bash, Zsh ou PowerShell) :

```bash
curl -X POST http://localhost:8080/PEPs_back/modules \
-H "Content-Type: application/json" \
-k \
-d '{ 
  "name": "Simulateur_Terminal",
  "location": "Bureau_Dev",
  "status": "Active",
  "ip": "127.0.0.1",
  "config": {
    "volume": 50,
    "mode": "Test",
    "actif": true,
    "son": false
  }
}'
```


# Command to simulate sending a complete Interaction (Bash/Zsh)
### Note: The $(date ...) variable automatically inserts the current UTC date and time.

```bash
curl -X POST http://localhost:8080/PEPs_back/interactions \
-H "Content-Type: application/json" \
-k \
-d "{
  \"idsound\": 1,
  \"idmodule\": 1,
  \"typeInteraction\": \"SENSOR_TRIGGER\",
  \"time_lancement\": \"$(date -u +'%Y-%m-%dT%H:%M:%SZ')\",
  \"owner_id\": 1
}"
```
### Explication des arguments :

* **-X POST** : Définit la méthode HTTP.
* **-H "Content-Type: application/json"** : Indique au serveur que vous envoyez du JSON.
* **-k (ou --insecure)** : Indispensable en HTTPS si votre certificat est auto-signé ou non reconnu localement.
* **-d** : Contient le corps (body) de la requête.
---
##  Guide d'implémentation ESP32

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
        "\"name\": \"Capteur_ESP32_01\",\n"
        "\"location\": \"Salle_A\",\n"
        "\"status\": \"Active\",\n"
        "\"ip\": \"192.168.1.123\",\n"
        "\"config\": {\n"
          "\"volume\": 75,\n"
          "\"mode\": \"Automatique\",\n"
          "\"actif\": true,\n"
          "\"son\": true
        "}
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

## Front-end Implementation (Angular)

The Angular frontend uses an `ApiService` to communicate with the backend. Here is how you would perform the same POST request to create a module from within the Angular application.

### Using the `createModule` method

The `ApiService` located at `front/pepsfront/src/app/services/api.ts` already provides a `createModule` method for this purpose.

**Example Usage:**

```typescript
import { ApiService } from './services/api'; // Adjust path if necessary
import { Module } from './models/interfaces';  // Adjust path if necessary
import { inject } from '@angular/core';

// This is an example of how you might use this in a component or another service.

// 1. Inject the ApiService
const apiService = inject(ApiService);

// 2. Define the module data from your curl command.
// Note on type mismatches:
// - 'status': "Active" in curl vs. 'Actif' | 'Inactif' in the interface.
// - 'config.mode': "Test" in curl vs. 'Manuel' | 'Automatique' in the interface.
// The code below casts the values to match the interface. You may need to adjust
// them if the backend expects different values than what the interface defines.

const newModule: Omit<Module, 'id'> = {
  name: "Simulateur_Terminal",
  location: "Bureau_Dev",
  status: "Active" as 'Actif', // Casting "Active" to 'Actif'
  ip: "127.0.0.1",
  config: {
    volume: 50,
    mode: "Test" as 'Manuel', // Casting "Test" to 'Manuel'
    actif: true,
    son: false
  }
};

// 3. Call the createModule function.
// The method can also take an 'overrideRole' to assign the module to a specific user role.
apiService.createModule(newModule).subscribe({
  next: (createdModule) => {
    console.log('Module created successfully:', createdModule);
    // Handle successful creation here
  },
  error: (error) => {
    console.error('Error creating module:', error);
    // Handle error here
  }
});
```

### Important Note on Backend URL

The `ApiService` in your Angular application uses the `apiUrl` from the environment files (`front/pepsfront/src/environments/`) to connect to the backend. This value has been set to `'http://localhost:8080/PEPs_back'`.

Note that the `curl` and ESP32 examples in this document use `https://localhost:8443/PEPs_back-0.1`. The backend might be available at different addresses depending on the environment (e.g., local development vs. IoT testing).

Please ensure the `apiUrl` in your `environment.ts` or `environment.development.ts` files points to the correct backend address for your current task.