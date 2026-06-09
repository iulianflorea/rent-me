import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-forgot-password',
  imports: [ReactiveFormsModule, RouterLink, TranslateModule],
  templateUrl: './forgot-password.component.html',
  styleUrl: '../login/login.component.scss',
})
export class ForgotPasswordComponent implements OnInit {
  isReset = false;
  token = '';
  sent = false;
  loading = false;

  forgotForm: ReturnType<FormBuilder['group']>;
  resetForm: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private route: ActivatedRoute,
    private notifications: NotificationService
  ) {
    this.forgotForm = this.fb.group({ email: ['', [Validators.required, Validators.email]] });
    this.resetForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    this.isReset = !!this.token;
  }

  submitForgot(): void {
    if (this.forgotForm.invalid || this.loading) return;
    this.loading = true;
    this.auth.forgotPassword(this.forgotForm.value.email!).subscribe({
      next: () => { this.sent = true; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  submitReset(): void {
    if (this.resetForm.invalid || this.loading) return;
    const { password, confirmPassword } = this.resetForm.value;
    if (password !== confirmPassword) {
      this.notifications.error('auth.register.passwordMismatch');
      return;
    }
    this.loading = true;
    this.auth.resetPassword(this.token, password!).subscribe({
      next: () => {
        this.notifications.success('auth.resetPassword.success');
        this.loading = false;
        this.sent = true;
      },
      error: () => { this.loading = false; },
    });
  }
}
