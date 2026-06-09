import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, Observable } from 'rxjs';

export type Lang = 'ro' | 'en';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly STORAGE_KEY = 'rentit_lang';
  private readonly lang$ = new BehaviorSubject<Lang>('ro');

  constructor(private translate: TranslateService) {
    translate.addLangs(['ro', 'en']);
    translate.setDefaultLang('ro');

    const saved = localStorage.getItem(this.STORAGE_KEY) as Lang | null;
    const browserLang = translate.getBrowserLang() as Lang;
    const initial: Lang = saved || (['ro', 'en'].includes(browserLang) ? browserLang : 'ro');
    this.use(initial);
  }

  use(lang: Lang): void {
    this.translate.use(lang);
    localStorage.setItem(this.STORAGE_KEY, lang);
    this.lang$.next(lang);
    document.documentElement.setAttribute('lang', lang);
  }

  current$(): Observable<Lang> {
    return this.lang$.asObservable();
  }

  get current(): Lang {
    return this.lang$.value;
  }
}
