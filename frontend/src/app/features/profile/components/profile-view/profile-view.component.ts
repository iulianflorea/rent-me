import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { ProfileApiService, UserProfileFull } from '../../services/profile-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { VerificationBadgeComponent } from '../../../../shared/components/verification-badge/verification-badge.component';
import { StarRatingComponent } from '../../../../shared/components/star-rating/star-rating.component';
import { SkeletonLoaderComponent } from '../../../../shared/components/skeleton-loader/skeleton-loader.component';

@Component({
  selector: 'app-profile-view',
  imports: [RouterLink, TranslateModule, DatePipe, VerificationBadgeComponent, StarRatingComponent, SkeletonLoaderComponent],
  templateUrl: './profile-view.component.html',
  styleUrl: './profile-view.component.scss',
})
export class ProfileViewComponent implements OnInit {
  profile: UserProfileFull | null = null;
  loading = true;
  isOwnProfile = false;

  constructor(
    private route: ActivatedRoute,
    private profileApi: ProfileApiService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.profileApi.getById(Number(id)).subscribe({
        next: (p) => { this.profile = p; this.loading = false; this.isOwnProfile = p.id === this.auth.currentUser?.id; },
        error: () => { this.loading = false; },
      });
    } else {
      this.profileApi.getMe().subscribe({
        next: (p) => { this.profile = p; this.loading = false; this.isOwnProfile = true; },
        error: () => { this.loading = false; },
      });
    }
  }
}
