import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DistantComponent } from './distant-component';

describe('DistantComponent', () => {
  let component: DistantComponent;
  let fixture: ComponentFixture<DistantComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DistantComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DistantComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
