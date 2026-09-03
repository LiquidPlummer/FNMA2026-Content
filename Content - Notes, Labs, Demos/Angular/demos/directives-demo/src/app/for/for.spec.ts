import { ComponentFixture, TestBed } from '@angular/core/testing';

import { For } from './for';

describe('For', () => {
  let component: For;
  let fixture: ComponentFixture<For>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [For]
    })
    .compileComponents();

    fixture = TestBed.createComponent(For);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
