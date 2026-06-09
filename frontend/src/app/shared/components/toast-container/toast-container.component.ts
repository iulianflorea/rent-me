import { Component } from '@angular/core';
import { AsyncPipe, NgClass } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-toast-container',
  imports: [AsyncPipe, NgClass, TranslateModule],
  template: `
    <div class="toast-container">
      @for (toast of notifications.toasts | async; track toast.id) {
        <div
          class="toast"
          [ngClass]="'toast-' + toast.type"
          (click)="notifications.dismiss(toast.id)"
        >
          {{ toast.message | translate }}
        </div>
      }
    </div>
  `,
})
export class ToastContainerComponent {
  constructor(public notifications: NotificationService) {}
}
