import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Disruptions } from './disruptions';

describe('Disruptions', () => {
  let component: Disruptions;
  let fixture: ComponentFixture<Disruptions>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Disruptions]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Disruptions);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
