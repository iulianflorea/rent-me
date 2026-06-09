import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DecimalPipe } from '@angular/common';
import { VerificationBadgeComponent } from '../../../../shared/components/verification-badge/verification-badge.component';
import { StarRatingComponent } from '../../../../shared/components/star-rating/star-rating.component';
import { ListingSummary } from '../../services/listings-api.service';

@Component({
  selector: 'app-listing-card',
  imports: [RouterLink, TranslateModule, DecimalPipe, VerificationBadgeComponent, StarRatingComponent],
  templateUrl: './listing-card.component.html',
  styleUrl: './listing-card.component.scss',
})
export class ListingCardComponent {
  @Input() listing!: ListingSummary;
}
