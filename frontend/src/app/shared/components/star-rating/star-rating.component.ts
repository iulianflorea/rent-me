import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-star-rating',
  template: `
    <div class="stars">
      @for (star of stars; track $index) {
        <svg
          xmlns="http://www.w3.org/2000/svg"
          [attr.width]="size"
          [attr.height]="size"
          viewBox="0 0 24 24"
          [attr.fill]="$index < fullStars ? 'currentColor' : 'none'"
          stroke="currentColor"
          stroke-width="2"
        >
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
        </svg>
      }
      @if (showCount && count > 0) {
        <span class="count">({{ count }})</span>
      }
    </div>
  `,
  styles: [`
    .stars {
      display: inline-flex;
      align-items: center;
      gap: 2px;
      color: #ff9f0a;
    }
    .count {
      font-size: 13px;
      color: var(--color-text-secondary);
      margin-left: 4px;
    }
  `],
})
export class StarRatingComponent {
  @Input() rating = 0;
  @Input() count = 0;
  @Input() size = 14;
  @Input() showCount = true;

  readonly stars = [1, 2, 3, 4, 5];

  get fullStars(): number {
    return Math.round(this.rating);
  }
}
