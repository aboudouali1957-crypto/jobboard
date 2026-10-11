import { ApplicationStatus, ContractType, Role } from './models';

export const CONTRACT_LABELS: Record<ContractType, string> = {
  CDI: 'CDI',
  CDD: 'CDD',
  FREELANCE: 'Freelance',
  STAGE: 'Stage',
  ALTERNANCE: 'Alternance',
};

export const CONTRACT_TYPES = Object.keys(CONTRACT_LABELS) as ContractType[];

export const STATUS_LABELS: Record<ApplicationStatus, string> = {
  RECEIVED: 'Reçue',
  INTERVIEW: 'Entretien',
  ACCEPTED: 'Acceptée',
  REJECTED: 'Refusée',
};

export const APPLICATION_STATUSES = Object.keys(STATUS_LABELS) as ApplicationStatus[];

export const ROLE_LABELS: Record<Role, string> = {
  CANDIDATE: 'Candidat',
  RECRUITER: 'Recruteur',
  ADMIN: 'Administrateur',
};

/** Transitions autorisées : miroir de ApplicationStatus.canTransitionTo côté back-end. */
export const NEXT_STATUSES: Record<ApplicationStatus, ApplicationStatus[]> = {
  RECEIVED: ['INTERVIEW', 'REJECTED'],
  INTERVIEW: ['ACCEPTED', 'REJECTED'],
  ACCEPTED: [],
  REJECTED: [],
};

/** Libellé du bouton qui déclenche la transition vers ce statut. */
export const TRANSITION_LABELS: Record<ApplicationStatus, string> = {
  RECEIVED: 'Remettre en attente',
  INTERVIEW: 'Proposer un entretien',
  ACCEPTED: 'Accepter',
  REJECTED: 'Refuser',
};
