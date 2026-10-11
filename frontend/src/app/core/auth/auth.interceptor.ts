import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { API } from '../http-utils';
import { AuthService } from './auth.service';

/** Ajoute le token JWT aux appels de l'API. */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthService).token();
  if (!token || !request.url.startsWith(API)) {
    return next(request);
  }
  return next(request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
