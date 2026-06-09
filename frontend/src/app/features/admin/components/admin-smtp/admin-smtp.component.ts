import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AdminApiService, SmtpConfig, SmtpStatus } from '../../services/admin-api.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-admin-smtp',
  imports: [ReactiveFormsModule, RouterLink, TranslateModule, DatePipe],
  templateUrl: './admin-smtp.component.html',
  styleUrl: './admin-smtp.component.scss',
})
export class AdminSmtpComponent implements OnInit, OnDestroy {
  form: ReturnType<FormBuilder['group']>;

  status: SmtpStatus | null = null;
  currentConfig: SmtpConfig | null = null;
  showPassword = false;
  saving = false;
  testing = false;
  private statusSub: Subscription | null = null;

  constructor(
    private fb: FormBuilder,
    private adminApi: AdminApiService,
    private notifications: NotificationService
  ) {
    this.form = this.fb.group({
      host: ['', Validators.required],
      port: [587, [Validators.required, Validators.min(1), Validators.max(65535)]],
      security: ['STARTTLS' as 'NONE' | 'STARTTLS' | 'SSL', Validators.required],
      username: ['', [Validators.required, Validators.email]],
      password: [''],
      displayName: ['RentIt Platform', Validators.required],
    });
  }

  ngOnInit(): void {
    this.adminApi.getSmtpConfig().subscribe({
      next: (c) => {
        this.currentConfig = c;
        this.form.patchValue({
          host: c.host,
          port: c.port,
          security: c.security,
          username: c.username,
          displayName: c.displayName,
        });
      },
    });

    this.pollStatus();
    this.statusSub = interval(300_000).pipe(switchMap(() => this.adminApi.getSmtpStatus())).subscribe((s) => (this.status = s));
  }

  pollStatus(): void {
    this.adminApi.getSmtpStatus().subscribe({ next: (s) => (this.status = s), error: () => {} });
  }

  save(): void {
    if (this.form.invalid || this.saving) return;
    this.saving = true;
    const payload = { ...this.form.value } as SmtpConfig & { password?: string };
    if (!payload.password) delete payload.password;
    this.adminApi.saveSmtpConfig(payload).subscribe({
      next: () => {
        this.notifications.success('admin.smtpConfig.saveSuccess');
        this.saving = false;
        this.pollStatus();
      },
      error: () => { this.saving = false; },
    });
  }

  saveAndTest(): void {
    if (this.form.invalid || this.saving) return;
    this.saving = true;
    const payload = { ...this.form.value } as SmtpConfig & { password?: string };
    if (!payload.password) delete payload.password;
    this.adminApi.saveSmtpConfig(payload).subscribe({
      next: () => {
        this.saving = false;
        this.test();
      },
      error: () => { this.saving = false; },
    });
  }

  test(): void {
    if (this.testing) return;
    this.testing = true;
    this.adminApi.testSmtp().subscribe({
      next: (res) => {
        this.testing = false;
        if (res.success) this.notifications.success('admin.smtpConfig.testSuccess');
        else this.notifications.error('admin.smtpConfig.testError');
        this.pollStatus();
      },
      error: () => { this.testing = false; this.notifications.error('admin.smtpConfig.testError'); },
    });
  }

  statusClass(): string {
    const map: Record<string, string> = {
      CONFIGURED: 'badge-success',
      UNCONFIGURED: 'badge-warning',
      ERROR: 'badge-danger',
    };
    return map[this.status?.status || 'UNCONFIGURED'];
  }

  statusLabel(): string {
    const map: Record<string, string> = {
      CONFIGURED: 'admin.smtpConfig.status.configured',
      UNCONFIGURED: 'admin.smtpConfig.status.unconfigured',
      ERROR: 'admin.smtpConfig.status.error',
    };
    return map[this.status?.status || 'UNCONFIGURED'];
  }

  ngOnDestroy(): void {
    this.statusSub?.unsubscribe();
  }
}
