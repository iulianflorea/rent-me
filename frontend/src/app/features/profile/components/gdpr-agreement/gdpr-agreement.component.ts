import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ProfileApiService } from '../../services/profile-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-gdpr-agreement',
  imports: [FormsModule, TranslateModule],
  templateUrl: './gdpr-agreement.component.html',
  styleUrl: './gdpr-agreement.component.scss',
})
export class GdprAgreementComponent {
  agreed = false;
  loading = false;

  constructor(
    private profileApi: ProfileApiService,
    private auth: AuthService,
    private router: Router,
    private notifications: NotificationService
  ) {}

  sign(): void {
    if (!this.agreed || this.loading) return;
    this.loading = true;
    this.profileApi.signGdpr().subscribe({
      next: () => {
        this.auth.updateUser({ gdprSigned: true });
        this.notifications.success('gdpr.signed');
        this.router.navigate(['/']);
      },
      error: () => { this.loading = false; },
    });
  }
}
