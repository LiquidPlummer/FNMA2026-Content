import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OldFashionedComponent } from './old-fashioned-component';

describe('OldFashionedComponent', () => {
  let component: OldFashionedComponent;
  let fixture: ComponentFixture<OldFashionedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OldFashionedComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OldFashionedComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
