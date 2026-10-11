import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { applyServerErrors, errorMessage } from '../../core/http-utils';
import { User } from '../../core/models';
import { PASSWORD_PATTERN, passwordsMatchValidator } from '../../shared/validators';

type AccountType = 'CANDIDATE' | 'RECRUITER';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './register.html',
  styleUrl: './auth.scss',
})
export class Register implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly accountType = signal<AccountType>('CANDIDATE');

  protected readonly form = inject(NonNullableFormBuilder).group(
    {
      type: ['CANDIDATE' as AccountType],
      firstName: ['', [Validators.required, Validators.maxLength(80)]],
      lastName: ['', [Validators.required, Validators.maxLength(80)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100), Validators.pattern(PASSWORD_PATTERN)]],
      confirmPassword: ['', [Validators.required]],
      companyName: [''],
      companyCity: [''],
    },
    { validators: passwordsMatchValidator },
  );

  ngOnInit(): void {
    // Les champs entreprise ne sont obligatoires que pour un compte recruteur
    this.form.controls.type.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((type) => {
      this.accountType.set(type);
      const companyName = this.form.controls.companyName;
      if (type === 'RECRUITER') {
        companyName.setValidators([Validators.required, Validators.maxLength(150)]);
      } else {
        companyName.clearValidators();
      }
      companyName.updateValueAndValidity();
    });
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const value = this.form.getRawValue();
    const base = {
      email: value.email.trim(),
      password: value.password,
      firstName: value.firstName.trim(),
      lastName: value.lastName.trim(),
    };
    const request$: Observable<User> =
      value.type === 'RECRUITER'
        ? this.auth.registerRecruiter({
            ...base,
            companyName: value.companyName.trim(),
            companyCity: value.companyCity.trim() || null,
          })
        : this.auth.registerCandidate(base);

    request$.subscribe({
      next: (user) => {
        void this.router.navigateByUrl(user.role === 'CANDIDATE' ? '/candidate/profile' : this.auth.homeUrl());
      },
      error: (error) => {
        this.submitting.set(false);
        if (!applyServerErrors(this.form, error)) {
          this.error.set(errorMessage(error, 'Inscription impossible'));
        }
      },
    });
  }
}
