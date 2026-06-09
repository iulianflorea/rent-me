import { Component, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-verification-badge',
  imports: [TranslateModule],
  template: `
    <span class="badge" [class]="badgeClass" [title]="tooltipKey | translate">
      @if (status === 'VERIFIED') {
        <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"></polyline></svg>
      }
      @if (size === 'full') {
        {{ tooltipKey | translate }}
      }
    </span>
  `,
  styles: [`
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 3px 8px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
    }
    .verified { background: rgba(52,199,89,0.12); color: #34c759; }
    .pending { background: rgba(255,159,10,0.12); color: #ff9f0a; }
    .none, .rejected { background: var(--color-bg-tertiary); color: var(--color-text-tertiary); }
  `],
})
export class VerificationBadgeComponent {
  @Input() status: 'NONE' | 'PENDING' | 'VERIFIED' | 'REJECTED' = 'NONE';
  @Input() size: 'dot' | 'full' = 'full';

  get badgeClass(): string {
    return this.status === 'VERIFIED' ? 'verified' : this.status === 'PENDING' ? 'pending' : 'none';
  }

  get tooltipKey(): string {
    const map: Record<string, string> = {
      VERIFIED: 'kyc.badge.verified',
      PENDING: 'kyc.badge.pending',
      REJECTED: 'kyc.badge.rejected',
      NONE: 'kyc.badge.unverified',
    };
    return map[this.status];
  }
}
