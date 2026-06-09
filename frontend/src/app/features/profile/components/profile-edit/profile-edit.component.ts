import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ProfileApiService } from '../../services/profile-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-profile-edit',
  imports: [ReactiveFormsModule, RouterLink, TranslateModule],
  templateUrl: './profile-edit.component.html',
  styleUrl: './profile-edit.component.scss',
})
export class ProfileEditComponent implements OnInit {
  form: ReturnType<FormBuilder['group']>;
  loading = false;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private profileApi: ProfileApiService,
    private auth: AuthService,
    private router: Router,
    private notifications: NotificationService
  ) {
    this.form = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      phone: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.loading = true;
    this.profileApi.getMe().subscribe({
      next: (p) => {
        this.form.patchValue({ firstName: p.firstName, lastName: p.lastName, phone: p.phone });
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  save(): void {
    if (this.form.invalid || this.saving) return;
    this.saving = true;
    this.profileApi.update(this.form.value as { firstName: string; lastName: string; phone: string }).subscribe({
      next: (p) => {
        this.auth.updateUser({ firstName: p.firstName, lastName: p.lastName });
        this.notifications.success('profile.updateSuccess');
        this.router.navigate(['/profile']);
      },
      error: () => { this.saving = false; },
    });
  }
}
