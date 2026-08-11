import { Component, DestroyRef, inject, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faChevronLeft } from '@fortawesome/free-solid-svg-icons';
import { DefaultInputComponent } from '../../shared/components/default-input/default-input.component';
import { DefaultButtonComponent } from '../../shared/components/default-button/default-button.component';
import { ageLimitValidator, valueMatchValidator } from '../../shared/validators/custom.validators';
import { Location } from '@angular/common';
import { AuthService } from '../../core/services/auth-service/auth.service';
import { RegisterRequest } from '../../core/models/auth/register-request';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

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
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  private authService = inject(AuthService);
  private router = inject(Router);
  private location = inject(Location);
  private destroyRef = inject(DestroyRef);

  registerForm = new FormGroup({
    name: new FormControl<string>('', [Validators.required]),
    username: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(5),
      Validators.maxLength(20),
      Validators.pattern('^[A-Za-z]{5}.*$'),
    ]),
    email: new FormControl<string>('', [Validators.required, Validators.email]),
    birthday: new FormControl<string>('', [Validators.required, ageLimitValidator(13)]),
    passwords: new FormGroup(
      {
        password: new FormControl<string>('', [Validators.required]),
        confirmPassword: new FormControl<string>('', [Validators.required]),
      },
      { validators: valueMatchValidator('password', 'confirmPassword') },
    ),
  });

  get f() {
    return this.registerForm.controls;
  }

  goBack() {
    if (!this.isLoading()) {
      this.registerForm.reset();
      this.location.back();
    }
    return;
  }

  onSubmit() {
    if (!this.isLoading()) {
      if (this.registerForm.invalid) {
        this.registerForm.markAllAsTouched();
        return;
      }
      this.isLoading.set(true);
      const registerRequest: RegisterRequest = {
        username: this.registerForm.get('username')!.value!,
        profileName: this.registerForm.get('name')!.value!,
        email: this.registerForm.get('email')!.value!,
        password: this.registerForm.controls.passwords.get('password')!.value!,
        passwordConfirm: this.registerForm.controls.passwords.get('confirmPassword')!.value!,
        dateOfBirth: this.registerForm.get('birthday')!.value!,
        termsAccepted: true,
      };

      this.authService
        .register(registerRequest)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.isLoading.set(false);
            this.registerForm.reset();
            this.router.navigate(['/login']);
          },
          error: (err) => {
            if (err.status === 409) {
              this.errorMessage.set('Username or email already exists.');
            } else {
              this.errorMessage.set('An unexpected error occurred. Please try again.');
            }
            this.isLoading.set(false);
          },
        });
    }
    return;
  }
}
