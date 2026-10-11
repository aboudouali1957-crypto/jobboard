import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ReferenceApi } from '../../core/api/reference.api';
import { applyServerErrors, errorMessage } from '../../core/http-utils';
import { Company } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-company-form',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressBarModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <header class="page-header">
        <div>
          <h1>Mon entreprise</h1>
          <p>Ces informations sont affichées aux candidats sur chacune de vos offres.</p>
        </div>
      </header>

      @if (loaded()) {
        <form class="panel" [formGroup]="form" (ngSubmit)="save()" novalidate>
          <div class="form-grid">
            <mat-form-field>
              <mat-label>Nom de l'entreprise</mat-label>
              <input matInput formControlName="name" autocomplete="organization" />
              @if (form.controls.name.hasError('required')) {
                <mat-error>Le nom est obligatoire</mat-error>
              }
            </mat-form-field>
            <mat-form-field>
              <mat-label>Ville du siège</mat-label>
              <input matInput formControlName="city" />
            </mat-form-field>
            <mat-form-field class="full">
              <mat-label>Site web</mat-label>
              <input matInput type="url" formControlName="website" placeholder="https://www.exemple.fr" />
              @if (form.controls.website.hasError('pattern')) {
                <mat-error>L'adresse doit commencer par http:// ou https://</mat-error>
              } @else if (form.controls.website.hasError('server')) {
                <mat-error>{{ form.controls.website.getError('server') }}</mat-error>
              }
            </mat-form-field>
            <mat-form-field class="full">
              <mat-label>Présentation</mat-label>
              <textarea matInput formControlName="description" rows="6" maxlength="5000"></textarea>
              <mat-hint align="end">{{ form.controls.description.value.length }} / 5000</mat-hint>
            </mat-form-field>
          </div>
          <div class="form-actions">
            <button matButton="filled" type="submit" [disabled]="saving() || form.pristine">Enregistrer</button>
          </div>
        </form>
      } @else {
        <mat-progress-bar mode="indeterminate" aria-label="Chargement" />
      }
    </div>
  `,
})
export class CompanyForm implements OnInit {
  private readonly api = inject(ReferenceApi);
  private readonly notify = inject(NotificationService);

  protected readonly loaded = signal(false);
  protected readonly saving = signal(false);

  protected readonly form = inject(NonNullableFormBuilder).group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    city: ['', [Validators.maxLength(100)]],
    website: ['', [Validators.maxLength(255), Validators.pattern(/^$|^https?:\/\/.+/)]],
    description: ['', [Validators.maxLength(5000)]],
  });

  ngOnInit(): void {
    this.api.myCompany().subscribe((company) => this.apply(company));
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const value = this.form.getRawValue();
    this.api
      .updateMyCompany({
        name: value.name.trim(),
        city: value.city.trim() || null,
        website: value.website.trim() || null,
        description: value.description.trim() || null,
      })
      .subscribe({
        next: (company) => {
          this.apply(company);
          this.saving.set(false);
          this.notify.success('Entreprise mise à jour');
        },
        error: (error) => {
          this.saving.set(false);
          if (!applyServerErrors(this.form, error)) {
            this.notify.error(errorMessage(error));
          }
        },
      });
  }

  private apply(company: Company): void {
    this.form.reset({
      name: company.name,
      city: company.city ?? '',
      website: company.website ?? '',
      description: company.description ?? '',
    });
    this.loaded.set(true);
  }
}
