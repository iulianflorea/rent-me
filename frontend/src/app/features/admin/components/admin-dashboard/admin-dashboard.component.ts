import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-admin-dashboard',
  imports: [RouterLink, TranslateModule],
  template: `
    <div class="page">
      <div class="container">
        <h1 style="margin-bottom: 32px;">{{ 'admin.title' | translate }}</h1>
        <div class="grid grid-3">
          <a routerLink="/admin/users" class="card admin-card">
            <div class="admin-icon">👥</div>
            <h3>{{ 'admin.users' | translate }}</h3>
            <p class="text-secondary text-sm">Gestionare utilizatori și KYC</p>
          </a>
          <a routerLink="/admin/smtp" class="card admin-card">
            <div class="admin-icon">📧</div>
            <h3>{{ 'admin.smtp' | translate }}</h3>
            <p class="text-secondary text-sm">Configurare server email</p>
          </a>
          <div class="card admin-card">
            <div class="admin-icon">📊</div>
            <h3>{{ 'admin.reports' | translate }}</h3>
            <p class="text-secondary text-sm">Rapoarte financiare platformă</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`.admin-card { padding: 24px; text-decoration: none; color: inherit; cursor: pointer; transition: transform 200ms ease; &:hover { transform: translateY(-2px); } } .admin-icon { font-size: 36px; margin-bottom: 12px; }`],
})
export class AdminDashboardComponent {}
