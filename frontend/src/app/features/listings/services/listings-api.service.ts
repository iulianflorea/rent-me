import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Endpoints } from '../../../api/endpoints';

export interface ListingSummary {
  id: number;
  title: string;
  category: string;
  pricePerDay: number;
  city: string;
  county: string;
  latitude: number;
  longitude: number;
  firstImageUrl: string;
  owner: { id: number; firstName: string; lastName: string; kycStatus: string; averageRating: number; reviewCount: number };
  distanceKm: number | null;
  status: string;
}

export interface ListingDetail extends ListingSummary {
  description: string;
  pricePerWeek: number | null;
  pricePerMonth: number | null;
  guaranteeAmount: number;
  address: string;
  categoryAttributes: Record<string, unknown>;
  images: { id: number; url: string; displayOrder: number }[];
  inWishlist: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface SearchParams {
  search?: string;
  category?: string;
  city?: string;
  county?: string;
  minPrice?: number;
  maxPrice?: number;
  startDate?: string;
  endDate?: string;
  lat?: number;
  lon?: number;
  radiusKm?: number;
  verifiedOnly?: boolean;
  minRating?: number;
  sortBy?: string;
  page?: number;
  size?: number;
}

export interface PaymentSplitPreview {
  totalAmount: number;
  platformFee: number;
  estimatedStripeFee: number;
  ownerReceives: number;
  guaranteeAmount: number;
  days: number;
  pricePerDay: number;
}

@Injectable({ providedIn: 'root' })
export class ListingsApiService {
  constructor(private http: HttpClient) {}

  search(params: SearchParams): Observable<PageResponse<ListingSummary>> {
    let p = new HttpParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') {
        p = p.set(k, String(v));
      }
    });
    return this.http.get<PageResponse<ListingSummary>>(Endpoints.listings.search, { params: p });
  }

  getById(id: number): Observable<ListingDetail> {
    return this.http.get<ListingDetail>(Endpoints.listings.byId(id));
  }

  getMy(page = 0, size = 20): Observable<PageResponse<ListingSummary>> {
    return this.http.get<PageResponse<ListingSummary>>(Endpoints.listings.my, {
      params: { page, size },
    });
  }

  create(data: Record<string, unknown>): Observable<ListingDetail> {
    return this.http.post<ListingDetail>(Endpoints.listings.base, data);
  }

  update(id: number, data: Record<string, unknown>): Observable<ListingDetail> {
    return this.http.patch<ListingDetail>(Endpoints.listings.byId(id), data);
  }

  uploadImages(id: number, files: File[]): Observable<ListingDetail> {
    const form = new FormData();
    files.forEach((f) => form.append('files', f));
    return this.http.post<ListingDetail>(Endpoints.listings.images(id), form);
  }

  publish(id: number): Observable<ListingDetail> {
    return this.http.post<ListingDetail>(Endpoints.listings.publish(id), {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(Endpoints.listings.byId(id));
  }

  getNearby(lat: number, lon: number, radiusKm = 10, page = 0, size = 10): Observable<PageResponse<ListingSummary>> {
    return this.http.get<PageResponse<ListingSummary>>(Endpoints.listings.nearby, {
      params: { lat, lon, radiusKm, page, size },
    });
  }

  getPaymentPreview(pricePerDay: number, days: number, category: string): Observable<PaymentSplitPreview> {
    return this.http.get<PaymentSplitPreview>(Endpoints.listings.paymentPreview, {
      params: { pricePerDay, days, category },
    });
  }
}
