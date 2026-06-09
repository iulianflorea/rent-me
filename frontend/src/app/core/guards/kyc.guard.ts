import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const kycGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn) {
    return router.createUrlTree(['/auth/login']);
  }
  if (!auth.isKycVerified) {
    return router.createUrlTree(['/profile/kyc']);
  }
  return true;
};
