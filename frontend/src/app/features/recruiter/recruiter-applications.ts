import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { filter, switchMap } from 'rxjs';
import { ApplicationApi } from '../../core/api/application.api';
import { OfferApi } from '../../core/api/offer.api';
import { errorMessage, saveBlob } from '../../core/http-utils';
import { APPLICATION_STATUSES, NEXT_STATUSES, STATUS_LABELS, TRANSITION_LABELS } from '../../core/labels';
import { ApplicationStatus, OfferSummary, Page, RecruiterApplication } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { TimeAgoPipe } from '../../shared/pipes';
import { StatusChip } from '../../shared/status-chip';
import { StatusDialog, StatusDialogData } from './status-dialog';

/**
 * Suivi des candidatures reçues, filtrable par offre et par statut.
 * Les filtres sont portés par l'URL (?offerId=3&status=INTERVIEW).
 */
@Component({
  selector: 'app-recruiter-applications',
  imports: [
    RouterLink,
    DatePipe,
    MatExpansionModule,
    MatChipsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
    StatusChip,
    TimeAgoPipe,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './recruiter-applications.html',
  styleUrl: './recruiter.scss',
})
export class RecruiterApplications implements OnInit {
  private readonly applicationApi = inject(ApplicationApi);
  private readonly offerApi = inject(OfferApi);
  private readonly dialog = inject(MatDialog);
  private readonly notify = inject(NotificationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly statuses = APPLICATION_STATUSES;
  protected readonly statusLabels = STATUS_LABELS;
  protected readonly transitionLabels = TRANSITION_LABELS;
  protected readonly nextStatuses = NEXT_STATUSES;

  protected readonly offers = signal<OfferSummary[]>([]);
  protected readonly offerId = signal<number | null>(null);
  protected readonly status = signal<ApplicationStatus | null>(null);
  protected readonly page = signal<Page<RecruiterApplication> | null>(null);
  protected readonly loading = signal(false);
  private pageIndex = 0;
  private pageSize = 20;

  ngOnInit(): void {
    this.offerApi.mine({ page: 0, size: 50, sort: 'createdAt,desc' }).subscribe((page) =>
      this.offers.set(page.content.map((item) => item.offer)),
    );

    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      this.offerId.set(params.has('offerId') ? Number(params.get('offerId')) : null);
      this.status.set((params.get('status') as ApplicationStatus | null) ?? null);
      this.pageIndex = 0;
      this.load();
    });
  }

  protected filterByOffer(value: number | 'all'): void {
    const offerId = value === 'all' ? null : value;
    void this.router.navigate([], { queryParams: { offerId }, queryParamsHandling: 'merge' });
  }

  protected filterByStatus(status: ApplicationStatus | null): void {
    void this.router.navigate([], { queryParams: { status }, queryParamsHandling: 'merge' });
  }

  protected changePage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  protected changeStatus(application: RecruiterApplication, target: ApplicationStatus): void {
    const data: StatusDialogData = {
      candidateName: `${application.candidate.firstName} ${application.candidate.lastName}`,
      offerTitle: application.offerTitle,
      target,
    };
    this.dialog
      .open<StatusDialog, StatusDialogData, string | null>(StatusDialog, { data, width: '520px' })
      .afterClosed()
      .pipe(
        filter((message): message is string => typeof message === 'string'),
        switchMap((message) => this.applicationApi.changeStatus(application.id, target, message || null)),
      )
      .subscribe({
        next: (updated) => {
          this.notify.success(`Statut « ${STATUS_LABELS[updated.status]} » enregistré, le candidat a été notifié par email`);
          // Si un filtre de statut est actif, la candidature sort de la liste : on recharge
          if (this.status()) {
            this.load();
          } else {
            this.replace(updated);
          }
        },
        error: (error) => this.notify.error(errorMessage(error, 'Le statut n’a pas pu être modifié')),
      });
  }

  protected downloadCv(application: RecruiterApplication): void {
    this.applicationApi.downloadCv(application.id).subscribe({
      next: (response) =>
        saveBlob(response, `CV-${application.candidate.firstName}-${application.candidate.lastName}.pdf`),
      error: (error) => this.notify.error(errorMessage(error, 'Le CV n’a pas pu être téléchargé')),
    });
  }

  private replace(updated: RecruiterApplication): void {
    const page = this.page();
    if (page) {
      this.page.set({ ...page, content: page.content.map((item) => (item.id === updated.id ? updated : item)) });
    }
  }

  private load(): void {
    this.loading.set(true);
    this.applicationApi
      .received({ offerId: this.offerId(), status: this.status() }, { page: this.pageIndex, size: this.pageSize })
      .subscribe({
        next: (page) => {
          this.page.set(page);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }
}
