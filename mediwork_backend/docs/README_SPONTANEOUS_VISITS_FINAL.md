# Endpoints de Visite Spontanée - Documentation Finale

## 🆕 Fonctionnalités Implémentées

### Choix de Date et Heure
Les collaborateurs peuvent maintenant spécifier leurs préférences de date et heure lors de la création d'une demande de visite spontanée.

**Note importante** : Les collaborateurs ne peuvent pas choisir le médecin. Le RH assignera un médecin lors de la planification.

## 📋 Données Requises pour Créer une Demande

### 🔴 Champs Obligatoires

1. **`reason`** (String)
   - **Type** : Texte obligatoire
   - **Description** : Le motif de la visite médicale
   - **Exemple** : "Consultation pour douleurs dorsales", "Suivi médical", "Contrôle de routine"
   - **Validation** : Ne peut pas être vide ou null

### 🟡 Champs Optionnels

2. **`additionalNotes`** (String)
   - **Type** : Texte optionnel
   - **Description** : Notes supplémentaires ou précisions sur la demande
   - **Exemple** : "Douleurs persistantes depuis 3 jours", "Préférence pour le matin", "Urgence relative"

3. **`preferredDateTime`** (LocalDateTime)
   - **Type** : Date et heure optionnelles
   - **Description** : Date et heure précises préférées pour la visite
   - **Validation** : Si fourni, doit être dans le futur
   - **Format** : ISO 8601 (ex: "2024-02-15T14:30:00")
   - **Exemple** : "2024-02-15T14:30:00" pour le 15 février 2024 à 14h30

## 🎯 Exemples de Requêtes

### Demande avec Date et Heure Précises
```json
{
  "reason": "Consultation pour douleurs dorsales",
  "additionalNotes": "Douleurs persistantes depuis 3 jours",
  "preferredDateTime": "2024-02-15T14:30:00"
}
```

### Demande sans Préférence de Date/Heure
```json
{
  "reason": "Consultation urgente",
  "additionalNotes": "Disponible à tout moment"
}
```

## 🔄 Statuts de Planification

### Statuts Disponibles
- **`PENDING`** : En attente de planification par le RH
- **`SCHEDULED`** : Visite planifiée dans un créneau
- **`NEEDS_RESCHEDULING`** : Nécessite une replanification (conflit avec les préférences)
- **`CANCELLED`** : Demande annulée

## 📊 Structure de la Réponse

```json
{
  "id": 456,
  "reason": "Consultation pour douleurs dorsales",
  "additionalNotes": "Douleurs persistantes depuis 3 jours",
  "visitType": "SPONTANEOUS",
  "status": "PENDING_DOCTOR_CONFIRMATION",
  "collaboratorName": "Jean Dupont",
  "doctorName": "Non assigné",
  "createdAt": "2024-01-20T10:30:00",
  "updatedAt": null,
  "slotInfo": "Non planifié",
  "preferredDateTime": "2024-02-15T14:30:00",
  "schedulingStatus": "PENDING"
}
```

## 🔧 Mise à Jour des Demandes

### Champs Modifiables
- **`reason`** : Motif de la visite
- **`additionalNotes`** : Notes supplémentaires
- **`preferredDateTime`** : Nouvelle date/heure préférée

### Contraintes
- Seulement si la demande est encore en attente (`PENDING_DOCTOR_CONFIRMATION`)
- La nouvelle date/heure doit être dans le futur
- Le statut de planification repasse à `PENDING`

## 🚀 Workflow Amélioré

1. **Collaborateur** crée une demande avec préférences de date/heure (sans choisir de médecin)
2. **Système** enregistre la demande avec statut `PENDING`
3. **RH** consulte les demandes avec leurs préférences
4. **RH** assigne un médecin et planifie en tenant compte des préférences
5. **Collaborateur** suit l'évolution et peut modifier ses préférences si nécessaire
6. **Statut** évolue selon le cycle de vie de la visite

## 💡 Avantages des Nouvelles Fonctionnalités

- **Flexibilité** : Les collaborateurs peuvent exprimer leurs préférences de date/heure
- **Efficacité** : Le RH peut planifier en tenant compte des préférences
- **Satisfaction** : Meilleure expérience utilisateur pour les collaborateurs
- **Optimisation** : Réduction des demandes de replanification
- **Transparence** : Suivi clair du statut de planification
- **Gestion RH** : Le RH garde le contrôle sur l'assignation des médecins

## 🔐 Sécurité et Validation

- Validation des dates futures
- Vérification des rôles et autorisations
- Logs automatiques de toutes les modifications
- Gestion des erreurs et validation des données
- Les collaborateurs ne peuvent pas choisir de médecin

## 🚫 Restrictions

- **Pas de choix de médecin** : Seul le RH peut assigner un médecin
- **Pas de créneau horaire flexible** : Seule la date/heure précise est supportée
- **Validation stricte** : Les dates passées sont rejetées

## 📝 Notes Techniques

- Les demandes sont créées sans médecin assigné
- Le champ `doctor_id` dans la table `visits` peut être null
- Le RH doit assigner un médecin lors de la planification
- Intégration complète avec le système de gestion des visites existant 