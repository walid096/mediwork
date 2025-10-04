# Endpoints de Visite Spontanée - Documentation

## Vue d'ensemble

Ce document décrit les nouveaux endpoints implémentés pour permettre aux collaborateurs de faire des demandes de visite spontanée et de suivre l'évolution de leurs visites.

## Fonctionnalités Principales

### 1. Consultation du Profil Collaborateur
- **Endpoint**: `GET /api/users/profile`
- **Rôle requis**: `COLLABORATOR`
- **Description**: Permet au collaborateur de consulter ses informations personnelles
- **Réponse**: Informations du profil (nom, prénom, email, matricule, etc.)

### 2. Historique des Visites
- **Endpoint**: `GET /api/visits/my-history`
- **Rôle requis**: `COLLABORATOR`
- **Description**: Permet au collaborateur de consulter l'historique complet de ses visites
- **Réponse**: Liste de toutes les visites du collaborateur avec leurs statuts

### 3. Demandes de Visite Spontanée

#### Créer une demande
- **Endpoint**: `POST /api/spontaneous-visits`
- **Rôle requis**: `COLLABORATOR`
- **Description**: Permet au collaborateur de créer une demande de visite spontanée
- **Corps de la requête**:
```json
{
  "reason": "Motif de la visite (obligatoire)",
  "doctorId": 123,
  "additionalNotes": "Notes supplémentaires (optionnel)"
}
```

#### Lister mes demandes
- **Endpoint**: `GET /api/spontaneous-visits/my-requests`
- **Rôle requis**: `COLLABORATOR`
- **Description**: Permet au collaborateur de lister toutes ses demandes de visite spontanée
- **Réponse**: Liste des demandes avec leurs statuts

#### Consulter une demande spécifique
- **Endpoint**: `GET /api/spontaneous-visits/{id}`
- **Rôle requis**: `COLLABORATOR`
- **Description**: Permet au collaborateur de consulter une demande spécifique
- **Paramètres**: `id` - ID de la demande

#### Modifier une demande
- **Endpoint**: `PUT /api/spontaneous-visits/{id}`
- **Rôle requis**: `COLLABORATOR`
- **Description**: Permet au collaborateur de modifier une demande (motif et notes)
- **Corps de la requête**:
```json
{
  "reason": "Nouveau motif de la visite",
  "additionalNotes": "Nouvelles notes"
}
```
- **Contraintes**: Seulement si la demande est encore en attente

#### Annuler une demande
- **Endpoint**: `DELETE /api/spontaneous-visits/{id}`
- **Rôle requis**: `COLLABORATOR`
- **Description**: Permet au collaborateur d'annuler une demande
- **Contraintes**: Seulement si la demande est encore en attente

### 4. Gestion RH/Admin
- **Endpoint**: `GET /api/spontaneous-visits`
- **Rôle requis**: `HR` ou `ADMIN`
- **Description**: Permet au RH de consulter toutes les demandes de visite spontanée
- **Réponse**: Liste de toutes les demandes pour planification

## Statuts des Visites

### Cycle de Vie des Demandes Spontanées

1. **PENDING_DOCTOR_CONFIRMATION** - Demande créée, en attente de validation RH
2. **SCHEDULED** - Visite planifiée dans un créneau
3. **IN_PROGRESS** - Visite en cours
4. **COMPLETED** - Visite terminée
5. **CANCELLED** - Demande annulée

## Modèles de Données

### SpontaneousVisitRequest
```java
{
  "reason": "String (obligatoire)",
  "doctorId": "Long (obligatoire)",
  "additionalNotes": "String (optionnel)"
}
```

### SpontaneousVisitResponse
```java
{
  "id": "Long",
  "reason": "String",
  "additionalNotes": "String",
  "visitType": "SPONTANEOUS",
  "status": "VisitStatus",
  "collaboratorName": "String",
  "doctorName": "String",
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime",
  "slotInfo": "String"
}
```

## Sécurité

- Tous les endpoints sont protégés par authentification JWT
- Les collaborateurs ne peuvent accéder qu'à leurs propres données
- Le RH peut consulter toutes les demandes pour planification
- Validation des rôles à chaque endpoint

## Workflow de Demande Spontanée

1. **Collaborateur** crée une demande avec motif et médecin souhaité
2. **Système** enregistre la demande avec statut "En attente"
3. **RH** consulte les demandes en attente
4. **RH** planifie la visite dans un créneau disponible
5. **Collaborateur** suit l'évolution via son tableau de bord
6. **Statut** évolue selon le cycle de vie de la visite

## Exemples d'Utilisation

### Créer une demande
```bash
curl -X POST /api/spontaneous-visits \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Consultation pour douleurs dorsales",
    "doctorId": 123,
    "additionalNotes": "Douleurs persistantes depuis 3 jours"
  }'
```

### Consulter mes demandes
```bash
curl -X GET /api/spontaneous-visits/my-requests \
  -H "Authorization: Bearer <token>"
```

### Consulter mon historique
```bash
curl -X GET /api/visits/my-history \
  -H "Authorization: Bearer <token>"
```

## Notes Techniques

- Les demandes spontanées sont stockées dans une table séparée `spontaneous_visit_details`
- Le champ `slot_id` de la table `visits` est maintenant nullable pour les demandes spontanées
- Intégration complète avec le système de gestion des visites existant
- Logs automatiques de toutes les actions
- Gestion des erreurs et validation des données 