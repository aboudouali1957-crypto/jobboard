import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/http-utils';

/** Comptes créés par le jeu de données de démonstration du back-end. */
const DEMO_ACCOUNTS = [
  { label: 'Candidat', email: 'candidat@jobboard.dev', password: 'Password123!' },
  { label: 'Recruteur', email: 'recruteur@jobboard.dev', password: 'Password123!' },
  { label: 'Admin', email: 'admin@jobboard.dev', password: 'Admin123!' },
];

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './login.html',
  styleUrl: './auth.scss',
})
export class Login {
  /** Page demandée avant la redirection vers la connexion (?returnUrl=...). */
  readonly returnUrl = input<string>();

  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly demoAccounts = DEMO_ACCOUNTS;
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly hidePassword = signal(true);

  protected readonly form = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  protected fill(account: { email: string; password: string }): void {
    this.form.setValue({ email: account.email, password: account.password });
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: () => {
        const target = this.safeReturnUrl() ?? this.auth.homeUrl();
        void this.router.navigateByUrl(target);
      },
      error: (error) => {
        this.submitting.set(false);
        this.error.set(errorMessage(error, 'Connexion impossible'));
      },
    });
  }

  /** N'accepte que des chemins internes pour éviter les redirections ouvertes. */
  private safeReturnUrl(): string | null {
    const url = this.returnUrl();
    return url && url.startsWith('/') && !url.startsWith('//') && !url.startsWith('/login') ? url : null;
  }
}
