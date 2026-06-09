import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Endpoints } from '../../../api/endpoints';
import { ListingSummary, PageResponse } from '../../listings/services/listings-api.service';

@Injectable({ providedIn: 'root' })
export class WishlistApiService {
  constructor(private http: HttpClient) {}

  getWishlist(page = 0, size = 20): Observable<PageResponse<ListingSummary>> {
    return this.http.get<PageResponse<ListingSummary>>(Endpoints.wishlist.base, { params: { page, size } });
  }

  add(listingId: number): Observable<void> {
    return this.http.post<void>(Endpoints.wishlist.add(listingId), {});
  }

  remove(listingId: number): Observable<void> {
    return this.http.delete<void>(Endpoints.wishlist.remove(listingId));
  }

  check(listingId: number): Observable<{ inWishlist: boolean }> {
    return this.http.get<{ inWishlist: boolean }>(Endpoints.wishlist.check(listingId));
  }
}
