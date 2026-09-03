import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomDirective } from './custom-directive';

describe('CustomDirective', () => {
  let component: CustomDirective;
  let fixture: ComponentFixture<CustomDirective>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomDirective]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CustomDirective);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
