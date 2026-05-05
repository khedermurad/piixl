import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function ageLimitValidator(maxAge: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;

    const age = calculateAge(control.value);

    return age < maxAge ? { maxAge: { requiredAge: maxAge, actualAge: age } } : null;
  };
}

export function passwordMatchValidator(g: AbstractControl): ValidationErrors | null {
  const password = g.get('password');
  const confirmPassword = g.get('confirmPassword');

  if (!password || !confirmPassword) return null;

  if (password.value !== confirmPassword.value) {
    confirmPassword.setErrors({ mismatch: true });
    return { mismatch: true };
  } else {
    if (confirmPassword.hasError('mismatch')) {
      confirmPassword.setErrors(null);
    }
    return null;
  }
}

function calculateAge(birthdate: string | Date): number {
  const today = new Date();
  const birthDate = new Date(birthdate);

  let age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();

  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age--;
  }

  return age;
}
