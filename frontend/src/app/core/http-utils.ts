import { HttpErrorResponse, HttpParams, HttpResponse } from '@angular/common/http';
import { AbstractControl, FormGroup } from '@angular/forms';
import { PageRequest, ProblemDetail } from './models';

export const API = '/api';

/** Construit des HttpParams en ignorant les valeurs vides. */
export function toParams(values: object, page?: PageRequest): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(values)) {
    if (value !== null && value !== undefined && value !== '' && value !== false) {
      params = params.set(key, String(value));
    }
  }
  if (page) {
    params = params.set('page', page.page).set('size', page.size);
    if (page.sort) {
      params = params.set('sort', page.sort);
    }
  }
  return params;
}

/** Message lisible extrait d'une erreur HTTP (ProblemDetail du back-end). */
export function errorMessage(error: unknown, fallback = 'Une erreur est survenue'): string {
  if (error instanceof HttpErrorResponse) {
    const problem = error.error as ProblemDetail | null;
    if (problem?.detail) {
      return problem.detail;
    }
    if (error.status === 0) {
      return 'Serveur injoignable, vérifiez votre connexion';
    }
  }
  return fallback;
}

/**
 * Reporte les erreurs de validation renvoyées par l'API (champ → message) sur les contrôles du formulaire.
 * @returns true si au moins une erreur a pu être associée à un champ
 */
export function applyServerErrors(form: FormGroup, error: unknown, aliases: Record<string, string> = {}): boolean {
  if (!(error instanceof HttpErrorResponse) || error.status !== 400) {
    return false;
  }
  const errors = (error.error as ProblemDetail | null)?.errors ?? {};
  let applied = false;
  for (const [field, message] of Object.entries(errors)) {
    const control: AbstractControl | null = form.get(aliases[field] ?? field);
    if (control) {
      control.setErrors({ server: message });
      control.markAsTouched();
      applied = true;
    }
  }
  return applied;
}

/** Déclenche le téléchargement d'un fichier reçu en Blob, en reprenant le nom fourni par Content-Disposition. */
export function saveBlob(response: HttpResponse<Blob>, fallbackName: string): void {
  if (!response.body) {
    return;
  }
  const url = URL.createObjectURL(response.body);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileNameFrom(response.headers.get('Content-Disposition')) ?? fallbackName;
  link.click();
  setTimeout(() => URL.revokeObjectURL(url), 10_000);
}

export function fileNameFrom(contentDisposition: string | null): string | null {
  if (!contentDisposition) {
    return null;
  }
  const encoded = /filename\*=UTF-8''([^;]+)/i.exec(contentDisposition);
  if (encoded) {
    return decodeURIComponent(encoded[1]);
  }
  const plain = /filename="?([^";]+)"?/i.exec(contentDisposition);
  return plain ? plain[1] : null;
}
