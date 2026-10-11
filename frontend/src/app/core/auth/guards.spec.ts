import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Role } from '../models';
import { AuthService } from './auth.service';
import { guestGuard, roleGuard } from './guards';

describe('guards', () => {
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    router = TestBed.inject(Router);
  });

  function loginAs(role: Role): void {
    TestBed.inject(AuthService).login('user@jobboard.dev', 'Password123!').subscribe();
    TestBed.inject(HttpTestingController)
      .expectOne('/api/auth/login')
      .flush({
        token: 'jwt',
        expiresAt: new Date(Date.now() + 3_600_000).toISOString(),
        user: { id: 1, email: 'user@jobboard.dev', firstName: 'A', lastName: 'B', role, companyId: null },
      });
  }

  function runRoleGuard(roles: Role[], url = '/admin') {
    const route = { data: { roles } } as unknown as ActivatedRouteSnapshot;
    const state = { url } as RouterStateSnapshot;
    return TestBed.runInInjectionContext(() => roleGuard(route, state));
  }

  it('redirige un visiteur vers la connexion en mémorisant la page demandée', () => {
    const result = runRoleGuard(['ADMIN'], '/admin/users') as UrlTree;

    expect(router.serializeUrl(result)).toBe('/login?returnUrl=%2Fadmin%2Fusers');
  });

  it("laisse passer l'utilisateur qui a le bon rôle", () => {
    loginAs('ADMIN');

    expect(runRoleGuard(['ADMIN'])).toBe(true);
  });

  it("renvoie un recruteur vers son espace s'il tente d'accéder à l'administration", () => {
    loginAs('RECRUITER');

    const result = runRoleGuard(['ADMIN']) as UrlTree;

    expect(router.serializeUrl(result)).toBe('/recruiter/offers');
  });

  it('empêche un utilisateur connecté de revenir sur la page de connexion', () => {
    loginAs('CANDIDATE');

    const result = TestBed.runInInjectionContext(() =>
      guestGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    ) as UrlTree;

    expect(router.serializeUrl(result)).toBe('/');
  });
});
