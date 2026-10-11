import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models';
import { AuthService } from './auth.service';

/** Exige un utilisateur connecté, sinon redirige vers la connexion en mémorisant la page demandée. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  if (auth.token()) {
    return true;
  }
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/**
 * Exige l'un des rôles déclarés dans `data.roles` de la route.
 * Exemple : `{ path: 'admin', canActivate: [roleGuard], data: { roles: ['ADMIN'] } }`
 */
export const roleGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.token()) {
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }
  const roles = (route.data['roles'] as Role[] | undefined) ?? [];
  return auth.hasRole(...roles) ? true : router.parseUrl(auth.homeUrl());
};

/** Réservé aux visiteurs non connectés (pages de connexion et d'inscription). */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.token() ? inject(Router).parseUrl(auth.homeUrl()) : true;
};
