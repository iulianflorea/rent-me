import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DecimalPipe } from '@angular/common';
import { ListingsApiService, ListingDetail } from '../../../listings/services/listings-api.service';
import { RentalsApiService } from '../../services/rentals-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { HttpClient } from '@angular/common/http';
import { Endpoints } from '../../../../api/endpoints';
import { loadStripe, Stripe, StripeElements } from '@stripe/stripe-js';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-rental-checkout',
  imports: [RouterLink, TranslateModule, DecimalPipe],
  templateUrl: './rental-checkout.component.html',
  styleUrl: './rental-checkout.component.scss',
})
export class RentalCheckoutComponent implements OnInit {
  listing: ListingDetail | null = null;
  startDate = '';
  endDate = '';
  loading = false;
  paying = false;
  rentalId: number | null = null;

  private stripe: Stripe | null = null;
  private elements: StripeElements | null = null;
  stripeReady = false;

  get days(): number {
    if (!this.startDate || !this.endDate) return 0;
    return Math.max(0, Math.ceil((new Date(this.endDate).getTime() - new Date(this.startDate).getTime()) / 86400000));
  }

  get rentalSubtotal(): number {
    return this.days * (this.listing?.pricePerDay || 0);
  }

  get guarantee(): number {
    if (!this.listing || this.listing.category === 'REAL_ESTATE') return 0;
    return this.rentalSubtotal * 0.5;
  }

  get total(): number {
    return this.rentalSubtotal + this.guarantee;
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private listingsApi: ListingsApiService,
    private rentalsApi: RentalsApiService,
    public auth: AuthService,
    private notifications: NotificationService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.startDate = this.route.snapshot.queryParamMap.get('startDate') || '';
    this.endDate = this.route.snapshot.queryParamMap.get('endDate') || '';

    if (!this.auth.isKycVerified) {
      this.notifications.warning('rental.checkout.kycRequired');
      this.router.navigate(['/profile/kyc']);
      return;
    }

    this.listingsApi.getById(id).subscribe((l) => (this.listing = l));
  }

  createRentalAndPay(): void {
    if (!this.listing || this.paying) return;
    this.paying = true;
    this.rentalsApi
      .create({ listingId: this.listing.id, startDate: this.startDate, endDate: this.endDate })
      .subscribe({
        next: (rental) => {
          this.rentalId = rental.id;
          this.http
            .get<{ clientSecret: string }>(Endpoints.payments.intent(rental.id))
            .subscribe({
              next: (pi) => this.mountStripe(pi.clientSecret),
              error: () => { this.paying = false; },
            });
        },
        error: () => { this.paying = false; },
      });
  }

  private async mountStripe(clientSecret: string): Promise<void> {
    this.stripe = await loadStripe(environment.stripePublicKey);
    if (!this.stripe) return;
    this.elements = this.stripe.elements({ clientSecret });
    const paymentElement = this.elements.create('payment');
    paymentElement.mount('#stripe-payment-element');
    this.stripeReady = true;
    this.paying = false;
  }

  async confirmPayment(): Promise<void> {
    if (!this.stripe || !this.elements || this.paying) return;
    this.paying = true;
    const { error } = await this.stripe.confirmPayment({
      elements: this.elements,
      confirmParams: { return_url: `${window.location.origin}/rentals/${this.rentalId}` },
    });
    if (error) {
      this.notifications.error(error.message || 'payment.failed');
      this.paying = false;
    }
  }
}
