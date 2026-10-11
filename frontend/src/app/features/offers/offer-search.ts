import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, ParamMap, Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';
import { OfferApi } from '../../core/api/offer.api';
import { ReferenceApi } from '../../core/api/reference.api';
import { CONTRACT_LABELS, CONTRACT_TYPES } from '../../core/labels';
import { ContractType, OfferFilters, OfferSummary, Page, Skill } from '../../core/models';
import { ContractLabelPipe, SalaryPipe, TimeAgoPipe } from '../../shared/pipes';

const DEFAULT_SIZE = 10;

const SORTS = [
  { value: 'createdAt,desc', label: 'Plus récentes' },
  { value: 'salaryMax,desc', label: 'Salaire le plus élevé' },
  { value: 'title,asc', label: 'Titre (A → Z)' },
];

/**
 * Recherche d'offres. Les filtres sont reflétés dans l'URL (?city=Lyon&contractType=CDI&page=1)
 * pour qu'une recherche puisse être partagée ou rechargée.
 */
@Component({
  selector: 'app-offer-search',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
    SalaryPipe,
    ContractLabelPipe,
    TimeAgoPipe,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './offer-search.html',
  styleUrl: './offer-search.scss',
})
export class OfferSearch implements OnInit {
  private readonly offerApi = inject(OfferApi);
  private readonly referenceApi = inject(ReferenceApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly contractTypes = CONTRACT_TYPES;
  protected readonly contractLabels = CONTRACT_LABELS;
  protected readonly sorts = SORTS;

  protected readonly result = signal<Page<OfferSummary> | null>(null);
  protected readonly loading = signal(false);
  protected readonly loadError = signal(false);
  protected readonly cities = signal<string[]>([]);
  protected readonly skills = signal<Skill[]>([]);

  protected readonly form = inject(NonNullableFormBuilder).group({
    keyword: [''],
    city: [''],
    contractType: ['' as ContractType | ''],
    minSalary: [null as number | null],
    skillId: [null as number | null],
    remote: [false],
    sort: ['createdAt,desc'],
  });

  ngOnInit(): void {
    this.offerApi.cities().subscribe({ next: (cities) => this.cities.set(cities), error: () => this.cities.set([]) });
    this.referenceApi.skills().subscribe({ next: (skills) => this.skills.set(skills), error: () => this.skills.set([]) });

    this.route.queryParamMap
      .pipe(
        tap((params) => this.form.patchValue(this.formValueFrom(params), { emitEvent: false })),
        tap(() => {
          this.loading.set(true);
          this.loadError.set(false);
        }),
        switchMap((params) =>
          this.offerApi
            .search(this.filtersFrom(params), {
              page: Number(params.get('page') ?? 0),
              size: Number(params.get('size') ?? DEFAULT_SIZE),
              sort: params.get('sort') ?? 'createdAt,desc',
            })
            .pipe(
              catchError(() => {
                this.loadError.set(true);
                return of(null);
              }),
            ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => {
        this.result.set(page);
        this.loading.set(false);
      });
  }

  protected search(): void {
    this.navigate({ page: null });
  }

  protected reset(): void {
    this.form.reset();
    this.navigate({ page: null });
  }

  protected changePage(event: PageEvent): void {
    this.navigate({ page: event.pageIndex || null, size: event.pageSize === DEFAULT_SIZE ? null : event.pageSize });
  }

  /** Reporte le formulaire dans l'URL ; le chargement est déclenché par l'abonnement à queryParamMap. */
  private navigate(extra: Record<string, number | null>): void {
    const value = this.form.getRawValue();
    const queryParams = {
      keyword: value.keyword.trim() || null,
      city: value.city || null,
      contractType: value.contractType || null,
      minSalary: value.minSalary || null,
      skillId: value.skillId || null,
      remote: value.remote || null,
      sort: value.sort === 'createdAt,desc' ? null : value.sort,
      size: this.route.snapshot.queryParamMap.get('size'),
      ...extra,
    };
    void this.router.navigate([], { queryParams });
  }

  private filtersFrom(params: ParamMap): OfferFilters {
    return {
      keyword: params.get('keyword'),
      city: params.get('city'),
      contractType: params.get('contractType') as ContractType | null,
      minSalary: params.has('minSalary') ? Number(params.get('minSalary')) : null,
      skillId: params.has('skillId') ? Number(params.get('skillId')) : null,
      remote: params.get('remote') === 'true' ? true : null,
    };
  }

  private formValueFrom(params: ParamMap) {
    return {
      keyword: params.get('keyword') ?? '',
      city: params.get('city') ?? '',
      contractType: (params.get('contractType') ?? '') as ContractType | '',
      minSalary: params.has('minSalary') ? Number(params.get('minSalary')) : null,
      skillId: params.has('skillId') ? Number(params.get('skillId')) : null,
      remote: params.get('remote') === 'true',
      sort: params.get('sort') ?? 'createdAt,desc',
    };
  }
}
