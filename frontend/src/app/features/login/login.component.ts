import { Component, inject, signal } from '@angular/core';
import { DefaultInputComponent } from '../../shared/components/default-input/default-input.component';
import { DefaultButtonComponent } from '../../shared/components/default-button/default-button.component';
import { DefaultHyperlinkComponent } from '../../shared/components/default-hyperlink/default-hyperlink.component';
import { Router } from '@angular/router';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
  ɵInternalFormsSharedModule,
} from '@angular/forms';
import { AuthService } from '../../core/services/auth-service/auth.service';
import { LoginRequest } from '../../core/models/auth/login-request';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-login',
  imports: [
    DefaultInputComponent,
    DefaultButtonComponent,
    DefaultHyperlinkComponent,
    ɵInternalFormsSharedModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  private router = inject(Router);
  private authService = inject(AuthService);

  loginForm = new FormGroup({
    username: new FormControl<string>('', [Validators.required]),
    password: new FormControl<string>('', Validators.required),
  });

  public onSubmit() {
    if (!this.isLoading()) {
      if (this.loginForm.invalid) {
        this.loginForm.markAllAsTouched();
        return;
      } else {
        this.isLoading.set(true);
        const loginRequest: LoginRequest = {
          username: this.loginForm.get('username')!.value!,
          password: this.loginForm.get('password')!.value!,
        };

        this.authService
          .login(loginRequest)
          .pipe(finalize(() => this.isLoading.set(false)))
          .subscribe({
            next: () => {
              this.authService.checkInitialAuth().subscribe(() => {
                this.loginForm.reset();
                this.router.navigate(['/dashboard']);
              });
            },
            error: (err) => {
              if (err.status === 401) {
                this.errorMessage.set('Username or password is invalid');
              } else {
                console.log(err);

                this.errorMessage.set('An unexpected error occurred. Please try again.');
              }
            },
          });
      }
    }
  }

  public onCreateNewAccount() {
    this.router.navigate(['/register']);
  }
}
