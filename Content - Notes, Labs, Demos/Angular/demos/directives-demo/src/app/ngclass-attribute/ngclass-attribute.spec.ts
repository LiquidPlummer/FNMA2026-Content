import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NgclassAttribute } from './ngclass-attribute';

describe('NgclassAttribute', () => {
  let component: NgclassAttribute;
  let fixture: ComponentFixture<NgclassAttribute>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NgclassAttribute]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NgclassAttribute);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
