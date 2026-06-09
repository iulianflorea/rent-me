import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ListingsApiService, ListingSummary } from '../../../listings/services/listings-api.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { SkeletonLoaderComponent } from '../../../../shared/components/skeleton-loader/skeleton-loader.component';

@Component({
  selector: 'app-my-listings',
  imports: [RouterLink, TranslateModule, SkeletonLoaderComponent],
  templateUrl: './my-listings.component.html',
  styleUrl: './my-listings.component.scss',
})
export class MyListingsComponent implements OnInit {
  listings: ListingSummary[] = [];
  loading = true;

  constructor(
    private listingsApi: ListingsApiService,
    private notifications: NotificationService
  ) {}

  ngOnInit(): void {
    this.listingsApi.getMy().subscribe({
      next: (p) => { this.listings = p.content; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  deleteListing(id: number): void {
    if (!confirm('Ești sigur că vrei să ștergi acest anunț?')) return;
    this.listingsApi.delete(id).subscribe({
      next: () => {
        this.listings = this.listings.filter((l) => l.id !== id);
        this.notifications.success('Anunțul a fost șters.');
      },
    });
  }

  statusBadgeClass(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'badge-success',
      DRAFT: 'badge-neutral',
      RENTED: 'badge-warning',
      INACTIVE: 'badge-neutral',
    };
    return map[status] || 'badge-neutral';
  }
}
