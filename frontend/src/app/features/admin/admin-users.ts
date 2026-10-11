import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleChange, MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { AdminApi } from '../../core/api/admin.api';
import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/http-utils';
import { ROLE_LABELS } from '../../core/labels';
import { AdminUser, Page, Role } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-admin-users',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.scss',
})
export class AdminUsers implements OnInit {
  private readonly api = inject(AdminApi);
  private readonly notify = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly auth = inject(AuthService);

  protected readonly columns = ['name', 'email', 'role', 'company', 'createdAt', 'enabled'];
  protected readonly roles = Object.keys(ROLE_LABELS) as Role[];
  protected readonly roleLabels = ROLE_LABELS;
  protected readonly currentUserId = computed(() => this.auth.user()?.id);

  protected readonly page = signal<Page<AdminUser> | null>(null);
  protected readonly loading = signal(false);
  private pageIndex = 0;
  private pageSize = 20;

  protected readonly filters = inject(NonNullableFormBuilder).group({
    role: [null as Role | null],
    search: [''],
  });

  ngOnInit(): void {
    this.filters.controls.role.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.reload());
    this.filters.controls.search.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.reload());
    this.load();
  }

  protected roleLabel(role: Role): string {
    return ROLE_LABELS[role];
  }

  protected changePage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  protected toggle(user: AdminUser, event: MatSlideToggleChange): void {
    this.api.setEnabled(user.id, event.checked).subscribe({
      next: (updated) => {
        this.notify.success(updated.enabled ? 'Compte réactivé' : 'Compte désactivé : la connexion est bloquée');
        const page = this.page();
        if (page) {
          this.page.set({ ...page, content: page.content.map((u) => (u.id === updated.id ? updated : u)) });
        }
      },
      error: (error) => {
        event.source.checked = !event.checked;
        this.notify.error(errorMessage(error));
      },
    });
  }

  private reload(): void {
    this.pageIndex = 0;
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    const { role, search } = this.filters.getRawValue();
    this.api.users({ role, search: search.trim() || null }, { page: this.pageIndex, size: this.pageSize }).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
