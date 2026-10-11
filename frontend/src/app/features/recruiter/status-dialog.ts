import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { STATUS_LABELS, TRANSITION_LABELS } from '../../core/labels';
import { ApplicationStatus } from '../../core/models';

export interface StatusDialogData {
  candidateName: string;
  offerTitle: string;
  target: ApplicationStatus;
}

const PLACEHOLDERS: Partial<Record<ApplicationStatus, string>> = {
  INTERVIEW: 'Ex. : Seriez-vous disponible mardi 14 à 10h pour un entretien en visio ?',
  ACCEPTED: 'Ex. : Bienvenue ! Nous revenons vers vous pour la proposition d’embauche.',
  REJECTED: 'Ex. : Nous avons retenu un profil plus expérimenté sur ce poste.',
};

/** Confirme un changement de statut et permet d'ajouter un message transmis au candidat par email. */
@Component({
  selector: 'app-status-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>{{ actionLabel }}</h2>
    <form [formGroup]="form" (ngSubmit)="confirm()">
      <mat-dialog-content>
        <p>
          La candidature de <strong>{{ data.candidateName }}</strong> pour « {{ data.offerTitle }} » passera au statut
          <strong>{{ statusLabel }}</strong>. Le candidat sera prévenu par email.
        </p>
        <mat-form-field>
          <mat-label>Message au candidat (facultatif)</mat-label>
          <textarea matInput formControlName="message" rows="4" maxlength="1000" [placeholder]="placeholder"></textarea>
          <mat-hint align="end">{{ form.controls.message.value.length }} / 1000</mat-hint>
        </mat-form-field>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button matButton type="button" mat-dialog-close>Annuler</button>
        <button matButton="filled" type="submit" [class.danger-button]="data.target === 'REJECTED'">Confirmer</button>
      </mat-dialog-actions>
    </form>
  `,
  styles: `
    mat-form-field {
      margin-top: 8px;
    }
  `,
})
export class StatusDialog {
  protected readonly data = inject<StatusDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject<MatDialogRef<StatusDialog, string | null>>(MatDialogRef);

  protected readonly actionLabel = TRANSITION_LABELS[this.data.target];
  protected readonly statusLabel = STATUS_LABELS[this.data.target];
  protected readonly placeholder = PLACEHOLDERS[this.data.target] ?? '';

  protected readonly form = inject(NonNullableFormBuilder).group({
    message: ['', [Validators.maxLength(1000)]],
  });

  /** Ferme la boîte avec le message saisi ('' si aucun) ; `undefined` signifie une annulation. */
  protected confirm(): void {
    this.dialogRef.close(this.form.controls.message.value.trim());
  }
}
