import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { filter, switchMap } from 'rxjs';
import { OfferApi } from '../../core/api/offer.api';
import { errorMessage } from '../../core/http-utils';
import { Page, RecruiterOffer } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { ConfirmDialog, ConfirmDialogData } from '../../shared/confirm-dialog';
import { ContractLabelPipe } from '../../shared/pipes';

@Component({
  selector: 'app-recruiter-offers',
  imports: [
    RouterLink,
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTooltipModule,
    ContractLabelPipe,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './recruiter-offers.html',
  styleUrl: './recruiter.scss',
})
export class RecruiterOffers implements OnInit {
  private readonly api = inject(OfferApi);
  private readonly dialog = inject(MatDialog);
  private readonly notify = inject(NotificationService);

  protected readonly columns = ['title', 'contract', 'status', 'applications', 'createdAt', 'actions'];
  protected readonly page = signal<Page<RecruiterOffer> | null>(null);
  protected readonly loading = signal(false);
  private pageIndex = 0;
  private pageSize = 10;

  ngOnInit(): void {
    this.load();
  }

  protected changePage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  protected toggleStatus(item: RecruiterOffer): void {
    const next = item.offer.status === 'OPEN' ? 'CLOSED' : 'OPEN';
    this.api.changeStatus(item.offer.id, next).subscribe({
      next: () => {
        this.notify.success(next === 'OPEN' ? 'Offre rouverte' : 'Offre fermée : elle n’apparaît plus dans la recherche');
        this.load();
      },
      error: (error) => this.notify.error(errorMessage(error)),
    });
  }

  protected remove(item: RecruiterOffer): void {
    const data: ConfirmDialogData = {
      title: 'Supprimer cette offre ?',
      message:
        item.totalApplications > 0
          ? `« ${item.offer.title} » et ses ${item.totalApplications} candidature(s) seront définitivement supprimées. Préférez la fermeture pour conserver l'historique.`
          : `« ${item.offer.title} » sera définitivement supprimée.`,
      confirmLabel: 'Supprimer',
      danger: true,
    };
    this.dialog
      .open(ConfirmDialog, { data })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed === true),
        switchMap(() => this.api.delete(item.offer.id)),
      )
      .subscribe({
        next: () => {
          this.notify.success('Offre supprimée');
          this.load();
        },
        error: (error) => this.notify.error(errorMessage(error)),
      });
  }

  private load(): void {
    this.loading.set(true);
    this.api.mine({ page: this.pageIndex, size: this.pageSize }).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
