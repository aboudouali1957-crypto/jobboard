import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { filter, switchMap } from 'rxjs';
import { ApplicationApi } from '../../core/api/application.api';
import { errorMessage } from '../../core/http-utils';
import { APPLICATION_STATUSES, STATUS_LABELS } from '../../core/labels';
import { ApplicationStatus, CandidateApplication, Page } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { ConfirmDialog, ConfirmDialogData } from '../../shared/confirm-dialog';
import { ContractLabelPipe } from '../../shared/pipes';
import { StatusChip } from '../../shared/status-chip';

@Component({
  selector: 'app-my-applications',
  imports: [
    RouterLink,
    DatePipe,
    MatChipsModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTooltipModule,
    StatusChip,
    ContractLabelPipe,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './my-applications.html',
  styleUrl: './my-applications.scss',
})
export class MyApplications implements OnInit {
  private readonly api = inject(ApplicationApi);
  private readonly dialog = inject(MatDialog);
  private readonly notify = inject(NotificationService);

  protected readonly statuses = APPLICATION_STATUSES;
  protected readonly statusLabels = STATUS_LABELS;

  protected readonly status = signal<ApplicationStatus | null>(null);
  protected readonly page = signal<Page<CandidateApplication> | null>(null);
  protected readonly loading = signal(false);
  private pageIndex = 0;
  private pageSize = 10;

  ngOnInit(): void {
    this.load();
  }

  protected filterBy(status: ApplicationStatus | null): void {
    this.status.set(status);
    this.pageIndex = 0;
    this.load();
  }

  protected changePage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  /** Étape atteinte dans la frise « Reçue → Entretien → Décision ». */
  protected stepReached(application: CandidateApplication, step: 'RECEIVED' | 'INTERVIEW' | 'DECISION'): boolean {
    switch (step) {
      case 'RECEIVED':
        return true;
      case 'INTERVIEW':
        return application.status === 'INTERVIEW' || application.status === 'ACCEPTED';
      case 'DECISION':
        return application.status === 'ACCEPTED' || application.status === 'REJECTED';
    }
  }

  protected withdraw(application: CandidateApplication): void {
    const data: ConfirmDialogData = {
      title: 'Retirer la candidature ?',
      message: `Votre candidature « ${application.offerTitle} » sera supprimée. Vous pourrez postuler à nouveau.`,
      confirmLabel: 'Retirer',
      danger: true,
    };
    this.dialog
      .open(ConfirmDialog, { data })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed === true),
        switchMap(() => this.api.withdraw(application.id)),
      )
      .subscribe({
        next: () => {
          this.notify.success('Candidature retirée');
          this.load();
        },
        error: (error) => this.notify.error(errorMessage(error)),
      });
  }

  private load(): void {
    this.loading.set(true);
    this.api.mine(this.status(), { page: this.pageIndex, size: this.pageSize }).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
