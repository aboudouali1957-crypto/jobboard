import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API } from '../http-utils';
import { NotificationService } from '../notification.service';
import { AuthService } from './auth.service';

/**
 * Gère les erreurs transverses (session expirée, accès refusé, serveur indisponible).
 * Les erreurs métier (400, 409, 422) sont laissées aux composants qui les affichent dans leur contexte.
 */
export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const notify = inject(NotificationService);

  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && request.url.startsWith(API)) {
        const isLoginCall = request.url.startsWith(`${API}/auth/login`);
        if (error.status === 401 && !isLoginCall) {
          const returnUrl = router.url;
          auth.clearSession();
          void router.navigate(['/login'], { queryParams: { returnUrl } });
          notify.error('Votre session a expiré, veuillez vous reconnecter');
        } else if (error.status === 403 && !isLoginCall) {
          notify.error("Vous n'avez pas les droits pour effectuer cette action");
        } else if (error.status === 0) {
          notify.error('Serveur injoignable, vérifiez que l’API est démarrée');
        } else if (error.status >= 500) {
          notify.error('Erreur serveur, veuillez réessayer plus tard');
        }
      }
      return throwError(() => error);
    }),
  );
};
