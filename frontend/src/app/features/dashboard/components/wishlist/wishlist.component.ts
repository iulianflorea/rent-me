import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { WishlistApiService } from '../../services/wishlist-api.service';
import { ListingSummary } from '../../../listings/services/listings-api.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { ListingCardComponent } from '../../../listings/components/listing-card/listing-card.component';

@Component({
  selector: 'app-wishlist',
  imports: [RouterLink, TranslateModule, ListingCardComponent],
  template: `
    <div class="page">
      <div class="container">
        <h2 style="margin-bottom: 24px;">{{ 'wishlist.title' | translate }}</h2>
        @if (listings.length === 0 && !loading) {
          <div class="empty-state">
            <div class="empty-icon">♡</div>
            <h3>{{ 'wishlist.empty' | translate }}</h3>
            <p>{{ 'wishlist.emptySubtitle' | translate }}</p>
            <a routerLink="/" class="btn btn-primary" style="margin-top: 16px;">Explorează</a>
          </div>
        } @else {
          <div class="grid grid-3">
            @for (listing of listings; track listing.id) {
              <app-listing-card [listing]="listing" />
            }
          </div>
        }
      </div>
    </div>
  `,
})
export class WishlistComponent implements OnInit {
  listings: ListingSummary[] = [];
  loading = true;

  constructor(
    private wishlistApi: WishlistApiService,
    private notifications: NotificationService
  ) {}

  ngOnInit(): void {
    this.wishlistApi.getWishlist().subscribe({
      next: (p) => { this.listings = p.content; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }
}
