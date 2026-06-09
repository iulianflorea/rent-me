import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Endpoints } from '../../../api/endpoints';
import { PageResponse } from '../../listings/services/listings-api.service';

export interface AdminUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  kycStatus: string;
  active: boolean;
  gdprSigned: boolean;
  createdAt: string;
  kycSelfieUrl: string | null;
  kycIdFrontUrl: string | null;
  kycIdBackUrl: string | null;
}

export interface SmtpConfig {
  host: string;
  port: number;
  security: 'NONE' | 'STARTTLS' | 'SSL';
  username: string;
  displayName: string;
  active: boolean;
  updatedAt: string | null;
  updatedBy: string | null;
}

export interface SmtpStatus {
  status: 'CONFIGURED' | 'UNCONFIGURED' | 'ERROR';
  errorMessage: string | null;
}

@Injectable({ providedIn: 'root' })
export class AdminApiService {
  constructor(private http: HttpClient) {}

  getUsers(params: { page?: number; size?: number; email?: string; kycStatus?: string }): Observable<PageResponse<AdminUser>> {
    let p = new HttpParams();
    Object.entries(params).forEach(([k, v]) => { if (v != null) p = p.set(k, String(v)); });
    return this.http.get<PageResponse<AdminUser>>(Endpoints.admin.users, { params: p });
  }

  reviewKyc(userId: number, approved: boolean, rejectionReason?: string): Observable<void> {
    const status = approved ? 'VERIFIED' : 'REJECTED';
    return this.http.post<void>(Endpoints.admin.kycReview(userId), { status, rejectionReason });
  }

  suspendUser(id: number): Observable<void> {
    return this.http.post<void>(Endpoints.admin.suspendUser(id), {});
  }

  activateUser(id: number): Observable<void> {
    return this.http.post<void>(Endpoints.admin.activateUser(id), {});
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(Endpoints.admin.userById(id));
  }

  getSmtpConfig(): Observable<SmtpConfig> {
    return this.http.get<SmtpConfig>(Endpoints.admin.smtp);
  }

  saveSmtpConfig(config: SmtpConfig & { password?: string }): Observable<void> {
    return this.http.post<void>(Endpoints.admin.smtp, config);
  }

  testSmtp(): Observable<{ success: boolean; message: string }> {
    return this.http.post<{ success: boolean; message: string }>(Endpoints.admin.smtpTest, {});
  }

  getSmtpStatus(): Observable<SmtpStatus> {
    return this.http.get<SmtpStatus>(Endpoints.admin.smtpStatus);
  }
}
