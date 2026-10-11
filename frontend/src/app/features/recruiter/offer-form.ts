import { ChangeDetectionStrategy, Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { OfferApi } from '../../core/api/offer.api';
import { ReferenceApi } from '../../core/api/reference.api';
import { applyServerErrors, errorMessage } from '../../core/http-utils';
import { CONTRACT_LABELS, CONTRACT_TYPES } from '../../core/labels';
import { ContractType, OfferDetail, Skill } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { salaryRangeValidator } from '../../shared/validators';

/** Création (/recruiter/offers/new) et modification (/recruiter/offers/:id/edit) d'une offre. */
@Component({
  selector: 'app-offer-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './offer-form.html',
  styleUrl: './recruiter.scss',
})
export class OfferForm implements OnInit {
  /** Présent uniquement en modification (paramètre de route :id). */
  readonly id = input<string>();

  private readonly offerApi = inject(OfferApi);
  private readonly referenceApi = inject(ReferenceApi);
  private readonly notify = inject(NotificationService);
  private readonly router = inject(Router);

  protected readonly contractTypes = CONTRACT_TYPES;
  protected readonly contractLabels = CONTRACT_LABELS;
  protected readonly isEdit = computed(() => !!this.id());
  protected readonly skills = signal<Skill[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = inject(NonNullableFormBuilder).group(
    {
      title: ['', [Validators.required, Validators.maxLength(150)]],
      description: ['', [Validators.required, Validators.minLength(30), Validators.maxLength(10000)]],
      city: ['', [Validators.required, Validators.maxLength(100)]],
      contractType: ['CDI' as ContractType, [Validators.required]],
      salaryMin: [null as number | null, [Validators.min(0), Validators.max(1_000_000)]],
      salaryMax: [null as number | null, [Validators.min(0), Validators.max(1_000_000)]],
      remote: [false],
      skillIds: [[] as number[], [Validators.maxLength(15)]],
    },
    { validators: salaryRangeValidator },
  );

  ngOnInit(): void {
    this.referenceApi.skills().subscribe((skills) => this.skills.set(skills));
    const id = this.id();
    if (id) {
      this.loading.set(true);
      this.offerApi.getMine(Number(id)).subscribe({
        next: (offer) => {
          this.patch(offer);
          this.loading.set(false);
        },
        error: (error) => {
          this.loading.set(false);
          this.notify.error(errorMessage(error, 'Offre introuvable'));
          void this.router.navigateByUrl('/recruiter/offers');
        },
      });
    }
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();
    const request = {
      ...value,
      title: value.title.trim(),
      description: value.description.trim(),
      city: value.city.trim(),
    };
    const id = this.id();
    const save$: Observable<OfferDetail> = id
      ? this.offerApi.update(Number(id), request)
      : this.offerApi.create(request);

    save$.subscribe({
      next: () => {
        this.notify.success(id ? 'Offre mise à jour' : 'Offre publiée !');
        void this.router.navigateByUrl('/recruiter/offers');
      },
      error: (error) => {
        this.saving.set(false);
        // L'erreur de cohérence des salaires est remontée par le back sous le nom « salaryRangeValid »
        if (!applyServerErrors(this.form, error, { salaryRangeValid: 'salaryMax' })) {
          this.error.set(errorMessage(error, "L'offre n'a pas pu être enregistrée"));
        }
      },
    });
  }

  private patch(offer: OfferDetail): void {
    this.form.reset({
      title: offer.title,
      description: offer.description,
      city: offer.city,
      contractType: offer.contractType,
      salaryMin: offer.salaryMin,
      salaryMax: offer.salaryMax,
      remote: offer.remote,
      skillIds: offer.skills.map((skill) => skill.id),
    });
  }
}
