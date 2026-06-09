import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'timeAgo', pure: false })
export class TimeAgoPipe implements PipeTransform {
  transform(value: string | Date): string {
    const date = new Date(value);
    const now = new Date();
    const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (seconds < 60) return 'acum';
    if (seconds < 3600) return `${Math.floor(seconds / 60)} min`;
    if (seconds < 86400) return `${Math.floor(seconds / 3600)} ore`;
    if (seconds < 2592000) return `${Math.floor(seconds / 86400)} zile`;
    return date.toLocaleDateString('ro-RO');
  }
}
