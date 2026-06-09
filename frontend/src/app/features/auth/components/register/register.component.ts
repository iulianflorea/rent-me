import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, TranslateModule],
  templateUrl: './register.component.html',
  styleUrl: '../login/login.component.scss',
})
export class RegisterComponent {
  form: ReturnType<FormBuilder['group']>;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private notifications: NotificationService
  ) {
    this.form = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    });
  }

  submit(): void {
    if (this.form.invalid || this.loading) return;
    const { confirmPassword, ...data } = this.form.value;
    if (data.password !== confirmPassword) {
      this.notifications.error('auth.register.passwordMismatch');
      return;
    }
    this.loading = true;
    this.auth
      .register(data as { email: string; password: string; firstName: string; lastName: string; phone: string })
      .subscribe({
        next: () => this.router.navigate(['/profile/gdpr']),
        error: () => {
          this.loading = false;
        },
      });
  }
}
