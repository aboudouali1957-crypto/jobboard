import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective, provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { AdminApi } from '../../core/api/admin.api';
import { APPLICATION_STATUSES, CONTRACT_LABELS, CONTRACT_TYPES, STATUS_LABELS } from '../../core/labels';
import { AdminStats, ApplicationStatus } from '../../core/models';

// Palette validée (contraste, daltonisme) ; les libellés d'axe portent l'identité des barres
const SERIES_COLOR = '#6250d6';
const STATUS_COLORS: Record<ApplicationStatus, string> = {
  RECEIVED: '#2a78d6',
  INTERVIEW: '#eda100',
  ACCEPTED: '#1baf7a',
  REJECTED: '#eb6834',
};
const GRID_COLOR = '#ebeae7';
const TICK_COLOR = '#52514e';

const monthFormat = new Intl.DateTimeFormat('fr-FR', { month: 'short', year: '2-digit' });

function monthLabel(isoMonth: string): string {
  const [year, month] = isoMonth.split('-').map(Number);
  return monthFormat.format(new Date(year, month - 1, 1));
}

const baseScales = {
  x: { grid: { display: false }, ticks: { color: TICK_COLOR } },
  y: { beginAtZero: true, grid: { color: GRID_COLOR }, border: { display: false }, ticks: { color: TICK_COLOR, precision: 0 } },
};

@Component({
  selector: 'app-admin-dashboard',
  imports: [RouterLink, DecimalPipe, BaseChartDirective, MatIconModule, MatButtonModule, MatProgressBarModule],
  // Chart.js n'est chargé qu'avec cette page (lazy loading)
  providers: [provideCharts(withDefaultRegisterables())],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
})
export class AdminDashboard implements OnInit {
  private readonly api = inject(AdminApi);

  protected readonly stats = signal<AdminStats | null>(null);
  protected readonly error = signal(false);
  protected readonly statuses = APPLICATION_STATUSES;
  protected readonly statusLabels = STATUS_LABELS;
  protected readonly statusColors = STATUS_COLORS;

  /** Taux d'acceptation parmi les candidatures ayant reçu une réponse définitive. */
  protected readonly acceptanceRate = computed(() => {
    const stats = this.stats();
    if (!stats) {
      return null;
    }
    const { ACCEPTED, REJECTED } = stats.applicationsByStatus;
    return ACCEPTED + REJECTED === 0 ? null : ACCEPTED / (ACCEPTED + REJECTED);
  });

  protected readonly maxCity = computed(() => Math.max(1, ...(this.stats()?.topCities.map((c) => c.count) ?? [])));

  protected readonly monthlyChart = computed<ChartConfiguration<'line'>['data']>(() => {
    const months = this.stats()?.applicationsPerMonth ?? [];
    return {
      labels: months.map((m) => monthLabel(m.month)),
      datasets: [
        {
          label: 'Candidatures',
          data: months.map((m) => m.count),
          borderColor: SERIES_COLOR,
          backgroundColor: 'rgba(98, 80, 214, 0.10)',
          fill: true,
          tension: 0.3,
          borderWidth: 2,
          pointRadius: 4,
          pointHoverRadius: 6,
          pointBackgroundColor: SERIES_COLOR,
          pointBorderColor: '#ffffff',
          pointBorderWidth: 2,
        },
      ],
    };
  });

  protected readonly statusChart = computed<ChartConfiguration<'bar'>['data']>(() => {
    const byStatus = this.stats()?.applicationsByStatus;
    return {
      labels: APPLICATION_STATUSES.map((s) => STATUS_LABELS[s]),
      datasets: [
        {
          label: 'Candidatures',
          data: APPLICATION_STATUSES.map((s) => byStatus?.[s] ?? 0),
          backgroundColor: APPLICATION_STATUSES.map((s) => STATUS_COLORS[s]),
          borderRadius: 4,
          borderSkipped: 'start',
          maxBarThickness: 24,
        },
      ],
    };
  });

  protected readonly contractChart = computed<ChartConfiguration<'bar'>['data']>(() => {
    const byContract = this.stats()?.offersByContractType;
    return {
      labels: CONTRACT_TYPES.map((t) => CONTRACT_LABELS[t]),
      datasets: [
        {
          label: 'Offres',
          data: CONTRACT_TYPES.map((t) => byContract?.[t] ?? 0),
          backgroundColor: SERIES_COLOR,
          borderRadius: 4,
          borderSkipped: 'start',
          maxBarThickness: 24,
        },
      ],
    };
  });

  protected readonly lineOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { mode: 'index', intersect: false },
    plugins: { legend: { display: false } },
    scales: baseScales,
  };

  protected readonly horizontalBarOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: { legend: { display: false } },
    scales: {
      x: { beginAtZero: true, grid: { color: GRID_COLOR }, border: { display: false }, ticks: { color: TICK_COLOR, precision: 0 } },
      y: { grid: { display: false }, ticks: { color: TICK_COLOR } },
    },
  };

  ngOnInit(): void {
    this.api.stats().subscribe({
      next: (stats) => this.stats.set(stats),
      error: () => this.error.set(true),
    });
  }
}
