import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-skeleton',
  template: `<div class="skeleton" [style.width]="width" [style.height]="height" [style.border-radius]="radius"></div>`,
  styles: [`:host { display: block; }`],
})
export class SkeletonLoaderComponent {
  @Input() width = '100%';
  @Input() height = '16px';
  @Input() radius = 'var(--radius-sm)';
}
