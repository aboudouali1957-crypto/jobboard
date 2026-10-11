import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApplicationApi } from '../../core/api/application.api';
import { CandidateApi } from '../../core/api/candidate.api';
import { OfferApi } from '../../core/api/offer.api';
import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/http-utils';
import { OfferDetail } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { ContractLabelPipe, SalaryPipe } from '../../shared/pipes';

@Component({
  selector: 'app-offer-detail',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    DatePipe,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    SalaryPipe,
    ContractLabelPipe,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './offer-detail.html',
  styleUrl: './offer-detail.scss',
})
export class OfferDetailPage {
  /** Paramètre de route :id (withComponentInputBinding). */
  readonly id = input.required<string>();

  private readonly offerApi = inject(OfferApi);
  private readonly applicationApi = inject(ApplicationApi);
  private readonly candidateApi = inject(CandidateApi);
  private readonly notify = inject(NotificationService);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly offer = signal<OfferDetail | null>(null);
  protected readonly notFound = signal(false);
  protected readonly alreadyApplied = signal(false);
  protected readonly hasCv = signal(true);
  protected readonly submitting = signal(false);
  protected readonly applyError = signal<string | null>(null);

  protected readonly isCandidate = computed(() => this.auth.role() === 'CANDIDATE');

  protected readonly applyForm = inject(NonNullableFormBuilder).group({
    coverLetter: ['', [Validators.maxLength(5000)]],
  });

  constructor() {
    // Recharge l'offre à chaque changement de l'identifiant dans l'URL
    effect(() => {
      const id = Number(this.id());
      untracked(() => this.load(id));
    });
  }

  protected apply(): void {
    const offer = this.offer();
    if (!offer || this.applyForm.invalid) {
      this.applyForm.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.applyError.set(null);
    const coverLetter = this.applyForm.controls.coverLetter.value.trim() || null;
    this.applicationApi.apply(offer.id, coverLetter).subscribe({
      next: () => {
        this.submitting.set(false);
        this.alreadyApplied.set(true);
        this.notify.success('Candidature envoyée ! Un email de confirmation vous a été adressé.');
      },
      error: (error) => {
        this.submitting.set(false);
        this.applyError.set(errorMessage(error, "La candidature n'a pas pu être envoyée"));
      },
    });
  }

  protected loginToApply(): void {
    void this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
  }

  private load(id: number): void {
    this.offer.set(null);
    this.notFound.set(false);
    this.offerApi.get(id).subscribe({
      next: (offer) => this.offer.set(offer),
      error: () => this.notFound.set(true),
    });

    if (this.auth.role() === 'CANDIDATE') {
      forkJoin({ appliedIds: this.applicationApi.appliedOfferIds(), profile: this.candidateApi.profile() }).subscribe({
        next: ({ appliedIds, profile }) => {
          this.alreadyApplied.set(appliedIds.includes(id));
          this.hasCv.set(profile.cv !== null);
        },
      });
    }
  }
}
