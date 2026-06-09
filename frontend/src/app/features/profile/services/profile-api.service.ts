import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Endpoints } from '../../../api/endpoints';

export interface UserProfileFull {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  role: string;
  kycStatus: string;
  gdprSigned: boolean;
  preferredLanguage: string;
  preferredTheme: string;
  averageRating: number;
  reviewCount: number;
  activeListingsCount: number;
  totalRentalsCount: number;
  createdAt: string;
}

export interface KycStatus {
  status: string;
  selfieUploaded: boolean;
  idFrontUploaded: boolean;
  idBackUploaded: boolean;
  dataSubmitted: boolean;
  rejectionReason: string | null;
}

@Injectable({ providedIn: 'root' })
export class ProfileApiService {
  constructor(private http: HttpClient) {}

  getMe(): Observable<UserProfileFull> {
    return this.http.get<UserProfileFull>(Endpoints.users.me);
  }

  getById(id: number): Observable<UserProfileFull> {
    return this.http.get<UserProfileFull>(Endpoints.users.byId(id));
  }

  update(data: Partial<{ firstName: string; lastName: string; phone: string }>): Observable<UserProfileFull> {
    return this.http.patch<UserProfileFull>(Endpoints.users.me, data);
  }

  updatePreferences(data: { preferredLanguage?: string; preferredTheme?: string }): Observable<void> {
    return this.http.patch<void>(Endpoints.users.preferences, data);
  }

  deleteAccount(): Observable<void> {
    return this.http.delete<void>(Endpoints.users.me);
  }

  getKycStatus(): Observable<KycStatus> {
    return this.http.get<KycStatus>(Endpoints.users.kyc.status);
  }

  uploadSelfie(file: File): Observable<void> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<void>(Endpoints.users.kyc.selfie, form);
  }

  uploadIdFront(file: File): Observable<void> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<void>(Endpoints.users.kyc.idFront, form);
  }

  uploadIdBack(file: File): Observable<void> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<void>(Endpoints.users.kyc.idBack, form);
  }

  submitKycData(data: {
    idSeries: string;
    idNumber: string;
    cnp: string;
    birthDate: string;
    idExpiryDate: string;
  }): Observable<void> {
    return this.http.post<void>(Endpoints.users.kyc.data, data);
  }

  signGdpr(): Observable<void> {
    return this.http.post<void>(Endpoints.users.gdpr, { accepted: true });
  }
}
