import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { CandidateApi } from '../../core/api/candidate.api';
import { ReferenceApi } from '../../core/api/reference.api';
import { AuthService } from '../../core/auth/auth.service';
import { applyServerErrors, errorMessage, saveBlob } from '../../core/http-utils';
import { CandidateProfile, Skill } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

export const MAX_CV_SIZE = 5 * 1024 * 1024;

/** Vérifie un fichier avant envoi ; renvoie un message d'erreur ou null s'il est acceptable. */
export function validateCvFile(file: File): string | null {
  const isPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');
  if (!isPdf) {
    return 'Le CV doit être un fichier PDF';
  }
  if (file.size > MAX_CV_SIZE) {
    return 'Le CV ne doit pas dépasser 5 Mo';
  }
  return null;
}

@Component({
  selector: 'app-candidate-profile',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './candidate-profile.html',
  styleUrl: './candidate-profile.scss',
})
export class CandidateProfilePage implements OnInit {
  private readonly api = inject(CandidateApi);
  private readonly referenceApi = inject(ReferenceApi);
  private readonly auth = inject(AuthService);
  private readonly notify = inject(NotificationService);

  protected readonly profile = signal<CandidateProfile | null>(null);
  protected readonly skills = signal<Skill[]>([]);
  protected readonly saving = signal(false);
  protected readonly uploading = signal(false);
  protected readonly cvError = signal<string | null>(null);

  protected readonly form = inject(NonNullableFormBuilder).group({
    firstName: ['', [Validators.required, Validators.maxLength(80)]],
    lastName: ['', [Validators.required, Validators.maxLength(80)]],
    phone: ['', [Validators.maxLength(30), Validators.pattern(/^$|^[+0-9 ().-]{6,30}$/)]],
    city: ['', [Validators.maxLength(100)]],
    headline: ['', [Validators.maxLength(150)]],
    bio: ['', [Validators.maxLength(3000)]],
    skillIds: [[] as number[], [Validators.maxLength(20)]],
  });

  ngOnInit(): void {
    this.referenceApi.skills().subscribe((skills) => this.skills.set(skills));
    this.api.profile().subscribe((profile) => this.applyProfile(profile));
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const value = this.form.getRawValue();
    this.api
      .updateProfile({
        firstName: value.firstName.trim(),
        lastName: value.lastName.trim(),
        phone: value.phone.trim() || null,
        city: value.city.trim() || null,
        headline: value.headline.trim() || null,
        bio: value.bio.trim() || null,
        skillIds: value.skillIds,
      })
      .subscribe({
        next: (profile) => {
          this.applyProfile(profile);
          this.auth.updateUser({ firstName: profile.firstName, lastName: profile.lastName });
          this.saving.set(false);
          this.notify.success('Profil enregistré');
        },
        error: (error) => {
          this.saving.set(false);
          if (!applyServerErrors(this.form, error)) {
            this.notify.error(errorMessage(error, "Le profil n'a pas pu être enregistré"));
          }
        },
      });
  }

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) {
      return;
    }
    const problem = validateCvFile(file);
    this.cvError.set(problem);
    if (problem) {
      return;
    }
    this.uploading.set(true);
    this.api.uploadCv(file).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.uploading.set(false);
        this.notify.success('CV enregistré');
      },
      error: (error) => {
        this.uploading.set(false);
        this.cvError.set(errorMessage(error, "Le CV n'a pas pu être envoyé"));
      },
    });
  }

  protected downloadCv(): void {
    this.api.downloadCv().subscribe({
      next: (response) => saveBlob(response, 'cv.pdf'),
      error: (error) => this.notify.error(errorMessage(error, 'Téléchargement impossible')),
    });
  }

  private applyProfile(profile: CandidateProfile): void {
    this.form.reset({
      firstName: profile.firstName,
      lastName: profile.lastName,
      phone: profile.phone ?? '',
      city: profile.city ?? '',
      headline: profile.headline ?? '',
      bio: profile.bio ?? '',
      skillIds: profile.skills.map((skill) => skill.id),
    });
    this.profile.set(profile);
  }
}
