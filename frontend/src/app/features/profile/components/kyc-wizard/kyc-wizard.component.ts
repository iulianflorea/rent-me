import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ProfileApiService, KycStatus } from '../../services/profile-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-kyc-wizard',
  imports: [ReactiveFormsModule, RouterLink, TranslateModule],
  templateUrl: './kyc-wizard.component.html',
  styleUrl: './kyc-wizard.component.scss',
})
export class KycWizardComponent implements OnInit {
  step = 1;
  kycStatus: KycStatus | null = null;
  loading = false;
  uploading = false;

  selfiePreview: string | null = null;
  idFrontPreview: string | null = null;
  idBackPreview: string | null = null;

  dataForm: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private profileApi: ProfileApiService,
    public auth: AuthService,
    private router: Router,
    private notifications: NotificationService
  ) {
    this.dataForm = this.fb.group({
      idSeries: ['', Validators.required],
      idNumber: ['', Validators.required],
      cnp: ['', [Validators.required, Validators.minLength(13), Validators.maxLength(13)]],
      birthDate: ['', Validators.required],
      idExpiryDate: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    if (this.auth.isKycVerified) {
      this.step = 3;
      return;
    }
    this.profileApi.getKycStatus().subscribe({
      next: (s) => {
        this.kycStatus = s;
        if (s.status === 'PENDING' || s.status === 'VERIFIED') this.step = 3;
        else if (s.selfieUploaded) this.step = 2;
      },
    });
  }

  captureSelfie(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.selfiePreview = URL.createObjectURL(file);
    this.uploading = true;
    this.profileApi.uploadSelfie(file).subscribe({
      next: () => { this.uploading = false; },
      error: () => { this.uploading = false; },
    });
  }

  uploadIdFront(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.idFrontPreview = URL.createObjectURL(file);
    this.profileApi.uploadIdFront(file).subscribe();
  }

  uploadIdBack(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.idBackPreview = URL.createObjectURL(file);
    this.profileApi.uploadIdBack(file).subscribe();
  }

  goToStep2(): void {
    if (!this.selfiePreview) { this.notifications.error('kyc.step1.capture'); return; }
    this.step = 2;
  }

  submitData(): void {
    if (this.dataForm.invalid || this.loading) return;
    this.loading = true;
    this.profileApi.submitKycData(this.dataForm.value as {
      idSeries: string; idNumber: string; cnp: string; birthDate: string; idExpiryDate: string;
    }).subscribe({
      next: () => {
        this.auth.updateUser({ kycStatus: 'PENDING' });
        this.step = 3;
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }
}
