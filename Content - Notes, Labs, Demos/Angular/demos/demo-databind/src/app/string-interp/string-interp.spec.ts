import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StringInterp } from './string-interp';

describe('StringInterp', () => {
  let component: StringInterp;
  let fixture: ComponentFixture<StringInterp>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StringInterp]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StringInterp);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
