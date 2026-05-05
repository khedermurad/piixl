import { Component, computed, inject, input } from '@angular/core';
import { FormControl, FormGroupDirective, ReactiveFormsModule } from '@angular/forms';
import { log } from 'console';

@Component({
  selector: 'app-default-input',
  imports: [ReactiveFormsModule],
  templateUrl: './default-input.component.html',
  styleUrl: './default-input.component.scss',
})
export class DefaultInputComponent {
  label = input<string>('');
  id = input<string>(`input-${Math.random().toString(36).toLowerCase().substring(2, 9)}`);
  type = input<'text' | 'email' | 'password' | 'number' | 'date'>('text');
  placeholder = input<string>('');

  control = input<FormControl>();

  private formGroupDirective = inject(FormGroupDirective, { optional: true });

  get showError(): boolean {
    const ctrl = this.control();
    const isSubmitted = this.formGroupDirective?.submitted ?? false;

    return ctrl ? ctrl.invalid && isSubmitted : false;
  }
}
