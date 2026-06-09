import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { Endpoints } from '../../api/endpoints';
import { StorageService } from './storage.service';

export interface UserProfile {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  role: 'USER' | 'ADMIN';
  kycStatus: 'NONE' | 'PENDING' | 'VERIFIED' | 'REJECTED';
  gdprSigned: boolean;
  preferredLanguage: string;
  preferredTheme: 'LIGHT' | 'DARK' | 'SYSTEM';
  averageRating: number;
  reviewCount: number;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserProfile;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'rentit_token';
  private readonly REFRESH_KEY = 'rentit_refresh';

  private currentUser$ = new BehaviorSubject<UserProfile | null>(null);

  constructor(
    private http: HttpClient,
    private storage: StorageService,
    private router: Router
  ) {
    const stored = this.storage.get<UserProfile>('rentit_user');
    if (stored && this.getToken()) {
      this.currentUser$.next(stored);
    }
  }

  register(data: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phone: string;
  }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(Endpoints.auth.register, data).pipe(
      tap((res) => this.handleAuth(res))
    );
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(Endpoints.auth.login, { email, password })
      .pipe(tap((res) => this.handleAuth(res)));
  }

  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.storage.get<string>(this.REFRESH_KEY);
    return this.http
      .post<AuthResponse>(Endpoints.auth.refresh, { refreshToken })
      .pipe(tap((res) => this.handleAuth(res)));
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(Endpoints.auth.forgotPassword, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    return this.http.post<void>(Endpoints.auth.resetPassword, { token, newPassword });
  }

  logout(): void {
    this.storage.remove(this.TOKEN_KEY);
    this.storage.remove(this.REFRESH_KEY);
    this.storage.remove('rentit_user');
    this.currentUser$.next(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return this.storage.get<string>(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return this.storage.get<string>(this.REFRESH_KEY);
  }

  get currentUser(): UserProfile | null {
    return this.currentUser$.value;
  }

  currentUser$$ = this.currentUser$.asObservable();

  get isLoggedIn(): boolean {
    return !!this.currentUser$.value && !!this.getToken();
  }

  get isAdmin(): boolean {
    return this.currentUser$.value?.role === 'ADMIN';
  }

  get isKycVerified(): boolean {
    return this.currentUser$.value?.kycStatus === 'VERIFIED';
  }

  updateUser(user: Partial<UserProfile>): void {
    const current = this.currentUser$.value;
    if (current) {
      const updated = { ...current, ...user };
      this.currentUser$.next(updated);
      this.storage.set('rentit_user', updated);
    }
  }

  private handleAuth(res: AuthResponse): void {
    this.storage.set(this.TOKEN_KEY, res.accessToken);
    this.storage.set(this.REFRESH_KEY, res.refreshToken);
    this.storage.set('rentit_user', res.user);
    this.currentUser$.next(res.user);
  }
}
