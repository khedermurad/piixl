import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function ageLimitValidator(maxAge: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;

    const age = calculateAge(control.value);

    return age < maxAge ? { maxAge: { requiredAge: maxAge, actualAge: age } } : null;
  };
}

export function valueMatchValidator(controlName1: string, controlName2: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const control1 = group.get(controlName1);
    const control2 = group.get(controlName2);

    if (!control1 || !control2) {
      return null;
    }
    const isMismatch = control1.value !== control2.value;

    if (isMismatch) {
      control2.setErrors({ ...control2.errors, mismatch: true });
      return { mismatch: true };
    } else {
      if (control2.hasError('mismatch')) {
        const { mismatch, ...otherErrors } = control2.errors || {};
        control2.setErrors(Object.keys(otherErrors).length ? otherErrors : null);
      }
      return null;
    }
  };
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
