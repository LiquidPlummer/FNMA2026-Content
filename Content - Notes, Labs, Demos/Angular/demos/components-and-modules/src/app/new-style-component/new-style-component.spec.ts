import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewStyleComponent } from './new-style-component';

describe('NewStyleComponent', () => {
  let component: NewStyleComponent;
  let fixture: ComponentFixture<NewStyleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewStyleComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewStyleComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
