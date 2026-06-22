import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faChevronLeft, faL } from '@fortawesome/free-solid-svg-icons';
import { DefaultInputComponent } from '../../shared/components/default-input/default-input.component';
import { DefaultButtonComponent } from '../../shared/components/default-button/default-button.component';
import {
  ageLimitValidator,
  passwordMatchValidator,
} from '../../shared/validators/custom.validators';
import { Location } from '@angular/common';
import { AuthService } from '../../core/services/auth-service/auth.service';
import { RegisterRequest } from '../../core/models/register-request';
import { RegisterResponse } from '../../core/models/register-response';
import { Router } from '@angular/router';

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
  private isLoading = signal<boolean>(false);
  private responseData = signal<RegisterResponse | null>(null);

  private authService = inject(AuthService);
  private router = inject(Router);

  constructor(private location: Location) {}

  registerForm = new FormGroup(
    {
      name: new FormControl<string>('', [Validators.required]),
      username: new FormControl<string>('', [
        Validators.required,
        Validators.minLength(5),
        Validators.maxLength(20),
        Validators.pattern('^[A-Za-z]{5}.*$'),
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
    this.registerForm.reset();
    this.location.back();
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }
    this.isLoading.set(true);
    const registerRequest: RegisterRequest = {
      username: this.registerForm.get('username')?.value!,
      profileName: this.registerForm.get('name')?.value!,
      email: this.registerForm.get('email')?.value!,
      password: this.registerForm.get('password')?.value!,
      passwordConfirm: this.registerForm.get('confirmPassword')?.value!,
      dateOfBirth: this.registerForm.get('birthday')?.value!,
      termsAccepted: true,
    };

    console.log(registerRequest);

    this.authService.register(registerRequest).subscribe({
      next: (response) => {
        this.responseData.set(response);
        this.isLoading.set(false);
        this.registerForm.reset();
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.log(err);
        this.isLoading.set(false);
      },
    });
  }
}
