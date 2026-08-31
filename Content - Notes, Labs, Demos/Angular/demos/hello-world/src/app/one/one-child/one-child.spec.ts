import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OneChild } from './one-child';

describe('OneChild', () => {
  let component: OneChild;
  let fixture: ComponentFixture<OneChild>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OneChild]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OneChild);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
