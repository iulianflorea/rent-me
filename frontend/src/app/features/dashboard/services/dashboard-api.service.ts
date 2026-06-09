import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Endpoints } from '../../../api/endpoints';

export interface DashboardOverview {
  totalListings: number;
  activeListings: number;
  totalRentalsAsOwner: number;
  totalRentalsAsTenant: number;
  pendingRentals: number;
  totalEarnings: number;
  totalSpending: number;
  unreadMessages: number;
  unreadNotifications: number;
  averageRating: number | null;
}

export interface FinancialReport {
  earnings: number;
  spending: number;
  netBalance: number;
  platformFeesCollected: number;
  stripeFees: number;
  netProfit: number;
  startDate: string;
  endDate: string;
}

export interface Notification {
  id: number;
  title: string;
  message: string;
  type: string;
  referenceId: number | null;
  read: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class DashboardApiService {
  constructor(private http: HttpClient) {}

  getOverview(): Observable<DashboardOverview> {
    return this.http.get<DashboardOverview>(Endpoints.dashboard.overview);
  }

  getReport(startDate: string, endDate: string): Observable<FinancialReport> {
    return this.http.post<FinancialReport>(Endpoints.dashboard.reports, { startDate, endDate });
  }

  getNotifications(page = 0, size = 20): Observable<{ content: Notification[]; totalElements: number }> {
    return this.http.get<{ content: Notification[]; totalElements: number }>(
      Endpoints.dashboard.notifications, { params: { page, size } }
    );
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(Endpoints.dashboard.notificationsUnread);
  }

  markAllRead(): Observable<void> {
    return this.http.post<void>(Endpoints.dashboard.markAllRead, {});
  }
}
