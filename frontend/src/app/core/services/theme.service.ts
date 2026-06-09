import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'rentit_theme';
  private readonly darkMode$ = new BehaviorSubject<boolean>(false);

  constructor() {
    const saved = localStorage.getItem(this.STORAGE_KEY);
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const isDark = saved ? saved === 'dark' : prefersDark;
    this.apply(isDark);
  }

  toggle(): void {
    this.apply(!this.darkMode$.value);
  }

  setDark(dark: boolean): void {
    this.apply(dark);
  }

  isDark$(): Observable<boolean> {
    return this.darkMode$.asObservable();
  }

  get isDark(): boolean {
    return this.darkMode$.value;
  }

  private apply(dark: boolean): void {
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
    localStorage.setItem(this.STORAGE_KEY, dark ? 'dark' : 'light');
    this.darkMode$.next(dark);
  }
}
