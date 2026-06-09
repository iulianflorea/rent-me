import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DecimalPipe } from '@angular/common';
import { RentalsApiService, RentalResponse } from '../../../rentals/services/rentals-api.service';
import { SkeletonLoaderComponent } from '../../../../shared/components/skeleton-loader/skeleton-loader.component';

@Component({
  selector: 'app-my-rentals',
  imports: [RouterLink, TranslateModule, DecimalPipe, SkeletonLoaderComponent],
  templateUrl: './my-rentals.component.html',
  styleUrl: './my-rentals.component.scss',
})
export class MyRentalsComponent implements OnInit {
  rentals: RentalResponse[] = [];
  loading = true;

  constructor(private rentalsApi: RentalsApiService) {}

  ngOnInit(): void {
    this.rentalsApi.getAsTenant().subscribe({
      next: (p) => { this.rentals = p.content; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  statusBadgeClass(status: string): string {
    const map: Record<string, string> = {
      PAID: 'badge-info', READY_TO_PICKUP: 'badge-warning',
      ACTIVE: 'badge-info', RETURNED: 'badge-success',
      CANCELLED: 'badge-danger', DISPUTED: 'badge-danger',
    };
    return map[status] || 'badge-neutral';
  }
}
