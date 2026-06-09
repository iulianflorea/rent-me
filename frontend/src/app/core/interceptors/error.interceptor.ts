import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notifications = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 0) {
        notifications.error('errors.networkError');
      } else if (error.status === 403) {
        notifications.error('errors.unauthorized');
      } else if (error.status === 404) {
        // Let components handle 404 themselves
      } else if (error.status >= 500) {
        notifications.error('errors.serverError');
      } else if (error.status === 422 || error.status === 400) {
        const errorCode = error.error?.errorCode;
        if (errorCode) {
          notifications.error(`errors.${errorCode}`);
        }
      }
      return throwError(() => error);
    })
  );
};
