# Tests des Nouvelles Fonctionnalités de Visite Spontanée

## 🧪 Tests à Effectuer

### 1. Création de Demande avec Date et Heure Précises

```bash
curl -X POST /api/spontaneous-visits \
  -H "Authorization: Bearer <token_collaborator>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Test - Consultation avec date précise",
    "doctorId": 123,
    "additionalNotes": "Test des nouvelles fonctionnalités",
    "preferredDateTime": "2024-02-15T14:30:00",
    "preferredTimeSlot": "AFTERNOON"
  }'
```

**Résultat attendu** : 
- Status 201 (Created)
- Demande créée avec `preferredDateTime` et `preferredTimeSlot`
- `schedulingStatus` = "PENDING"

### 2. Création de Demande avec Créneau Horaire Seulement

```bash
curl -X POST /api/spontaneous-visits \
  -H "Authorization: Bearer <token_collaborator>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Test - Consultation avec créneau horaire",
    "doctorId": 123,
    "additionalNotes": "Test des nouvelles fonctionnalités",
    "preferredTimeSlot": "MORNING"
  }'
```

**Résultat attendu** :
- Status 201 (Created)
- Demande créée avec `preferredTimeSlot` seulement
- `preferredDateTime` = null
- `schedulingStatus` = "PENDING"

### 3. Création de Demande sans Préférences

```bash
curl -X POST /api/spontaneous-visits \
  -H "Authorization: Bearer <token_collaborator>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Test - Consultation sans préférences",
    "doctorId": 123,
    "additionalNotes": "Test des nouvelles fonctionnalités"
  }'
```

**Résultat attendu** :
- Status 201 (Created)
- Demande créée sans préférences de date/heure
- `preferredDateTime` = null
- `preferredTimeSlot` = null
- `schedulingStatus` = "PENDING"

### 4. Validation des Dates Passées

```bash
curl -X POST /api/spontaneous-visits \
  -H "Authorization: Bearer <token_collaborator>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Test - Date passée",
    "doctorId": 123,
    "preferredDateTime": "2023-01-01T10:00:00"
  }'
```

**Résultat attendu** :
- Status 400 (Bad Request)
- Message d'erreur : "La date et heure de la visite doivent être dans le futur"

### 5. Consultation des Demandes Créées

```bash
curl -X GET /api/spontaneous-visits/my-requests \
  -H "Authorization: Bearer <token_collaborator>"
```

**Résultat attendu** :
- Status 200 (OK)
- Liste des demandes avec tous les nouveaux champs
- Vérification que `preferredDateTime`, `preferredTimeSlot` et `schedulingStatus` sont présents

### 6. Mise à Jour d'une Demande

```bash
curl -X PUT /api/spontaneous-visits/456 \
  -H "Authorization: Bearer <token_collaborator>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Motif mis à jour",
    "additionalNotes": "Notes mises à jour",
    "preferredDateTime": "2024-02-20T09:00:00",
    "preferredTimeSlot": "MORNING"
  }'
```

**Résultat attendu** :
- Status 200 (OK)
- Demande mise à jour avec les nouveaux champs
- Vérification que tous les champs ont été modifiés

### 7. Consultation par le RH

```bash
curl -X GET /api/spontaneous-visits \
  -H "Authorization: Bearer <token_rh>"
```

**Résultat attendu** :
- Status 200 (OK)
- Liste de toutes les demandes avec les préférences de date/heure
- Permet au RH de planifier en tenant compte des préférences

## 🔍 Vérifications à Effectuer

### Base de Données
- Vérifier que la table `spontaneous_visit_details` contient les nouveaux champs
- Vérifier que les données sont correctement enregistrées
- Vérifier les contraintes et index

### Validation
- Vérifier que les dates passées sont rejetées
- Vérifier que les enums sont correctement gérés
- Vérifier que les champs optionnels sont bien optionnels

### Sécurité
- Vérifier que seuls les collaborateurs peuvent créer des demandes
- Vérifier que les collaborateurs ne peuvent voir que leurs propres demandes
- Vérifier que le RH peut voir toutes les demandes

### Performance
- Vérifier que les requêtes avec les nouveaux champs sont performantes
- Vérifier que les index sont utilisés correctement

## 📝 Notes de Test

- Utiliser des tokens JWT valides pour chaque rôle
- Tester avec différents types de données (dates, créneaux, etc.)
- Vérifier les messages d'erreur et la validation
- Tester les cas limites (dates très éloignées, etc.)
- Vérifier la cohérence des données entre les entités 