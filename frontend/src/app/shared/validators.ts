import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Le salaire maximum doit être supérieur ou égal au minimum (miroir de OfferRequest.isSalaryRangeValid). */
export const salaryRangeValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const min = group.get('salaryMin')?.value as number | null;
  const max = group.get('salaryMax')?.value as number | null;
  if (min === null || max === null || min === undefined || max === undefined) {
    return null;
  }
  return max >= min ? null : { salaryRange: true };
};

/** Les deux champs de mot de passe doivent être identiques. */
export const passwordsMatchValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const password = group.get('password')?.value as string;
  const confirmation = group.get('confirmPassword')?.value as string;
  return !confirmation || password === confirmation ? null : { passwordsMismatch: true };
};

/** Au moins une lettre et un chiffre (même règle que le back-end). */
export const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).+$/;
