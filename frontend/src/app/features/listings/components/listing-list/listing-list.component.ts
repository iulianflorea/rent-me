import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { ListingsApiService, ListingSummary, SearchParams } from '../../services/listings-api.service';
import { ListingCardComponent } from '../listing-card/listing-card.component';
import { SkeletonLoaderComponent } from '../../../../shared/components/skeleton-loader/skeleton-loader.component';

@Component({
  selector: 'app-listing-list',
  imports: [ReactiveFormsModule, TranslateModule, ListingCardComponent, SkeletonLoaderComponent],
  templateUrl: './listing-list.component.html',
  styleUrl: './listing-list.component.scss',
})
export class ListingListComponent implements OnInit, OnDestroy {
  listings: ListingSummary[] = [];
  loading = true;
  totalElements = 0;
  currentPage = 0;
  pageSize = 12;
  skeletons = Array(8);

  private destroy$ = new Subject<void>();

  categories = ['TOOLS', 'VEHICLES', 'REAL_ESTATE', 'ELECTRONICS', 'SPORTS', 'OTHER'];
  sortOptions = [
    { value: 'newest', label: 'listing.filters.sortOptions.newest' },
    { value: 'priceAsc', label: 'listing.filters.sortOptions.priceAsc' },
    { value: 'priceDesc', label: 'listing.filters.sortOptions.priceDesc' },
    { value: 'rating', label: 'listing.filters.sortOptions.rating' },
  ];

  filterForm: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private listingsApi: ListingsApiService
  ) {
    this.filterForm = this.fb.group({
      search: [''],
      city: [''],
      county: [''],
      category: [''],
      minPrice: [null as number | null],
      maxPrice: [null as number | null],
      sortBy: ['newest'],
      verifiedOnly: [false],
    });
  }

  ngOnInit(): void {
    this.search();
    this.filterForm.valueChanges
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(() => {
        this.currentPage = 0;
        this.search();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  search(): void {
    this.loading = true;
    const vals = this.filterForm.value;
    const params: SearchParams = {
      search: vals.search || undefined,
      city: vals.city || undefined,
      county: vals.county || undefined,
      category: vals.category || undefined,
      minPrice: vals.minPrice || undefined,
      maxPrice: vals.maxPrice || undefined,
      sortBy: vals.sortBy || 'newest',
      verifiedOnly: vals.verifiedOnly || undefined,
      page: this.currentPage,
      size: this.pageSize,
    };
    this.listingsApi.search(params).subscribe({
      next: (res) => {
        this.listings = res.content;
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  get totalPages(): number {
    return Math.ceil(this.totalElements / this.pageSize);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.search();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.search();
    }
  }

  resetFilters(): void {
    this.filterForm.patchValue({
      city: '',
      county: '',
      category: '',
      minPrice: null,
      maxPrice: null,
      verifiedOnly: false,
      sortBy: 'newest',
    });
  }
}
