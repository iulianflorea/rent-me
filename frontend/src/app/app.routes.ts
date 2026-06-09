import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/listings/components/listing-list/listing-list.component').then(
        (m) => m.ListingListComponent
      ),
  },
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/components/login/login.component').then(
            (m) => m.LoginComponent
          ),
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./features/auth/components/register/register.component').then(
            (m) => m.RegisterComponent
          ),
      },
      {
        path: 'forgot-password',
        loadComponent: () =>
          import('./features/auth/components/forgot-password/forgot-password.component').then(
            (m) => m.ForgotPasswordComponent
          ),
      },
      {
        path: 'reset-password',
        loadComponent: () =>
          import('./features/auth/components/forgot-password/forgot-password.component').then(
            (m) => m.ForgotPasswordComponent
          ),
      },
    ],
  },
  {
    path: 'listings',
    children: [
      {
        path: 'create',
        loadComponent: () =>
          import('./features/listings/components/listing-create/listing-create.component').then(
            (m) => m.ListingCreateComponent
          ),
        canActivate: [authGuard],
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./features/listings/components/listing-detail/listing-detail.component').then(
            (m) => m.ListingDetailComponent
          ),
      },
      {
        path: ':id/edit',
        loadComponent: () =>
          import('./features/listings/components/listing-edit/listing-edit.component').then(
            (m) => m.ListingEditComponent
          ),
        canActivate: [authGuard],
      },
      {
        path: ':id/checkout',
        loadComponent: () =>
          import('./features/rentals/components/rental-checkout/rental-checkout.component').then(
            (m) => m.RentalCheckoutComponent
          ),
        canActivate: [authGuard],
      },
    ],
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/profile/components/profile-view/profile-view.component').then(
            (m) => m.ProfileViewComponent
          ),
      },
      {
        path: 'edit',
        loadComponent: () =>
          import('./features/profile/components/profile-edit/profile-edit.component').then(
            (m) => m.ProfileEditComponent
          ),
      },
      {
        path: 'kyc',
        loadComponent: () =>
          import('./features/profile/components/kyc-wizard/kyc-wizard.component').then(
            (m) => m.KycWizardComponent
          ),
      },
      {
        path: 'gdpr',
        loadComponent: () =>
          import('./features/profile/components/gdpr-agreement/gdpr-agreement.component').then(
            (m) => m.GdprAgreementComponent
          ),
      },
    ],
  },
  {
    path: 'users/:id',
    loadComponent: () =>
      import('./features/profile/components/profile-view/profile-view.component').then(
        (m) => m.ProfileViewComponent
      ),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/dashboard/components/dashboard-home/dashboard-home.component').then(
            (m) => m.DashboardHomeComponent
          ),
      },
      {
        path: 'listings',
        loadComponent: () =>
          import('./features/dashboard/components/my-listings/my-listings.component').then(
            (m) => m.MyListingsComponent
          ),
      },
      {
        path: 'rentals',
        loadComponent: () =>
          import('./features/dashboard/components/my-rentals/my-rentals.component').then(
            (m) => m.MyRentalsComponent
          ),
      },
      {
        path: 'earnings',
        loadComponent: () =>
          import('./features/dashboard/components/my-earnings/my-earnings.component').then(
            (m) => m.MyEarningsComponent
          ),
      },
      {
        path: 'wishlist',
        loadComponent: () =>
          import('./features/dashboard/components/wishlist/wishlist.component').then(
            (m) => m.WishlistComponent
          ),
      },
    ],
  },
  {
    path: 'rentals/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/rentals/components/rental-status/rental-status.component').then(
        (m) => m.RentalStatusComponent
      ),
  },
  {
    path: 'chat',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/chat/components/chat-list/chat-list.component').then(
            (m) => m.ChatListComponent
          ),
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./features/chat/components/chat-room/chat-room.component').then(
            (m) => m.ChatRoomComponent
          ),
      },
    ],
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/admin/components/admin-dashboard/admin-dashboard.component').then(
            (m) => m.AdminDashboardComponent
          ),
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./features/admin/components/admin-users/admin-users.component').then(
            (m) => m.AdminUsersComponent
          ),
      },
      {
        path: 'smtp',
        loadComponent: () =>
          import('./features/admin/components/admin-smtp/admin-smtp.component').then(
            (m) => m.AdminSmtpComponent
          ),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
