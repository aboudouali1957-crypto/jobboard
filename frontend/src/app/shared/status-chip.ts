import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { STATUS_LABELS } from '../core/labels';
import { ApplicationStatus } from '../core/models';

const ICONS: Record<ApplicationStatus, string> = {
  RECEIVED: 'inbox',
  INTERVIEW: 'event',
  ACCEPTED: 'check_circle',
  REJECTED: 'cancel',
};

/** Pastille colorée représentant le statut d'une candidature. */
@Component({
  selector: 'app-status-chip',
  imports: [MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="status-chip" [class]="'status-' + status().toLowerCase()">
      <mat-icon aria-hidden="true">{{ icon() }}</mat-icon>
      {{ label() }}
    </span>
  `,
})
export class StatusChip {
  readonly status = input.required<ApplicationStatus>();
  protected readonly label = computed(() => STATUS_LABELS[this.status()]);
  protected readonly icon = computed(() => ICONS[this.status()]);
}
