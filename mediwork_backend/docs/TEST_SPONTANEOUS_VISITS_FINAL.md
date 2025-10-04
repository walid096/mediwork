# Tests des Fonctionnalités Finales de Visite Spontanée

## 🧪 Tests à Effectuer

### 1. Création de Demande avec Date et Heure Précises

```bash
curl -X POST /api/spontaneous-visits \
  -H "Authorization: Bearer <token_collaborator>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Test - Consultation avec date précise",
    "additionalNotes": "Test des nouvelles fonctionnalités",
    "preferredDateTime": "2024-02-15T14:30:00"
  }'
```

**Résultat attendu** : 
- Status 201 (Created)
- Demande créée avec `preferredDateTime`
- `doctorName` = "Non assigné"
- `schedulingStatus` = "PENDING"

### 2. Création de Demande sans Préférences

```bash
curl -X POST /api/spontaneous-visits \
  -H "Authorization: Bearer <token_collaborator>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Test - Consultation sans préférences",
    "additionalNotes": "Test des nouvelles fonctionnalités"
  }'
```

**Résultat attendu** :
- Status 201 (Created)
- Demande créée sans préférences de date/heure
- `preferredDateTime` = null
- `doctorName` = "Non assigné"
- `schedulingStatus` = "PENDING"

### 3. Validation des Dates Passées

```bash
curl -X POST /api/spontaneous-visits \
  -H "Authorization: Bearer <token_collaborator>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Test - Date passée",
    "preferredDateTime": "2023-01-01T10:00:00"
  }'
```

**Résultat attendu** :
- Status 400 (Bad Request)
- Message d'erreur : "La date et heure de la visite doivent être dans le futur"

### 4. Consultation des Demandes Créées

```bash
curl -X GET /api/spontaneous-visits/my-requests \
  -H "Authorization: Bearer <token_collaborator>"
```

**Résultat attendu** :
- Status 200 (OK)
- Liste des demandes avec tous les nouveaux champs
- Vérification que `preferredDateTime` et `schedulingStatus` sont présents
- Vérification que `doctorName` = "Non assigné"

### 5. Mise à Jour d'une Demande

```bash
curl -X PUT /api/spontaneous-visits/456 \
  -H "Authorization: Bearer <token_collaborator>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Motif mis à jour",
    "additionalNotes": "Notes mises à jour",
    "preferredDateTime": "2024-02-20T09:00:00"
  }'
```

**Résultat attendu** :
- Status 200 (OK)
- Demande mise à jour avec les nouveaux champs
- Vérification que tous les champs ont été modifiés

### 6. Consultation par le RH

```bash
curl -X GET /api/spontaneous-visits \
  -H "Authorization: Bearer <token_rh>"
```

**Résultat attendu** :
- Status 200 (OK)
- Liste de toutes les demandes avec les préférences de date/heure
- Permet au RH de planifier en tenant compte des préférences
- Le RH peut voir que les demandes n'ont pas de médecin assigné

## 🔍 Vérifications à Effectuer

### Base de Données
- Vérifier que la table `spontaneous_visit_details` contient les nouveaux champs
- Vérifier que le champ `preferred_time_slot` a été supprimé
- Vérifier que les données sont correctement enregistrées
- Vérifier les contraintes et index

### Validation
- Vérifier que les dates passées sont rejetées
- Vérifier que les enums sont correctement gérés
- Vérifier que les champs optionnels sont bien optionnels
- Vérifier que le champ `doctorId` n'est plus requis

### Sécurité
- Vérifier que seuls les collaborateurs peuvent créer des demandes
- Vérifier que les collaborateurs ne peuvent voir que leurs propres demandes
- Vérifier que le RH peut voir toutes les demandes
- Vérifier que les collaborateurs ne peuvent pas choisir de médecin

### Logique Métier
- Vérifier que les demandes sont créées sans médecin assigné
- Vérifier que le RH peut assigner un médecin lors de la planification
- Vérifier que le workflow fonctionne correctement

## 📝 Cas de Test Spécifiques

### Test de Création sans Médecin
- **Objectif** : Vérifier qu'une demande peut être créée sans médecin
- **Action** : Créer une demande sans spécifier de médecin
- **Résultat attendu** : Demande créée avec succès, `doctorName` = "Non assigné"

### Test de Validation des Dates
- **Objectif** : Vérifier que les dates passées sont rejetées
- **Action** : Créer une demande avec une date dans le passé
- **Résultat attendu** : Erreur 400 avec message approprié

### Test de Mise à Jour
- **Objectif** : Vérifier que les préférences peuvent être modifiées
- **Action** : Modifier une demande existante
- **Résultat attendu** : Demande mise à jour avec succès

## 🚫 Tests à NE PAS Effectuer

- **Choix de médecin** : Les collaborateurs ne peuvent pas choisir de médecin
- **Créneau horaire flexible** : Seule la date/heure précise est supportée
- **Assignation de médecin** : Seul le RH peut assigner un médecin

## 📊 Métriques de Test

- **Taux de succès** : 100% pour les demandes valides
- **Taux d'erreur** : 100% pour les dates passées
- **Performance** : Temps de réponse < 500ms
- **Sécurité** : 0 accès non autorisé

## 🔧 Configuration de Test

### Environnement
- Base de données de test avec schéma mis à jour
- Utilisateurs de test avec rôles appropriés
- Tokens JWT valides pour chaque rôle

### Données de Test
- Médecins existants dans la base
- Collaborateurs avec différents profils
- Dates de test variées (passé, présent, futur)

## 📋 Checklist de Validation

- [ ] Création de demande sans médecin
- [ ] Création de demande avec préférences de date
- [ ] Validation des dates passées
- [ ] Mise à jour des demandes
- [ ] Consultation par le RH
- [ ] Gestion des erreurs
- [ ] Sécurité et autorisations
- [ ] Performance des requêtes
- [ ] Intégrité des données
- [ ] Logs et audit 