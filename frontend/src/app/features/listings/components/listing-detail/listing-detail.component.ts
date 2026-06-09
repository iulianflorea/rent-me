import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DecimalPipe } from '@angular/common';
import { ListingsApiService, ListingDetail } from '../../services/listings-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { VerificationBadgeComponent } from '../../../../shared/components/verification-badge/verification-badge.component';
import { StarRatingComponent } from '../../../../shared/components/star-rating/star-rating.component';
import { SkeletonLoaderComponent } from '../../../../shared/components/skeleton-loader/skeleton-loader.component';
import { WishlistApiService } from '../../../dashboard/services/wishlist-api.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Endpoints } from '../../../../api/endpoints';

interface ReviewItem {
  id: number;
  reviewer: { firstName: string; lastName: string };
  rating: number;
  comment: string;
  createdAt: string;
}

@Component({
  selector: 'app-listing-detail',
  imports: [RouterLink, TranslateModule, DecimalPipe, VerificationBadgeComponent, StarRatingComponent, SkeletonLoaderComponent, ReactiveFormsModule],
  templateUrl: './listing-detail.component.html',
  styleUrl: './listing-detail.component.scss',
})
export class ListingDetailComponent implements OnInit {
  listing: ListingDetail | null = null;
  loading = true;
  reviews: ReviewItem[] = [];
  inWishlist = false;

  dateForm: ReturnType<FormBuilder['group']>;

  get today(): string {
    return new Date().toISOString().split('T')[0];
  }

  get days(): number {
    const { startDate, endDate } = this.dateForm.value;
    if (!startDate || !endDate) return 0;
    const diff = new Date(endDate).getTime() - new Date(startDate).getTime();
    return Math.max(0, Math.ceil(diff / 86400000));
  }

  get totalPrice(): number {
    return this.days * (this.listing?.pricePerDay || 0);
  }

  get guaranteeAmount(): number {
    if (!this.listing || this.listing.category === 'REAL_ESTATE' || this.days < 1) return 0;
    return this.totalPrice * 0.5;
  }

  selectedImage = 0;

  private readonly fieldOrder: Record<string, string[]> = {
    TOOLS:       ['tipScula', 'stare', 'brand', 'model', 'putere', 'alimentare', 'tensiuneBaterie', 'capacitateBaterie', 'includeAcesorii'],
    VEHICLES:    ['marca', 'model', 'anFabricatie', 'caroserie', 'combustibil', 'cilindree', 'putereMotor', 'rulaj', 'transmisie', 'tractiune', 'nrLocuri', 'capacitatePortbagaj', 'permisNecesar', 'asigurareInclusa', 'franciza'],
    REAL_ESTATE: ['tipProprietate', 'regim', 'suprafataUtila', 'suprafataTotal', 'nrCamere', 'nrBai', 'etaj', 'totalEtaje', 'nrMaxOaspeti', 'checkIn', 'checkOut', 'reguliCasa'],
    ELECTRONICS: ['tipDevice', 'brand', 'model', 'stare', 'specificatii', 'accesoriiIncluse'],
    SPORTS:      ['tipSport', 'stare', 'dimensiune', 'includeProtectie'],
    OTHER:       ['tipObiect', 'stare', 'dimensiuni', 'greutate', 'note'],
  };

  private readonly fieldUnits: Record<string, string> = {
    putere: 'W', cilindree: 'cm³', putereMotor: 'CP', rulaj: 'km',
    suprafataUtila: 'm²', suprafataTotal: 'm²', capacitateBaterie: 'Ah',
    capacitatePortbagaj: 'L', franciza: 'RON',
  };

  get attrs(): Record<string, unknown> {
    const raw = this.listing?.categoryAttributes;
    if (!raw) return {};
    if (typeof raw === 'string') {
      try { return JSON.parse(raw); } catch { return {}; }
    }
    return raw as Record<string, unknown>;
  }

  get specRows(): { key: string; value: string }[] {
    const a = this.attrs;
    const category = this.listing?.category || '';
    const skip = new Set(['dotari', 'facilitati', 'functiiPrincipale']);
    const order = this.fieldOrder[category]
      ?? Object.keys(a).filter(k => !skip.has(k));

    return order.reduce<{ key: string; value: string }[]>((rows, key) => {
      const val = a[key];
      if (val === null || val === undefined || val === '' || val === false) return rows;
      const unit = this.fieldUnits[key] ? ` ${this.fieldUnits[key]}` : '';
      const display = typeof val === 'boolean' ? 'Da' : `${val}${unit}`;
      rows.push({ key: `listing.fields.${key}`, value: display });
      return rows;
    }, []);
  }

  get dotariList(): string[] {
    const d = this.attrs['dotari'];
    if (!d || typeof d !== 'object' || Array.isArray(d)) return [];
    return Object.entries(d as Record<string, boolean>).filter(([, v]) => v).map(([k]) => k);
  }

  get facilitatiList(): string[] {
    const f = this.attrs['facilitati'];
    if (!f || typeof f !== 'object' || Array.isArray(f)) return [];
    return Object.entries(f as Record<string, boolean>).filter(([, v]) => v).map(([k]) => k);
  }

  get functiiPrincipale(): string[] {
    const f = this.attrs['functiiPrincipale'];
    return Array.isArray(f) ? (f as string[]) : [];
  }

  get hasSpecs(): boolean {
    return this.specRows.length > 0 || this.dotariList.length > 0
      || this.facilitatiList.length > 0 || this.functiiPrincipale.length > 0;
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private listingsApi: ListingsApiService,
    private wishlistApi: WishlistApiService,
    private http: HttpClient,
    public auth: AuthService,
    private notifications: NotificationService
  ) {
    this.dateForm = this.fb.group({
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.listingsApi.getById(id).subscribe({
      next: (l) => {
        this.listing = l;
        this.inWishlist = l.inWishlist;
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
    this.http.get<{ content: ReviewItem[] }>(Endpoints.reviews.byUserId(0), {
      params: { listingId: id, page: 0, size: 5 },
    }).subscribe({ next: (r) => (this.reviews = r.content), error: () => {} });
  }

  checkout(): void {
    if (!this.auth.isLoggedIn) {
      this.router.navigate(['/auth/login']);
      return;
    }
    if (!this.auth.isKycVerified) {
      this.notifications.warning('kyc.required');
      this.router.navigate(['/profile/kyc']);
      return;
    }
    if (this.dateForm.invalid || this.days < 1) return;
    const { startDate, endDate } = this.dateForm.value;
    this.router.navigate(['/listings', this.listing!.id, 'checkout'], {
      queryParams: { startDate, endDate },
    });
  }

  toggleWishlist(): void {
    if (!this.auth.isLoggedIn) return;
    if (this.inWishlist) {
      this.wishlistApi.remove(this.listing!.id).subscribe(() => {
        this.inWishlist = false;
        this.notifications.info('wishlist.removed');
      });
    } else {
      this.wishlistApi.add(this.listing!.id).subscribe(() => {
        this.inWishlist = true;
        this.notifications.success('wishlist.added');
      });
    }
  }
}
