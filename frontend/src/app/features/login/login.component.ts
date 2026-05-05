import { Component, signal } from '@angular/core';
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
import { passwordMatchValidator } from '../../shared/validators/custom.validators';

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
  constructor(private router: Router) {}

  loginForm = new FormGroup(
    {
      email: new FormControl<string>('', [Validators.required, Validators.email]),
      password: new FormControl<string>('', Validators.required),
    },
    { validators: passwordMatchValidator },
  );

  public onSubmit() {}

  public onCreateNewAccount() {
    this.router.navigate(['/register']);
  }
}
