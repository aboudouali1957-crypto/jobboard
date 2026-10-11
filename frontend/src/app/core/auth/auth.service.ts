import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { map, Observable, tap } from 'rxjs';
import { API } from '../http-utils';
import { AuthResponse, RegisterCandidateRequest, RegisterRecruiterRequest, Role, User } from '../models';

const STORAGE_KEY = 'jobboard.session';

interface Session {
  token: string;
  expiresAt: string;
  user: User;
}

/** Page d'accueil de chaque rôle après connexion. */
export const HOME_BY_ROLE: Record<Role, string> = {
  CANDIDATE: '/',
  RECRUITER: '/recruiter/offers',
  ADMIN: '/admin',
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly session = signal<Session | null>(readSession());

  readonly user = computed(() => this.session()?.user ?? null);
  readonly role = computed(() => this.user()?.role ?? null);
  readonly isAuthenticated = computed(() => this.user() !== null);

  /** Token courant, ou null s'il est absent ou expiré. */
  token(): string | null {
    const session = this.session();
    if (!session) {
      return null;
    }
    if (new Date(session.expiresAt).getTime() <= Date.now()) {
      this.clearSession();
      return null;
    }
    return session.token;
  }

  hasRole(...roles: Role[]): boolean {
    const role = this.role();
    return role !== null && roles.includes(role);
  }

  homeUrl(): string {
    const role = this.role();
    return role ? HOME_BY_ROLE[role] : '/';
  }

  login(email: string, password: string): Observable<User> {
    return this.http
      .post<AuthResponse>(`${API}/auth/login`, { email, password })
      .pipe(tap((response) => this.store(response)), map((response) => response.user));
  }

  registerCandidate(request: RegisterCandidateRequest): Observable<User> {
    return this.http
      .post<AuthResponse>(`${API}/auth/register/candidate`, request)
      .pipe(tap((response) => this.store(response)), map((response) => response.user));
  }

  registerRecruiter(request: RegisterRecruiterRequest): Observable<User> {
    return this.http
      .post<AuthResponse>(`${API}/auth/register/recruiter`, request)
      .pipe(tap((response) => this.store(response)), map((response) => response.user));
  }

  /** Met à jour le nom affiché après une modification du profil. */
  updateUser(changes: Partial<Pick<User, 'firstName' | 'lastName'>>): void {
    const session = this.session();
    if (session) {
      this.persist({ ...session, user: { ...session.user, ...changes } });
    }
  }

  logout(redirectTo = '/'): void {
    this.clearSession();
    void this.router.navigateByUrl(redirectTo);
  }

  private store(response: AuthResponse): void {
    this.persist({ token: response.token, expiresAt: response.expiresAt, user: response.user });
  }

  private persist(session: Session): void {
    this.session.set(session);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    } catch {
      // Stockage indisponible (navigation privée) : la session reste en mémoire
    }
  }

  /** Supprime la session sans navigation (utilisé lorsque le token est rejeté par l'API). */
  clearSession(): void {
    this.session.set(null);
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      // ignoré
    }
  }
}

function readSession(): Session | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }
    const session = JSON.parse(raw) as Session;
    return new Date(session.expiresAt).getTime() > Date.now() ? session : null;
  } catch {
    return null;
  }
}
