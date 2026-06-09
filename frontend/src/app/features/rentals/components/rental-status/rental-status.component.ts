import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DecimalPipe } from '@angular/common';
import { RentalsApiService, RentalResponse } from '../../services/rentals-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-rental-status',
  imports: [RouterLink, TranslateModule, DecimalPipe],
  templateUrl: './rental-status.component.html',
  styleUrl: './rental-status.component.scss',
})
export class RentalStatusComponent implements OnInit {
  rental: RentalResponse | null = null;
  loading = true;
  scanning = false;
  showQr = false;
  qrImage = '';

  constructor(
    private route: ActivatedRoute,
    private rentalsApi: RentalsApiService,
    public auth: AuthService,
    private notifications: NotificationService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.rentalsApi.getById(id).subscribe({
      next: (r) => { this.rental = r; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  get isOwner(): boolean {
    return this.rental?.owner.id === this.auth.currentUser?.id;
  }

  get isTenant(): boolean {
    return this.rental?.tenant.id === this.auth.currentUser?.id;
  }

  markReady(): void {
    if (!this.rental) return;
    this.rentalsApi.markReadyToPickup(this.rental.id).subscribe({
      next: (r) => { this.rental = r; this.notifications.success('Marcat ca gata de ridicare!'); },
    });
  }

  loadQr(): void {
    if (!this.rental) return;
    this.rentalsApi.getQr(this.rental.id).subscribe({
      next: (res) => { this.qrImage = res.qrCodeBase64; this.showQr = true; },
    });
  }

  startScan(): void {
    this.scanning = true;
    import('jsqr').then(({ default: jsQR }) => {
      const video = document.createElement('video');
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d')!;

      navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } }).then((stream) => {
        video.srcObject = stream;
        video.play();

        const scan = (): void => {
          if (video.readyState === video.HAVE_ENOUGH_DATA) {
            canvas.width = video.videoWidth;
            canvas.height = video.videoHeight;
            ctx.drawImage(video, 0, 0);
            const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
            const code = jsQR(imageData.data, imageData.width, imageData.height);
            if (code) {
              stream.getTracks().forEach((t) => t.stop());
              this.scanning = false;
              this.rentalsApi.returnByQr(code.data).subscribe({
                next: (r) => { this.rental = r; this.notifications.success('rental.return.success'); },
                error: () => this.notifications.error('rental.return.error'),
              });
              return;
            }
          }
          if (this.scanning) requestAnimationFrame(scan);
        };
        requestAnimationFrame(scan);
      }).catch(() => { this.scanning = false; });
    });
  }

  statusBadgeClass(status: string): string {
    const map: Record<string, string> = {
      PAID: 'badge-info',
      READY_TO_PICKUP: 'badge-warning',
      ACTIVE: 'badge-info',
      RETURNED: 'badge-success',
      CANCELLED: 'badge-danger',
      DISPUTED: 'badge-danger',
    };
    return map[status] || 'badge-neutral';
  }
}
