import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TripDetailDialog } from './trip-detail-dialog';

describe('TripDetailDialog', () => {
  let component: TripDetailDialog;
  let fixture: ComponentFixture<TripDetailDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TripDetailDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TripDetailDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
