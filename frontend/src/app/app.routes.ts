import { Routes } from '@angular/router';
import { guestGuard, roleGuard } from './core/auth/guards';

// Chaque page est chargée à la demande (lazy loading) pour alléger le bundle initial
export const routes: Routes = [
  {
    path: '',
    title: 'Offres d’emploi - JobBoard',
    loadComponent: () => import('./features/offers/offer-search').then((m) => m.OfferSearch),
  },
  {
    path: 'offers/:id',
    title: 'Offre - JobBoard',
    loadComponent: () => import('./features/offers/offer-detail').then((m) => m.OfferDetailPage),
  },
  {
    path: 'login',
    title: 'Connexion - JobBoard',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login').then((m) => m.Login),
  },
  {
    path: 'register',
    title: 'Inscription - JobBoard',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register').then((m) => m.Register),
  },
  {
    path: 'candidate',
    canActivate: [roleGuard],
    data: { roles: ['CANDIDATE'] },
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'applications' },
      {
        path: 'applications',
        title: 'Mes candidatures - JobBoard',
        loadComponent: () => import('./features/candidate/my-applications').then((m) => m.MyApplications),
      },
      {
        path: 'profile',
        title: 'Mon profil - JobBoard',
        loadComponent: () => import('./features/candidate/candidate-profile').then((m) => m.CandidateProfilePage),
      },
    ],
  },
  {
    path: 'recruiter',
    canActivate: [roleGuard],
    data: { roles: ['RECRUITER'] },
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'offers' },
      {
        path: 'offers',
        title: 'Mes offres - JobBoard',
        loadComponent: () => import('./features/recruiter/recruiter-offers').then((m) => m.RecruiterOffers),
      },
      {
        path: 'offers/new',
        title: 'Publier une offre - JobBoard',
        loadComponent: () => import('./features/recruiter/offer-form').then((m) => m.OfferForm),
      },
      {
        path: 'offers/:id/edit',
        title: 'Modifier une offre - JobBoard',
        loadComponent: () => import('./features/recruiter/offer-form').then((m) => m.OfferForm),
      },
      {
        path: 'applications',
        title: 'Candidatures reçues - JobBoard',
        loadComponent: () =>
          import('./features/recruiter/recruiter-applications').then((m) => m.RecruiterApplications),
      },
      {
        path: 'company',
        title: 'Mon entreprise - JobBoard',
        loadComponent: () => import('./features/recruiter/company-form').then((m) => m.CompanyForm),
      },
    ],
  },
  {
    path: 'admin',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    children: [
      {
        path: '',
        title: 'Tableau de bord - JobBoard',
        loadComponent: () => import('./features/admin/admin-dashboard').then((m) => m.AdminDashboard),
      },
      {
        path: 'users',
        title: 'Utilisateurs - JobBoard',
        loadComponent: () => import('./features/admin/admin-users').then((m) => m.AdminUsers),
      },
    ],
  },
  {
    path: '**',
    title: 'Page introuvable - JobBoard',
    loadComponent: () => import('./features/not-found').then((m) => m.NotFound),
  },
];
