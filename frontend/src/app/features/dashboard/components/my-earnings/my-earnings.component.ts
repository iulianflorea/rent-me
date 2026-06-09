import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DecimalPipe } from '@angular/common';
import { RentalsApiService, RentalResponse } from '../../../rentals/services/rentals-api.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { SkeletonLoaderComponent } from '../../../../shared/components/skeleton-loader/skeleton-loader.component';

@Component({
  selector: 'app-my-earnings',
  imports: [RouterLink, TranslateModule, DecimalPipe, SkeletonLoaderComponent],
  templateUrl: './my-earnings.component.html',
  styleUrl: '../my-rentals/my-rentals.component.scss',
})
export class MyEarningsComponent implements OnInit {
  rentals: RentalResponse[] = [];
  loading = true;

  constructor(
    private rentalsApi: RentalsApiService,
    private notifications: NotificationService
  ) {}

  ngOnInit(): void {
    this.rentalsApi.getAsOwner().subscribe({
      next: (p) => { this.rentals = p.content; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  markReady(rental: RentalResponse): void {
    this.rentalsApi.markReadyToPickup(rental.id).subscribe({
      next: (r) => {
        const idx = this.rentals.findIndex((x) => x.id === r.id);
        if (idx !== -1) this.rentals[idx] = r;
        this.notifications.success('Marcat ca gata de ridicare!');
      },
    });
  }

  statusBadgeClass(status: string): string {
    const map: Record<string, string> = {
      PAID: 'badge-info', READY_TO_PICKUP: 'badge-warning',
      ACTIVE: 'badge-info', RETURNED: 'badge-success',
      CANCELLED: 'badge-danger',
    };
    return map[status] || 'badge-neutral';
  }
}
