import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DecimalPipe } from '@angular/common';
import { Subject, debounceTime, distinctUntilChanged, switchMap, takeUntil, of, catchError, startWith } from 'rxjs';
import { ListingsApiService, PaymentSplitPreview } from '../../services/listings-api.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-listing-edit',
  imports: [ReactiveFormsModule, RouterLink, TranslateModule, DecimalPipe],
  templateUrl: '../listing-create/listing-create.component.html',
  styleUrl: '../listing-create/listing-create.component.scss',
})
export class ListingEditComponent implements OnInit, OnDestroy {
  categories = ['TOOLS', 'VEHICLES', 'REAL_ESTATE', 'ELECTRONICS', 'SPORTS', 'OTHER'];
  selectedFiles: File[] = [];
  previews: string[] = [];
  loading = false;
  listingId!: number;
  paymentPreview: PaymentSplitPreview | null = null;
  toolTags: string[] = [];

  private destroy$ = new Subject<void>();
  private priceChange$ = new Subject<{ price: number; category: string }>();

  form: ReturnType<FormBuilder['group']>;

  readonly stareOptions = ['Nou', 'Ca nou', 'Folosit bine', 'Folosit cu urme'];
  readonly alimentareOptions = ['Priză 230V', 'Trifazat 380V', 'Baterie', 'Termic'];
  readonly tensiuneOptions = ['12V', '18V', '36V', '54V'];
  readonly tipSculaOptions = ['Bormaşină', 'Flex', 'Fierăstrău', 'Polizor', 'Compresor', 'Generator', 'Sudură', 'Altele'];
  readonly caroserieOptions = ['Berlină', 'Hatchback', 'Break', 'SUV', 'Van', 'Dubă', 'Pick-up', 'Auto-utilitară', 'Decapotabilă', 'Coupé'];
  readonly combustibilOptions = ['Benzină', 'Diesel', 'Electric', 'Hibrid', 'GPL'];
  readonly transmisieOptions = ['Manuală', 'Automată', 'Semi-automată'];
  readonly tractiuneOptions = ['2WD', '4WD', 'AWD'];
  readonly dotariOptions = ['AC', 'GPS', 'Scaun copil', 'Bluetooth', 'Cruise control', 'Cameră marșarier', 'Senzori parcare', 'USB', 'Climatizare bi-zonă'];
  readonly permisOptions = ['B', 'B+E', 'C', 'D', 'Fără permis'];
  readonly tipProprietateOptions = ['Apartament', 'Casă', 'Vilă', 'Cameră', 'Studio', 'Spațiu comercial', 'Teren'];
  readonly facilitatiOptions = ['WiFi', 'Parcare', 'AC', 'Încălzire centrală', 'Terasă', 'Grădină', 'Piscină', 'Lift', 'Mașină spălat', 'Uscător', 'Bucătărie echipată', 'Animale permise', 'Fumători permis'];
  readonly regimOptions = ['Zilnic', 'Săptămânal', 'Lunar'];
  readonly tipElectronicsOptions = ['Laptop', 'Camera foto/video', 'Dronă', 'Proiector', 'Consolă jocuri', 'Echipament audio', 'Altele'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private listingsApi: ListingsApiService,
    private router: Router,
    private notifications: NotificationService
  ) {
    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5)]],
      category: ['TOOLS', Validators.required],
      description: ['', [Validators.required, Validators.minLength(20)]],
      pricePerDay: [null as number | null, [Validators.required, Validators.min(1)]],
      pricePerWeek: [null as number | null],
      pricePerMonth: [null as number | null],
      city: ['', Validators.required],
      county: ['', Validators.required],
      address: [''],
      attrs: this.buildToolsAttrs(),
    });
  }

  ngOnInit(): void {
    this.form.get('category')!.valueChanges.pipe(
      startWith(this.form.get('category')!.value),
      takeUntil(this.destroy$)
    ).subscribe((cat) => {
      this.toolTags = [];
      this.form.setControl('attrs', this.buildAttrs(cat));
    });

    this.form.get('pricePerDay')!.valueChanges.pipe(takeUntil(this.destroy$)).subscribe((price) => {
      if (price && price > 0) {
        if (!this.form.get('pricePerWeek')!.touched) {
          this.form.get('pricePerWeek')!.setValue(Math.round(price * 7 * 0.9), { emitEvent: false });
        }
        if (!this.form.get('pricePerMonth')!.touched) {
          this.form.get('pricePerMonth')!.setValue(Math.round(price * 30 * 0.8), { emitEvent: false });
        }
      }
      this.emitPreview();
    });

    this.form.get('category')!.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(() => this.emitPreview());

    this.priceChange$.pipe(
      debounceTime(400),
      distinctUntilChanged((a, b) => a.price === b.price && a.category === b.category),
      switchMap(({ price, category }) =>
        price > 0
          ? this.listingsApi.getPaymentPreview(price, 1, category).pipe(catchError(() => of(null)))
          : of(null)
      ),
      takeUntil(this.destroy$)
    ).subscribe((preview) => (this.paymentPreview = preview));

    this.listingId = Number(this.route.snapshot.paramMap.get('id'));
    this.listingsApi.getById(this.listingId).subscribe((l) => {
      this.form.patchValue({
        title: l.title,
        category: l.category,
        description: l.description,
        pricePerDay: l.pricePerDay,
        pricePerWeek: l.pricePerWeek,
        pricePerMonth: l.pricePerMonth,
        city: l.city,
        county: l.county,
        address: l.address,
      });
      this.previews = l.images.map((img) => img.url);
      this.form.setControl('attrs', this.buildAttrs(l.category));
      const attrsJson = typeof l.categoryAttributes === 'string'
        ? l.categoryAttributes
        : JSON.stringify(l.categoryAttributes);
      this.patchAttrs(attrsJson);
      this.emitPreview();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get currentCategory(): string {
    return this.form.get('category')!.value || 'TOOLS';
  }

  get attrsGroup(): FormGroup {
    return this.form.get('attrs') as FormGroup;
  }

  private emitPreview(): void {
    const price = this.form.get('pricePerDay')!.value;
    const category = this.currentCategory;
    if (price && price > 0) {
      this.priceChange$.next({ price, category });
    } else {
      this.paymentPreview = null;
    }
  }

  buildAttrs(category: string): FormGroup {
    switch (category) {
      case 'TOOLS':       return this.buildToolsAttrs();
      case 'VEHICLES':    return this.buildVehiclesAttrs();
      case 'REAL_ESTATE': return this.buildRealEstateAttrs();
      case 'ELECTRONICS': return this.buildElectronicsAttrs();
      case 'SPORTS':      return this.buildSportsAttrs();
      default:            return this.buildOtherAttrs();
    }
  }

  patchAttrs(json: string | null | undefined): void {
    if (!json) return;
    try {
      const data = JSON.parse(json);
      if (Array.isArray(data.functiiPrincipale)) {
        this.toolTags = data.functiiPrincipale;
      }
      this.attrsGroup.patchValue(data, { emitEvent: false });
    } catch { /* ignore */ }
  }

  private buildToolsAttrs(): FormGroup {
    return this.fb.group({
      brand: [''],
      model: [''],
      putere: [null as number | null],
      alimentare: ['Priză 230V'],
      tensiuneBaterie: [''],
      capacitateBaterie: [null as number | null],
      tipScula: [''],
      stare: ['Nou'],
      includeAcesorii: [''],
    });
  }

  private buildVehiclesAttrs(): FormGroup {
    return this.fb.group({
      marca: ['', Validators.required],
      model: ['', Validators.required],
      anFabricatie: [null as number | null, [Validators.required, Validators.min(1900), Validators.max(new Date().getFullYear())]],
      caroserie: [''],
      cilindree: [null as number | null],
      putereMotor: [null as number | null],
      combustibil: ['Benzină'],
      transmisie: ['Manuală'],
      rulaj: [null as number | null],
      nrLocuri: [null as number | null],
      capacitatePortbagaj: [null as number | null],
      tractiune: ['2WD'],
      dotari: this.fb.group(
        Object.fromEntries(this.dotariOptions.map(d => [d, [false]]))
      ),
      permisNecesar: ['B'],
      asigurareInclusa: [false],
      franciza: [null as number | null],
    });
  }

  private buildRealEstateAttrs(): FormGroup {
    return this.fb.group({
      tipProprietate: ['Apartament'],
      suprafataUtila: [null as number | null, Validators.required],
      suprafataTotal: [null as number | null],
      nrCamere: [null as number | null],
      nrBai: [null as number | null],
      etaj: [null as number | null],
      totalEtaje: [null as number | null],
      facilitati: this.fb.group(
        Object.fromEntries(this.facilitatiOptions.map(f => [f, [false]]))
      ),
      regim: ['Zilnic'],
      checkIn: [''],
      checkOut: [''],
      reguliCasa: [''],
      nrMaxOaspeti: [null as number | null],
    });
  }

  private buildElectronicsAttrs(): FormGroup {
    return this.fb.group({
      tipDevice: ['Laptop'],
      brand: [''],
      model: [''],
      stare: ['Nou'],
      specificatii: [''],
      accesoriiIncluse: [''],
    });
  }

  private buildSportsAttrs(): FormGroup {
    return this.fb.group({
      tipSport: ['', Validators.required],
      dimensiune: [''],
      stare: ['Nou'],
      includeProtectie: [false],
    });
  }

  private buildOtherAttrs(): FormGroup {
    return this.fb.group({
      tipObiect: [''],
      stare: ['Nou'],
      dimensiuni: [''],
      greutate: [''],
      note: [''],
    });
  }

  addToolTag(value: string): void {
    const tag = value.trim().replace(/,$/, '');
    if (tag && !this.toolTags.includes(tag)) {
      this.toolTags.push(tag);
    }
  }

  removeToolTag(index: number): void {
    this.toolTags.splice(index, 1);
  }

  onToolTagKeydown(event: KeyboardEvent, input: HTMLInputElement): void {
    if (event.key === 'Enter' || event.key === ',') {
      event.preventDefault();
      this.addToolTag(input.value);
      input.value = '';
    }
  }

  onFilesChange(event: Event): void {
    const files = Array.from((event.target as HTMLInputElement).files || []);
    const total = this.previews.length + files.length;
    if (total > 10) { this.notifications.error('listing.create.maxImages'); return; }
    this.selectedFiles.push(...files);
    files.forEach((f) => this.previews.push(URL.createObjectURL(f)));
  }

  removeImage(index: number): void {
    const isExisting = index < (this.previews.length - this.selectedFiles.length);
    if (!isExisting) {
      const fileIndex = index - (this.previews.length - this.selectedFiles.length);
      this.selectedFiles.splice(fileIndex, 1);
    }
    this.previews.splice(index, 1);
  }

  submit(): void {
    if (this.form.invalid || this.loading) return;
    this.loading = true;

    const attrsValue: Record<string, unknown> = { ...this.attrsGroup.value };
    if (this.currentCategory === 'TOOLS') {
      attrsValue['functiiPrincipale'] = [...this.toolTags];
    }

    const payload: Record<string, unknown> = {};
    ['title', 'category', 'description', 'pricePerDay', 'pricePerWeek', 'pricePerMonth', 'city', 'county', 'address'].forEach((k) => {
      const v = this.form.get(k)!.value;
      if (v != null && v !== '') payload[k] = v;
    });
    payload['categoryAttributes'] = JSON.stringify(attrsValue);

    this.listingsApi.update(this.listingId, payload).subscribe({
      next: () => {
        if (this.selectedFiles.length > 0) {
          this.listingsApi.uploadImages(this.listingId, this.selectedFiles).subscribe({
            next: () => {
              this.notifications.success('listing.edit.success');
              this.router.navigate(['/listings', this.listingId]);
            },
            error: () => { this.loading = false; },
          });
        } else {
          this.notifications.success('listing.edit.success');
          this.router.navigate(['/listings', this.listingId]);
        }
      },
      error: () => { this.loading = false; },
    });
  }
}
