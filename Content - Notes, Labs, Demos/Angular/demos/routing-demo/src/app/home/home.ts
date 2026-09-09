import { Component, inject } from '@angular/core';
import { LockService } from '../lock-service';

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
})
export class Home {
  protected lock = inject(LockService);
}
