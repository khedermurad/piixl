import { Component, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faChevronLeft } from '@fortawesome/free-solid-svg-icons';
import { DefaultInputComponent } from '../../shared/components/default-input/default-input.component';
import { DefaultButtonComponent } from '../../shared/components/default-button/default-button.component';
import {
  ageLimitValidator,
  passwordMatchValidator,
} from '../../shared/validators/custom.validators';
import { Location } from '@angular/common';

@Component({
  selector: 'app-register',
  imports: [
    FontAwesomeModule,
    FormsModule,
    DefaultInputComponent,
    DefaultButtonComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  faChevronLeft = faChevronLeft;

  isLoading = signal(false);

  constructor(private location: Location) {}

  registerForm = new FormGroup(
    {
      name: new FormControl<string>('', [Validators.required]),
      username: new FormControl<string>('', [
        Validators.required,
        Validators.minLength(5),
        Validators.maxLength(20),
        Validators.pattern('^[A-Za-z]{5}.*'),
      ]),
      email: new FormControl<string>('', [Validators.required, Validators.email]),
      birthday: new FormControl<string>('', [Validators.required, ageLimitValidator(13)]),
      password: new FormControl<string>('', [Validators.required]),
      confirmPassword: new FormControl<string>('', [Validators.required]),
    },
    { validators: passwordMatchValidator },
  );

  get f() {
    return this.registerForm.controls;
  }

  goBack() {
    this.location.back();
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }
    console.log('Valid data');

    // logic
    //this.goBack();
  }
}
