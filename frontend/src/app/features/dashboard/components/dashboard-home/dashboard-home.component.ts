import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DecimalPipe } from '@angular/common';
import { DashboardApiService, DashboardOverview } from '../../services/dashboard-api.service';
import { SkeletonLoaderComponent } from '../../../../shared/components/skeleton-loader/skeleton-loader.component';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-dashboard-home',
  imports: [RouterLink, TranslateModule, DecimalPipe, SkeletonLoaderComponent],
  templateUrl: './dashboard-home.component.html',
  styleUrl: './dashboard-home.component.scss',
})
export class DashboardHomeComponent implements OnInit {
  overview: DashboardOverview | null = null;
  loading = true;

  constructor(
    private dashApi: DashboardApiService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.dashApi.getOverview().subscribe({
      next: (o) => { this.overview = o; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }
}
