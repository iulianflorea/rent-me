import { Component, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { AdminApiService, AdminUser } from '../../services/admin-api.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { VerificationBadgeComponent } from '../../../../shared/components/verification-badge/verification-badge.component';
import { debounceTime } from 'rxjs';

@Component({
  selector: 'app-admin-users',
  imports: [ReactiveFormsModule, TranslateModule, DatePipe, VerificationBadgeComponent],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.scss',
})
export class AdminUsersComponent implements OnInit {
  users: AdminUser[] = [];
  loading = true;
  totalElements = 0;
  page = 0;
  pageSize = 20;
  searchControl = new FormControl('');
  selectedUser: AdminUser | null = null;

  constructor(
    private adminApi: AdminApiService,
    private notifications: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.searchControl.valueChanges.pipe(debounceTime(400)).subscribe(() => {
      this.page = 0;
      this.loadUsers();
    });
  }

  loadUsers(): void {
    this.loading = true;
    this.adminApi.getUsers({ page: this.page, size: this.pageSize, email: this.searchControl.value || undefined }).subscribe({
      next: (p) => { this.users = p.content; this.totalElements = p.totalElements; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  approveKyc(user: AdminUser): void {
    this.adminApi.reviewKyc(user.id, true).subscribe({
      next: () => { user.kycStatus = 'VERIFIED'; this.notifications.success('KYC aprobat!'); },
    });
  }

  rejectKyc(user: AdminUser): void {
    const reason = prompt('Motiv respingere:');
    if (!reason) return;
    this.adminApi.reviewKyc(user.id, false, reason).subscribe({
      next: () => { user.kycStatus = 'REJECTED'; this.notifications.success('KYC respins.'); },
    });
  }

  toggleSuspend(user: AdminUser): void {
    const obs = user.active ? this.adminApi.suspendUser(user.id) : this.adminApi.activateUser(user.id);
    obs.subscribe({ next: () => { user.active = !user.active; } });
  }

  get totalPages(): number { return Math.ceil(this.totalElements / this.pageSize); }
}
