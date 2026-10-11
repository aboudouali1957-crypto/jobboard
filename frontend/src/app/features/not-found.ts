import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink, MatButtonModule, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <div class="empty-state panel">
        <mat-icon>explore_off</mat-icon>
        <h1>Page introuvable</h1>
        <p>Le lien est peut-être erroné ou la page a été déplacée.</p>
        <a matButton="filled" routerLink="/">Retour aux offres</a>
      </div>
    </div>
  `,
})
export class NotFound {}
