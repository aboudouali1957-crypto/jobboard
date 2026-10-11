import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule, MatIconRegistry } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth/auth.service';
import { ROLE_LABELS } from './core/labels';
import { Role } from './core/models';

interface NavLink {
  path: string;
  label: string;
  icon: string;
  exact?: boolean;
}

const NAV_BY_ROLE: Record<Role | 'GUEST', NavLink[]> = {
  GUEST: [{ path: '/', label: 'Offres', icon: 'search', exact: true }],
  CANDIDATE: [
    { path: '/', label: 'Offres', icon: 'search', exact: true },
    { path: '/candidate/applications', label: 'Mes candidatures', icon: 'assignment' },
    { path: '/candidate/profile', label: 'Mon profil', icon: 'person' },
  ],
  RECRUITER: [
    { path: '/recruiter/offers', label: 'Mes offres', icon: 'work' },
    { path: '/recruiter/applications', label: 'Candidatures', icon: 'groups' },
    { path: '/recruiter/company', label: 'Entreprise', icon: 'apartment' },
  ],
  ADMIN: [
    { path: '/admin', label: 'Tableau de bord', icon: 'monitoring', exact: true },
    { path: '/admin/users', label: 'Utilisateurs', icon: 'manage_accounts' },
    { path: '/', label: 'Offres', icon: 'search', exact: true },
  ],
};

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly auth = inject(AuthService);
  protected readonly links = computed(() => NAV_BY_ROLE[this.auth.role() ?? 'GUEST']);
  protected readonly roleLabel = computed(() => {
    const role = this.auth.role();
    return role ? ROLE_LABELS[role] : '';
  });
  protected readonly initials = computed(() => {
    const user = this.auth.user();
    return user ? `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase() : '';
  });
  protected readonly year = new Date().getFullYear();

  constructor() {
    // Les icônes utilisent la police « Material Symbols » chargée dans index.html
    inject(MatIconRegistry).setDefaultFontSetClass('material-symbols-outlined');
  }

  protected logout(): void {
    this.auth.logout();
  }
}
