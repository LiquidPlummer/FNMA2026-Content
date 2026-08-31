import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { CalculatorService } from './calculator.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    CalculatorService
  ]
};
