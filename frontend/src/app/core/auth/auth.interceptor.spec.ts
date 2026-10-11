import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => controller.verify());

  function login(expiresAt: string): void {
    auth.login('candidat@jobboard.dev', 'Password123!').subscribe();
    controller.expectOne('/api/auth/login').flush({
      token: 'jwt-token',
      expiresAt,
      user: { id: 1, email: 'candidat@jobboard.dev', firstName: 'Camille', lastName: 'Martin', role: 'CANDIDATE', companyId: null },
    });
  }

  it("n'ajoute pas d'en-tête sans session", () => {
    http.get('/api/candidate/profile').subscribe();

    expect(controller.expectOne('/api/candidate/profile').request.headers.has('Authorization')).toBe(false);
  });

  it('ajoute le token Bearer aux appels de l’API', () => {
    login(new Date(Date.now() + 3_600_000).toISOString());

    http.get('/api/candidate/profile').subscribe();

    expect(controller.expectOne('/api/candidate/profile').request.headers.get('Authorization')).toBe('Bearer jwt-token');
    expect(auth.role()).toBe('CANDIDATE');
  });

  it("n'envoie jamais le token à un autre domaine", () => {
    login(new Date(Date.now() + 3_600_000).toISOString());

    http.get('https://example.com/data').subscribe();

    expect(controller.expectOne('https://example.com/data').request.headers.has('Authorization')).toBe(false);
  });

  it('ignore un token expiré et ferme la session', () => {
    login(new Date(Date.now() - 1_000).toISOString());

    http.get('/api/candidate/profile').subscribe();

    expect(controller.expectOne('/api/candidate/profile').request.headers.has('Authorization')).toBe(false);
    expect(auth.isAuthenticated()).toBe(false);
  });
});
