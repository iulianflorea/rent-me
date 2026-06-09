import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Endpoints } from '../../../api/endpoints';
import { PageResponse } from '../../listings/services/listings-api.service';

export interface RentalResponse {
  id: number;
  referenceNumber: string;
  listing: { id: number; title: string; firstImageUrl: string; category: string };
  tenant: { id: number; firstName: string; lastName: string; kycStatus: string };
  owner: { id: number; firstName: string; lastName: string; kycStatus: string };
  startDate: string;
  endDate: string;
  totalDays: number;
  pricePerDay: number;
  subtotal: number;
  guaranteeAmount: number;
  totalAmount: number;
  status: string;
  qrCodeBase64: string | null;
  chatRoomId: number | null;
}

@Injectable({ providedIn: 'root' })
export class RentalsApiService {
  constructor(private http: HttpClient) {}

  create(data: { listingId: number; startDate: string; endDate: string }): Observable<RentalResponse> {
    return this.http.post<RentalResponse>(Endpoints.rentals.base, data);
  }

  getById(id: number): Observable<RentalResponse> {
    return this.http.get<RentalResponse>(Endpoints.rentals.byId(id));
  }

  getAsTenant(page = 0, size = 20): Observable<PageResponse<RentalResponse>> {
    return this.http.get<PageResponse<RentalResponse>>(Endpoints.rentals.asTenant, { params: { page, size } });
  }

  getAsOwner(page = 0, size = 20): Observable<PageResponse<RentalResponse>> {
    return this.http.get<PageResponse<RentalResponse>>(Endpoints.rentals.asOwner, { params: { page, size } });
  }

  markReadyToPickup(id: number): Observable<RentalResponse> {
    return this.http.post<RentalResponse>(Endpoints.rentals.readyToPickup(id), {});
  }

  confirmPickup(id: number): Observable<RentalResponse> {
    return this.http.post<RentalResponse>(Endpoints.rentals.confirmPickup(id), {});
  }

  returnByQr(qrToken: string): Observable<RentalResponse> {
    return this.http.post<RentalResponse>(Endpoints.rentals.return, { qrToken });
  }

  cancel(id: number): Observable<RentalResponse> {
    return this.http.post<RentalResponse>(Endpoints.rentals.cancel(id), {});
  }

  getQr(id: number): Observable<{ qrCodeBase64: string }> {
    return this.http.get<{ qrCodeBase64: string }>(Endpoints.rentals.qr(id));
  }
}
