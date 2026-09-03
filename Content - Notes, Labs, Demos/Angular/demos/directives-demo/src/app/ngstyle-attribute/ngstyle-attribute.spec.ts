import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NgstyleAttribute } from './ngstyle-attribute';

describe('NgstyleAttribute', () => {
  let component: NgstyleAttribute;
  let fixture: ComponentFixture<NgstyleAttribute>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NgstyleAttribute]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NgstyleAttribute);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
